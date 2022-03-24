import javax.swing.*;
import java.awt.*;

public class Point
{
	protected final int x, y;
	private Color color = Color.BLACK;
	private JPanel panel;

	public Point(JPanel panel, int x, int y)
	{
		this.panel = panel;
		this.x = x;
		this.y = y;
	}

	/**
	 * function to find distance between two points
	 * @param p	the point to find the distance between
	 * @return	the distance between this point and p
	 */
	public double dist(Point p) {
		int px = p.getX() - this.getX();
		int py = p.getY() - this.getY();
		return Math.sqrt(px * px + py * py);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	/**
	 *	function to return x coord of where this should be drawn on the screen
	 * @return	the x pos this should be drawn at
	 */
	public int getScreenX()
	{
		return x+(panel.getWidth()-GrahamScan.WIDTH)/2;
	}

	/**
	 *	function to return y coord of where this should be drawn on the screen. Inverts coordinates so y=0 is at the bottom
	 * @return	the y pos this should be drawn at
	 */
	public int getScreenY()
	{
		return GrahamScan.HEIGHT-y+(panel.getHeight()-GrahamScan.HEIGHT)/2;
	}

	public Color getColor()
	{
		return color;
	}

	public Point setColor(Color color)
	{
		this.color = color;
		return this;
	}
}