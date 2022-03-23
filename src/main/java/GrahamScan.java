import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GrahamScan extends JPanel
{
	private static final int HEIGHT = 500, WIDTH = 500;
	private Point[] points;
	private ArrayList<Point> hull;

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
		var b = new JButton("run");
		b.addActionListener(e->run());
		run();
		add(b);
		new Thread(() -> {
			while (true)
			{
				run();
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void run()
	{
		points = randomPoints(100, WIDTH,HEIGHT);
		Arrays.sort(points, Comparator.comparingDouble(Point::getY));
		Arrays.sort(points, Comparator.comparingDouble(p -> polarAngle(points[0], p)));
		Stack<Point> pointStack = new Stack<>();
		pointStack.add(points[0]);
		pointStack.add(points[1]);

		for (int i = 2; i < points.length; i++) {

			Point head = points[i];
			Point middle = pointStack.pop();
			Point tail = pointStack.peek();

			int turn = turnType(tail, middle, head);

			switch(turn)
			{
			case 1:
				pointStack.push(middle);
				pointStack.push(head);
				break;
			case -1:
				i--;
				break;
			case 0:
				pointStack.push(head);
				break;
			}
		}

		pointStack.push(points[0]);

		hull =  new ArrayList<>(pointStack);
		repaint();
	}

	private static double polarAngle(Point lowest, Point p2)
	{
		return java.lang.Math.atan2((p2.y-lowest.y), (p2.x-lowest.x));
	}

	private static int turnType(Point p1, Point p2, Point p3)
	{
		int crossProduct = ((p2.x - p1.x) * (p3.y - p1.y)) - ((p2.y - p1.y) * (p3.x - p1.x));

		if(crossProduct > 0) return 1;	//	counter-clockwise
		if(crossProduct < 0) return -1;	//	clockwise
		return 0;	//	co-linear
	}

	private static Point[] randomPoints(int size, int xMax, int yMax)
	{
		Point[] points = new Point[size];
		var rand = new Random();
		for (int i = 0; i < size; i++)
		{
			points[i] = new Point(rand.nextInt(xMax), rand.nextInt(yMax));
		}
		return points;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		for (var p : points)
		{
			g.setColor(p == points[0] ? Color.RED : Color. BLACK) ;
			g.fillOval(p.x+(getWidth()-WIDTH)/2, HEIGHT-p.y+(getHeight()-HEIGHT)/2, 5, 5);
		}
		for (int i = 0; i < hull.size()-1; i++)
		{
			Point p1 = hull.get(i);
			Point p2 = hull.get(i+1);
			g.drawLine(p1.x+(getWidth()-WIDTH)/2,HEIGHT-p1.y+(getHeight()-HEIGHT)/2,p2.x+(getWidth()-WIDTH)/2,HEIGHT-p2.y+(getHeight()-HEIGHT)/2);
		}
	}

	public static void main(String[] args)
	{
		new GrahamScan();
	}
}