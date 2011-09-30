package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import src.utils.Point;

/**
 * Tests for the {@link Point} class.
 */
public class PointTest {

    @Test
    public void testPointConstruction() {
        Point point = new Point(15, 4);
        assertEquals(15, point.getX());
        assertEquals(4, point.getY());

        point = new Point(0, 0);
        assertEquals(0, point.getX());
        assertEquals(0, point.getY());

        point = new Point(-213, -2);
        assertEquals(-213, point.getX());
        assertEquals(-2, point.getY());
    }

    @Test
    public void testEquality() {
        fail();
    }

    @Test
    public void testDistanceFrom() {
        fail();
    }
}
