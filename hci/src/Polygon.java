package src;


import java.util.ArrayList;
import java.util.List;

import src.utils.Point;

/**
 * Class representing a Polygon, either in-progress or finished. Supports
 * undo/redo state.
 */
public class Polygon {
	
	private String name;
	private ArrayList<Point> points;
	private int pointIndex;
		
	public Polygon(String name) {
		this(name, new ArrayList<Point>());
	}
	
	public Polygon(String name, List<Point> inputPoints) {
		this.name = name;
		
		points = new ArrayList<Point>(inputPoints.size());
		for (Point point : inputPoints) {
			points.add(point);
		}
		pointIndex = points.size() - 1;
	}

	/**
	 * Returns the name of the polygon.
	 * 
	 * @return the name of the polygon
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the polygon.
	 * 
	 * @param name the new name for the polygon
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a list of the current points making up the polygon. The list is
	 * a copy of internally stored data, and so modifying it will not affect
	 * the parent polygon.
	 * 
	 * @return a list of the current points in the polygon.
	 */
	public List<Point> getPoints() {
		List<Point> subList = new ArrayList<Point>(pointIndex + 1);
		
		// No need to deep-copy as Points are immutable objects.
		for (int i = 0; i <= pointIndex; i++) {
			subList.add(points.get(i));
		}
		
		return subList;
	}

	/**
	 * Adds a point to the polygon.
	 * 
	 * @param point the new point to add to the polygon
	 */
	public void addPoint(Point point) {
		// Adding a point deletes the 'redo' cache.
		ArrayList<Point> newPoints = new ArrayList<Point>(pointIndex + 2);
		for (int i = 0; i < pointIndex + 1; i++) {
			newPoints.add(points.get(i));
		}
		
		newPoints.add(point);
		points = newPoints;
		pointIndex++;
	}

	/**
	 * Removes the last point that was added to the polygon. The removed polygon
	 * is stored so that it can be re-added by calling {@link #redoPoint()}.
	 * If there are no points to remove, does nothing.
	 */
	public void removeLastPoint() {
		if (pointIndex > 0) {
			pointIndex--;
		}
	}

	/**
	 * Re-adds the last deleted point from the polygon. If there are no deleted points
	 * to re-add, does nothing.
	 */
	public void redoPoint() {
		if (canRedo())
			pointIndex++;	
	}

	/**
	 * Checks whether or not there are any deleted points that can be re-added to the polygon.
	 */
	public boolean canRedo() {
		return points.size() - pointIndex > 1;
	}
}