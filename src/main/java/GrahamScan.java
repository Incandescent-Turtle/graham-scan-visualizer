import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GrahamScan extends JPanel
{
	protected static final int HEIGHT = 500, WIDTH = 500;
	private static int WAIT = 10, AMOUNT = 100;

	/**
	 *		Type to represent the type of turn when determining concave rotations
	 */
	enum Turn { COUNTER_CLOCK_WISE, CLOCK_WISE, CO_LINEAR }

	private Point[] points;
	private Stack<Point> hull;

	public GrahamScan()
	{
		var f = new JFrame();
		var dim = new Dimension(WIDTH,HEIGHT);
		f.setPreferredSize(dim);
		f.setMinimumSize(dim);
		f.setLocationRelativeTo(null);
		f.add(this);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		var b = new JButton("shuffle");
		b.addActionListener(e->new Thread(()->{randomizeDots();repaint();}).start());
		add(b);

		var b1 = new JButton("run");
		b1.addActionListener(e-> new Thread(()->{randomizeDots();run();}).start());
		add(b1);

		var sp = new JSpinner(new SpinnerNumberModel(WAIT, 0, 1_000_000, 1));
		sp.addChangeListener(e -> WAIT = (int) sp.getValue());
		var l = new JLabel("Delay: ");
		add(l);
		add(sp);


		var sp1 = new JSpinner(new SpinnerNumberModel(AMOUNT, 0, 5000, 1));
		sp1.addChangeListener(e -> AMOUNT = (int) sp1.getValue());
		var l2 = new JLabel("Dot Amount: ");
		add(l2);
		add(sp1);

		randomizeDots();
		repaint();
		//	un-comment for it to continuously run
//		new Thread(()-> {
//			while(true)
//			{
//				randomizeDots();
//				run();
//				try
//				{
//					Thread.sleep(WAIT);
//				} catch (InterruptedException e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}).start();
	}

	/**
	 * 	randomizes the dot positions
	 */
	private void randomizeDots()
	{
		points = randomPoints(AMOUNT, WIDTH,HEIGHT);
		if(hull != null) hull.removeAllElements();
	}

	/**
	 *	runs the Graham Scan algorithm
	 */
	private void run()
	{
		//	point with lowest y (closest to 0)
		Point lowest = Arrays.stream(points).min(Comparator.comparingDouble(Point::getY)).get();
		lowest.setColor(Color.RED);
		//	sorts based on polar angle relative to the lowest point
		Arrays.sort(points, (p1, p2) -> comparePoints(lowest, p1, p2));
		hull = new Stack<>();
		hull.add(lowest);
		hull.add(points[1]);


		for (int i = 2; i < points.length; i++)
		{
			//	point we're looking at
			Point head = points[i];
			Point middle = hull.pop().setColor(Color.BLACK);
			Point tail = hull.peek();

			//	type of turn required for the head
			Turn turn = turnType(tail, middle, head);

			switch(turn)
			{
				case COUNTER_CLOCK_WISE:
					//	all points are POSSIBLE
					hull.push(middle.setColor(Color.GREEN));
					hull.push(head.setColor(Color.GREEN));
					break;
				case CO_LINEAR:
					//	middle is skipped (stays popped) and head is possible
					hull.push(head.setColor(Color.GREEN));
					break;
				case CLOCK_WISE:
					//	bad. middle stays popped.
					i--;
					break;
			}
			repaint();
			try
			{
				Thread.sleep(WAIT);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		//	complete hull
		hull.push(lowest);
		repaint();
	}

	/**
	 * a compare method to compare polar angles and distances for Graham Scan
	 * @param lowest the point with the lowest y value (closest to 0)
	 * @param p1 the first Point
	 * @param p2 the Second Point
	 * @return	> 0 if p1 has a greater polar angle or if angles are the same if it's further from the lowest point<br><br>
	 * 			= 0 if p1 and p2 are equal in position<br><br>
	 * 			< 0 if p1 has a smaller polar angle or if angles are the same if it's closer to the lowest point
	 */
	private static int comparePoints(Point lowest, Point p1, Point p2)
	{
		if(p1.getX() == p2.getX() && p1.getY() == p2.getY()) return 0;

		double thetaA = Math.atan2(p1.y - lowest.y, p1.x - lowest.x);
		double thetaB = Math.atan2(p2.y - lowest.y, p2.x - lowest.x);

		if(thetaA < thetaB) {
			return -1;
		}
		else if(thetaA > thetaB) {
			return 1;
		}
		else {
			// collinear with the 'lowest' point, let the point closest to it come first

			double distanceA = lowest.dist(p1);
			double distanceB = lowest.dist(p2);

			if(distanceA < distanceB) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}

	/**
	 * classifying the type of turn that must be made to connect p3 to p2 and p1
	 * @param p1	the first point
	 * @param p2	the midpoint, connected to p1
	 * @param p3	the last point, for which the turn is to be determined
	 * @return		1 for counter-clockwise<br>
	 * 				0 for co-linear<br>
	 * 				-1 for clockwise
	 */
	private static Turn turnType(Point p1, Point p2, Point p3)
	{
		int crossProduct = ((p2.x - p1.x) * (p3.y - p1.y)) - ((p2.y - p1.y) * (p3.x - p1.x));

		if(crossProduct > 0) return Turn.COUNTER_CLOCK_WISE;	//	counter-clockwise
		if(crossProduct < 0) return Turn.CLOCK_WISE;	//	clockwise
		return Turn.CO_LINEAR;	//	co-linear
	}

	/**
	 *
	 * @param size	the number of points to generate
	 * @param xMax	the max x value to generate points at (exclusive)
	 * @param yMax	the max y value to generate points at (exclusive)
	 * @return		returns size points between (0,0) and (xMax, yMax)
	 */
	private Point[] randomPoints(int size, int xMax, int yMax)
	{
		Point[] points = new Point[size];
		var rand = new Random();
		for (int i = 0; i < size; i++)
		{
			points[i] = new Point(this, rand.nextInt(xMax), rand.nextInt(yMax));
		}
		return points;
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		int dotThickness = 6;
		if(points !=null)
		{
			for (var p : points)
			{
				g.setColor(p.getColor()) ;
				g.fillOval(p.getScreenX(), p.getScreenY(), dotThickness, dotThickness);
			}
		}
		if(hull != null && hull.size() >= 2)
		{
			var hull = new ArrayList<>(this.hull);
			for (int i = 0; i < hull.size()-1; i++)
			{
				g.setStroke(new BasicStroke(5));
				Point p1 = hull.get(i);
				Point p2 = hull.get(i+1);
				g.setColor(Color.BLACK);
				g.drawLine(p1.getScreenX()+dotThickness/2,p1.getScreenY()+dotThickness/2,p2.getScreenX()+dotThickness/2,p2.getScreenY()+dotThickness/2);
			}
		}
	}

	public static void main(String[] args)
	{
		new GrahamScan();
	}
}