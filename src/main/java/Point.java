import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

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

	public int getScreenX()
	{
		return x+(panel.getWidth()-GrahamScan.WIDTH)/2;
	}

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