package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import src.Polygon;
import src.utils.Point;

/**
 * Tests for the {@link Polygon} class.
 */
public class PolygonTest {

    @Test
    public void testPolygonConstruction() {
        Polygon polygon = createTestPolygon();

        assertEquals("TestPolygon", polygon.getName());
        assertEquals(createTestPoints(), polygon.getPoints());
    }

    @Test
    public void testAddPoints() {
        Polygon polygon = createTestPolygon();

        Point p1 = new Point(6, 0);
        Point p2 = new Point(3, 2);
        polygon.addPoint(p1);
        polygon.addPoint(p2);

        // Grab another instance of the same base list and add the same points
        // to it.
        List<Point> pointList = createTestPoints();
        pointList.add(p1);
        pointList.add(p2);

        assertEquals(pointList, polygon.getPoints());
    }

    @Test
    public void testDeletePoints() {
        Polygon polygon = createTestPolygon();
        polygon.removeLastPoint();

        // Grab another instance of the same base list and remove the last point
        // from it.
        List<Point> pointList = createTestPoints();
        pointList.remove(pointList.size() - 1);

        assertEquals(pointList, polygon.getPoints());
    }

    @Test
    public void testRedoPoints() {
        Polygon polygon = createTestPolygon();
        List<Point> pointList = createTestPoints();

        // Make sure that removing and redoing a point gets you back to the
        // start.
        polygon.removeLastPoint();
        polygon.redoPoint();
        assertEquals(pointList, polygon.getPoints());

        // Make sure that redoing a point when there is nothing to redo is a
        // NOP.
        polygon.redoPoint();
        assertEquals(pointList, polygon.getPoints());

        // Make sure that redo only re-adds one item.
        polygon.removeLastPoint();
        polygon.removeLastPoint();
        polygon.redoPoint();
        pointList.remove(pointList.size() - 1);
        assertEquals(pointList, polygon.getPoints());
    }

    @Test
    public void testAbleToRedo() {
        Polygon polygon = createTestPolygon();

        assertFalse(polygon.canRedo());

        polygon.removeLastPoint();
        assertTrue(polygon.canRedo());

        polygon.redoPoint();
        assertFalse(polygon.canRedo());
    }

    /**
     * Creates a polygon for use in JUnit tests.
     */
    private static Polygon createTestPolygon() {
        String name = "TestPolygon";
        List<Point> points = createTestPoints();

        return new Polygon(name, points);
    }

    /**
     * Creates a list of {@link Point}s for use in JUnit tests.
     * 
     * @return
     */
    private static List<Point> createTestPoints() {
        Point pt1 = new Point(0, 0);
        Point pt2 = new Point(0, 5);
        Point pt3 = new Point(5, 5);
        Point pt4 = new Point(5, 0);

        List<Point> points = new ArrayList<Point>();
        points.add(pt1);
        points.add(pt2);
        points.add(pt3);
        points.add(pt4);

        return points;
    }
}
