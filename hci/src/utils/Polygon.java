package src.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a Polygon, either in-progress or finished. Supports
 * undo/redo state.
 */
public class Polygon {
    private String name;
    private List<Point> points;
    private int pointIndex;
    private List<String> tags;

    public Polygon() {
        this("");
    }

    public Polygon(String name) {
        this(name, new ArrayList<Point>());
    }

    public Polygon(String name, List<Point> inputPoints) {
        this.name = name;

        // Defensive-copy the input points.
        points = new ArrayList<Point>();
        for (Point point : inputPoints) {
            points.add(point);
        }
        pointIndex = points.size() - 1;

        tags = new ArrayList<String>();
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
     * Returns a list of the current points making up the polygon. The list is a
     * copy of internally stored data, and so modifying it will not affect the
     * parent polygon.
     * 
     * @return a list of the current points in the polygon.
     */
    public List<Point> getPoints() {
        List<Point> subList = new ArrayList<Point>(pointIndex + 1);

        // No need to deep-copy as Points are immutable.
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

    public boolean replacePoint(Point oldPoint, Point newPoint) {
        if (!points.contains(oldPoint) || points.indexOf(oldPoint) > pointIndex) {
            return false;
        }

        return Collections.replaceAll(points, oldPoint, newPoint);
    }

    /**
     * Removes the last point that was added to the polygon. The removed polygon
     * is stored so that it can be re-added by calling {@link #redoPoint()}. If
     * there are no points to remove, does nothing.
     */
    public void removeLastPoint() {
       // if (pointIndex > 0) {
            pointIndex--;
       // }
    }

    /**
     * Re-adds the last deleted point from the polygon. If there are no deleted
     * points to re-add, does nothing.
     */
    public void redoPoint() {
        if (canRedo()) {
            pointIndex++;
        }
    }

    /**
     * Checks whether or not there are any deleted points that can be re-added
     * to the polygon.
     */
    public boolean canRedo() {
        return points.size() - pointIndex > 1;
    }

    @Override
    public String toString() {
        return "Polygon [name=" + name + ", points=" + getPoints() + "]";
    }

    /**
     * Adds a tag to the polygon. If the tag is already attached to the polygon,
     * no action is taken.
     * 
     * @param tag the tag to add
     */
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Return a list of the tags attached to the polygon.
     */
    public List<String> getTags() {
        return new ArrayList<String>(tags);
    }

    /**
     * Removes a tag from the polygon.
     * 
     * @param string the name of the tag to remove
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

	public void addPointAt(Point targetPoint, int i) {      
        points.add(i, targetPoint);
		pointIndex++;

	}
}
