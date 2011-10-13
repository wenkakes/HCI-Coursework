package src.nonui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import src.ui.ImagePanelView;
import src.utils.Point;
import src.utils.Polygon;

public class ImageController {
    // The farthest distance, in pixels, that the user can click from a point
    // and still 'select' it.
    private static final double EDITING_THRESHOLD_DISTANCE = 5.0;

    private final AppController appController;
    private ImagePanelView imagePanel;

    // Used when adding/editing points.
    Polygon currentPolygon = new Polygon();
    Polygon editedPolygon = new Polygon();
    private Point currentPoint = null;

    public ImageController(AppController appController) {
        this.appController = appController;
    }

    public void setPanel(ImagePanelView imagePanel) {
        this.imagePanel = imagePanel;
    }

    public List<List<Point>> getCompletedPolygonsPoints() {
        Map<String, Polygon> completedPolygons = appController.getCompletedPolygons();
        List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
        for (Polygon polygon : completedPolygons.values()) {
            points.add(new ArrayList<Point>(polygon.getPoints()));
        }
        return points;
    }

    public List<List<Point>> getSelectedPolygonsPoints() {
        List<Polygon> selectedPolygons = getSelectedPolygons();
        List<List<Point>> points = new ArrayList<List<Point>>(selectedPolygons.size());
        for (Polygon selectedPolygon : selectedPolygons) {
            points.add(selectedPolygon.getPoints());
        }
        return points;
    }

    public List<Point> getCurrentPolygonPoints() {
        return currentPolygon.getPoints();
    }

    public List<Point> getEditedPolygonPoints() {
        if (appController.getApplicationState() == ApplicationState.EDITING_POLYGON) {
            return editedPolygon.getPoints();
        }

        return null;
    }

    /**
     * Called when the image is clicked on.
     * 
     * @param x the x-coordinate of the mouse click
     * @param y the y-coordinate of the mouse click
     * @param doubleClick whether or not the user is double clicking
     */
    public void imageMouseClick(int x, int y, boolean doubleClick) {
        switch (appController.getApplicationState()) {
            case DEFAULT:
                if (doubleClick) {
                    // Double clicking does nothing in the default state.
                    return;
                }
                imagePanel.repaint();
                // TODO: Enter explicit editing at this point.

                break;
            case ADDING_POLYGON:
                if (doubleClick) {
                    finishedAddingPolygon();
                } else {
                    currentPolygon.addPoint(new Point(x, y));
                    imagePanel.repaint();
                }
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing logic.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the mouse is pressed over the image (but not released
     * immediately, which generates a click instead).
     * 
     * @param x the x coordinate where the mouse was pressed
     * @param y the y coordinate where the mouse was pressed
     */
    public void imageMousePress(int x, int y) {
        switch (appController.getApplicationState()) {
            case DEFAULT:
                selectClosestPoint(x, y);
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                // Implement explicit editing logic.
                selectClosestPoint(x, y);
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the mouse is released (from a press/drag, not a click) over
     * the image.
     */
    public void imageMouseReleased() {
        switch (appController.getApplicationState()) {
            case DEFAULT:
                currentPoint = null;
                currentPolygon = new Polygon();
                imagePanel.repaint();
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                // Implement logic for explicit editing.
                currentPoint = null;
                currentPolygon = new Polygon();
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    public void imageMouseDrag(int x, int y) {
        switch (appController.getApplicationState()) {
            case DEFAULT:
                // Do nothing
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                if (currentPoint != null) {
                    Point newPoint = new Point(x, y);
                    if (currentPolygon.replacePoint(currentPoint, newPoint)) {
                        currentPoint = newPoint;
                    }
                    editedPolygon = currentPolygon;
                    imagePanel.repaint();
                }
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    private List<Polygon> getSelectedPolygons() {
        Map<String, Polygon> completedPolygons = appController.getCompletedPolygons();
        List<String> selectedNames = appController.getSelectedNames();
        List<Polygon> selectedPolygons = new ArrayList<Polygon>(selectedNames.size());

        for (String name : selectedNames) {
            selectedPolygons.add(completedPolygons.get(name));
        }
        return selectedPolygons;
    }

    /**
     * Called when the user is finished adding the current polygon, either by
     * clicking on the starting point, double-clicking, or clicking the "Done"
     * button on the toolbox.
     */
    public void finishedAddingPolygon() {
        if (appController.getApplicationState() != ApplicationState.ADDING_POLYGON) {
            return;
        }

        JFrame appFrame = appController.getAppFrame();

        if (currentPolygon.getPoints().size() < 3) {
            JOptionPane.showMessageDialog(appFrame,
                    "A polygon must have 3 or more vertices.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = "";
        boolean hasName = false;
        while (!hasName) {
            String message = "Label Name";
            name = JOptionPane.showInputDialog(appFrame, message, name);

            // TODO: Should this totally cancel, or only cancel the "done"?
            // Occurs if the user hits the cancel option.
            if (name == null) {
                currentPolygon = new Polygon();
                imagePanel.repaint();

                return;
            }

            name = name.trim();
            if (appController.getCompletedPolygons().containsKey(name)) {
                JOptionPane.showMessageDialog(appFrame, "That name is already in use.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else if (name.isEmpty()) {
                JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else if (!name.matches("[a-zA-Z0-9]+")) {
                JOptionPane.showMessageDialog(appFrame,
                        "Only alphanumeric characters are allowed in label names.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                hasName = true;
            }
        }

        currentPolygon.setName(name);
        appController.getCompletedPolygons().put(name, currentPolygon);
        currentPolygon = new Polygon();

        appController.finishedAddingPolygon(name);

        imagePanel.repaint();
    }

    /**
     * Selects the closest point to a given target point.
     * 
     * @param x the x coordinate of the target
     * @param y the y coordinate of the target
     */
    private void selectClosestPoint(int x, int y) {
        Point targetPoint = new Point(x, y);
        Point closestPoint = null;
        Polygon closestPolygon = null;

        double smallestDistance = -1;

        for (Polygon polygon : appController.getCompletedPolygons().values()) {
            for (Point point : polygon.getPoints()) {
                double distanceToTarget = targetPoint.distanceFrom(point);

                if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                    if (isSelected(polygon.getName())) {
                        smallestDistance = distanceToTarget;
                        closestPoint = point;
                        closestPolygon = polygon;
                    }
                }

            }
        }

        if (smallestDistance >= 0 && smallestDistance < EDITING_THRESHOLD_DISTANCE) {
            appController.setApplicationState(ApplicationState.EDITING_POLYGON);
            currentPoint = closestPoint;
            currentPolygon = closestPolygon;
        } else {
            appController.setApplicationState(ApplicationState.DEFAULT);
        }
    }

    private boolean isSelected(String name) {
        List<Polygon> polygons = getSelectedPolygons();
        for (Polygon selected : polygons) {
            if (selected.getName() == name) {
                return true;
            }
        }
        return false;
    }

    public void undo() {
        currentPolygon.removeLastPoint();
        imagePanel.repaint();
    }

    public void redo() {
        currentPolygon.redoPoint();
        imagePanel.repaint();
    }

    public Polygon getEditedPolygon() {
        return editedPolygon;
    }

    public void cancel() {
        currentPolygon = new Polygon();
        imagePanel.repaint();
    }
}
