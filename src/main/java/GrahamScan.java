import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.*;

public class GrahamScan extends JPanel
{
	protected static final int HEIGHT = 500, WIDTH = 500;
	private static int delay = 10, stepSize = 1;
	private static boolean showDots = true, showLines = true;
	private final JFrame frame;

	/**
	 *		Type to represent the type of turn when determining concave rotations
	 */
	enum Turn { COUNTER_CLOCK_WISE, CLOCK_WISE, CO_LINEAR }
	enum Shape { RANDOM, HEART, SQUARE, CIRCLE, TRIANGLE }

	private Point[] points;
	private Stack<Point> hull;
	private Area shape;
	private Shape shapeType;
	private boolean running = false, loop = false;
	private int amount = 100;

	public GrahamScan()
	{
		frame = new JFrame("Graham Scan Visualizer");
		var dim = new Dimension(WIDTH,HEIGHT);
		frame.setPreferredSize(dim);
		frame.setMinimumSize(dim);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		shapeType = Shape.RANDOM;
		new UI().createUI();
		genNewShape();
		randomizeDots();
		frame.setVisible(true);
		repaint();
	}

	/**
	 * 	randomizes the dot positions
	 */
	private void randomizeDots()
	{
		points = randomPoints(amount, WIDTH,HEIGHT);
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

		int steps = 0;
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
			//	skips delay if haven't reached step threshold
			if(steps >= stepSize)
			{
				steps = 0;
				try
				{
					Thread.sleep(delay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			steps++;
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
		return Shapes.fitToShape(points, shape);
	}

	/**
	 * 	creates a new shape depending on the shape type selected
	 */
	private void genNewShape()
	{
		Random rand = new Random();
		shape = switch(shapeType)
		{
			//	random shape with 3-102 vertices
			case RANDOM -> Shapes.randomShape(rand, 0,0, WIDTH, HEIGHT, 3, 100);
			case HEART -> Shapes.heart(0,0, WIDTH, HEIGHT);
			case CIRCLE -> new Area(new Ellipse2D.Float(0,0, WIDTH, HEIGHT));
			case SQUARE -> new Area(new Rectangle(0,0,WIDTH,HEIGHT));
			case TRIANGLE -> Shapes.triangle(rand, 0,0,WIDTH,HEIGHT);
		};
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;

		//	drawing the dots
		int dotThickness = 6;
		if(showDots && points !=null)
		{
			for (var p : points)
			{
				g.setColor(p.getColor());
				g.fillOval(p.getScreenX(), p.getScreenY(), dotThickness, dotThickness);
			}
		}

		//	drawing lines in the hull
		if(showLines && hull != null && hull.size() >= 2)
		{
			var hull = new ArrayList<>(this.hull);
			for (int i = 0; i < hull.size()-1; i++)
			{
				g.setStroke(new BasicStroke(dotThickness));
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

	class UI
	{
		//	for all components that can't be used while algo is running
		private final List<JComponent> disableOnRun = new ArrayList<>();

		private JLabel shapeListLabel;
		private JComboBox<Shape> shapeList;

		private JButton shuffleButton, runButton;

		private JLabel 		delayLabel, 	stepSizeLabel, 		amountLabel;
		private JSpinner 	delaySpinner, 	stepSizeSpinner, 	amountSpinner;

		private JCheckBox showDotsCheckBox, showLinesCheckBox, loopCheckBox;

		/**
		 * 	creates all UI components and puts them on the panel
		 */
		protected void createUI()
		{
			JPanel bar = new JPanel();
			bar.setLayout(new WrapLayout());
			createShapeComboBox();
			createShuffleButton();
			createRunButton();
			createDelaySpinner();
			createStepSizeSpinner();
			createAmountSpinner();
			createShowDotsCheckBox();
			createShowLinesCheckBox();
			createLoopCheckBox();

			bar.add(shapeListLabel);
			bar.add(shapeList);

			bar.add(shuffleButton);

			bar.add(runButton);

			bar.add(delayLabel);
			bar.add(delaySpinner);

			bar.add(stepSizeLabel);
			bar.add(stepSizeSpinner);

			bar.add(amountLabel);
			bar.add(amountSpinner);

			bar.add(showDotsCheckBox);

			bar.add(showLinesCheckBox);

			bar.add(loopCheckBox);

			frame.add(bar, BorderLayout.PAGE_START);

			disableOnRun.add(shapeList);
			disableOnRun.add(runButton);
			disableOnRun.add(shuffleButton);
		}

		private void createShapeComboBox()
		{
			shapeListLabel = new JLabel("Shape Type: ");
			shapeList = new JComboBox<Shape>();
			for (var s : Shape.values())
			{
				shapeList.addItem(s);
			}
			shapeList.setSelectedItem(shapeType);
			shapeList.addActionListener(e -> {
				shapeType = (Shape) shapeList.getSelectedItem();
				genNewShape();
				randomizeDots();
				repaint();
			});
		}

		private void createShuffleButton()
		{
			shuffleButton = new JButton("Shuffle");
			shuffleButton.addActionListener(e -> {
				if(!running)
				{
					new Thread(()-> {
						genNewShape();
						randomizeDots();
						repaint();
					}).start();
				}
			});
		}

		private void createRunButton()
		{
			runButton = new JButton("Run");
			runButton.addActionListener(e -> new Thread(() -> {
				//	whether this has been run more than once
				boolean firstDone = false;
				//	loops if the loop field is true
				do {
					running = true;
					//	disables all components
					disableOnRun.forEach(c -> c.setEnabled(false));
					//	generates a new shape every loop
					if(firstDone)
					{
						genNewShape();
						randomizeDots();
					}
					run();
					running = false;
					//	re-enables components after algo is done
					disableOnRun.forEach(c -> c.setEnabled(true));
					firstDone = true;
				} while (loop);
			}).start());
		}

		private void createDelaySpinner()
		{
			delaySpinner = new JSpinner(new SpinnerNumberModel(delay, 0, 1_000_000, 1));
			delaySpinner.addChangeListener(e -> delay = (int) delaySpinner.getValue());
			delayLabel = new JLabel("Delay: ");
		}

		private void createStepSizeSpinner()
		{
			stepSizeSpinner = new JSpinner(new SpinnerNumberModel(stepSize, 1, 1_000_000, 1));
			stepSizeSpinner.addChangeListener(e -> stepSize = (int) stepSizeSpinner.getValue());
			stepSizeLabel = new JLabel("Step Size: ");
		}

		private void createAmountSpinner()
		{
			amountSpinner = new JSpinner(new SpinnerNumberModel(amount, 0, 100_000, 1));
			amountSpinner.addChangeListener(e -> {
				amount = (int) amountSpinner.getValue();
				//	if algo is running, dot amount updates the next time it's ran
				if(!running) randomizeDots();
				repaint();
			});
			amountLabel = new JLabel("Dot Amount: ");
		}

		private void createShowDotsCheckBox()
		{
			showDotsCheckBox = new JCheckBox("Show Dots");
			showDotsCheckBox.addChangeListener(e -> {
				showDots = showDotsCheckBox.isSelected();
				repaint();
			});
			showDotsCheckBox.setSelected(true);
		}

		private void createShowLinesCheckBox()
		{
			showLinesCheckBox = new JCheckBox("Show Lines");
			showLinesCheckBox.addChangeListener(e -> {
				showLines = showLinesCheckBox.isSelected();
				repaint();
			});
			showLinesCheckBox.setSelected(true);
		}

		private void createLoopCheckBox()
		{
			loopCheckBox = new JCheckBox("Loop Algorithm");
			loopCheckBox.addChangeListener(e -> loop = loopCheckBox.isSelected());
			loopCheckBox.setSelected(false);
		}
	}
}