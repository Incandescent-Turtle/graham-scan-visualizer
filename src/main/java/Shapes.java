import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Shapes
{
	/**
	 *	returns an array of the Points that are inside the given shape
	 * @param pointsToFit	the array you want to filter
	 * @param shape			the shape the Points must lie within
	 * @return				a filtered array containing only the Points within the shape
	 */
	protected static Point[] fitToShape(Point[] pointsToFit, Area shape)
	{
		var list = Arrays.stream(pointsToFit).filter(p -> shape.contains(p.x, p.y)).toList();
		return list.toArray(new Point[list.size()]);
	}

	/**
	 * a function to create a heart
	 * @param x	x coord of top left
	 * @param y	y coord of top left
	 * @param width	width of heart
	 * @param height height of heart
	 * @return	an Area representing a heart shape
	 */
	protected static Area heart(int x, int y, int width, int height)
	{
		int ovalHeight = height/2;
		int ovalWidth = width/2 + width/15;

		int[] triangleX = {
				x + width/30,
				x + width - width/30,
				x+width/2};
		int[] triangleY = {
				y + height - ovalHeight + ovalHeight/4,
				y + height - ovalHeight + ovalHeight/4,
				y};
		Polygon triangle = new Polygon(triangleX, triangleY, 3);
		Ellipse2D oval = new Ellipse2D.Float(
				x,
				y + height/2,
				ovalWidth,
				ovalHeight);

		Ellipse2D oval2 = new Ellipse2D.Float(
				x + width - ovalWidth,
				y + height/2,
				ovalWidth,
				ovalHeight);
		Area a = new Area();
		a.add(new Area(oval));
		a.add(new Area(oval2));
		a.add(new Area(triangle));
		return a;
	}

	/**
	 * a function to create a triangle with a randomly placed top point
	 * @param rand		the random object to use
	 * @param x			top left x coord
	 * @param y			top left y coord
	 * @param width		width of triangle
	 * @param height	height of triangle
	 * @return			an Area representing the triangle
	 */
	protected static Area triangle(Random rand, int x, int y, int width, int height)
	{
		Polygon shape = new Polygon();
		shape.addPoint(x,y);
		shape.addPoint(width+x,y);
		shape.addPoint(rand.nextInt(width+1)+x, height+y);
		return new Area(shape);
	}

	/**
	 * a function to create a random polygon
	 * @param rand		Random object to use
	 * @param x			x coord of top left
	 * @param y			y coord of top left
	 * @param width		max width of shape
	 * @param height	max height of shape
	 * @param minPoints the minimum amount of vertices
	 * @param maxPoints the maximum amount of vertices
	 * @return			returns a random shape within the specified bounds
	 */
	protected static Area randomShape(Random rand, int x, int y, int width, int height, int minPoints, int maxPoints)
	{
		Polygon shape = new Polygon();
		int n = rand.nextInt(maxPoints-minPoints)+minPoints;
		for (int i = 0; i < n; i++)
		{
			shape.addPoint(rand.nextInt(width)+x, rand.nextInt(height)+y);
		}
		return new Area(shape);
	}
}