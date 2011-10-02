package src;

import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import src.utils.LabelIO;
import src.utils.LabelIO.LabelParseException;
import src.utils.Point;
import src.utils.Polygon;

/**
 * Main controller class for the application.
 */
public class AppController {
    // The farthest distance, in pixels, that the user can click from a point
    // and still 'select' it.
    private static final double EDITING_THRESHOLD_DISTANCE = 5.0;

    // The core Swing components that make up the application.
    private final JFrame appFrame = new JFrame("Image Labeller");
    private final MenuBarView menuBar = new MenuBarView(this);
    private final ImagePanelView imagePanel = new ImagePanelView(this);
    private final LabelPanelView labelPanel = new LabelPanelView(appFrame, this);
    private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(appFrame, this);

    Polygon currentPolygon = new Polygon();
    private Map<String, Polygon> completedPolygons = new HashMap<String, Polygon>();
    private Point currentPoint = null; // Used when moving points.

    // The application state.
    private ApplicationState applicationState = ApplicationState.DEFAULT;

    public AppController(String imageName) {
        appFrame.setLayout(new FlowLayout());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        appFrame.add(imagePanel);
        appFrame.add(labelPanel);
        appFrame.setJMenuBar(menuBar);

        // Load the default image and set it.
        try {
            BufferedImage image = ImageIO.read(new File(imageName));
            imagePanel.setImage(image);
        } catch (IOException e) {
            labelPanel.setAddButtonEnabled(false);
            labelPanel.setLoadButtonEnabled(false);
        }

        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setResizable(false);
    }

    /**
     * Called when the image is clicked on.
     * 
     * @param x the x-coordinate of the mouse click
     * @param y the y-coordinate of the mouse click
     * @param doubleClick whether or not the user is double clicking
     */
    public void imageMouseClick(int x, int y, boolean doubleClick) {
        switch (applicationState) {
            case DEFAULT:
                if (doubleClick) {
                    // Double clicking does nothing in the default state.
                    return;
                }

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
     * Called when the mouse is dragged over the image.
     * 
     * @param x the x coordinate that the mouse is now at
     * @param y the y coordinate that the mouse is now at
     */
    public void imageMouseDrag(int x, int y) {
        switch (applicationState) {
            case DEFAULT:
                // If the user is dragging a point, move it.
                if (currentPoint != null) {
                    Point newPoint = new Point(x, y);
                    if (currentPolygon.replacePoint(currentPoint, newPoint)) {
                        currentPoint = newPoint;
                    }
                    imagePanel.repaint();
                }
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                // TODO: Implement dragging for explicit editing.
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
        switch (applicationState) {
            case DEFAULT:
                selectClosestPoint(x, y);
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                // Implement explicit editing logic.
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the mouse is released (from a press/drag, not a click) over
     * the image.
     */
    public void imageMouseReleased() {
        switch (applicationState) {
            case DEFAULT:
                currentPoint = null;
                currentPolygon = new Polygon();
                break;
            case ADDING_POLYGON:
                // Do nothing.
                break;
            case EDITING_POLYGON:
                // Implement logic for explicit editing.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Returns a list of the points of each completed polygon.
     */
    public List<List<Point>> getCompletedPolygonsPoints() {
        List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
        for (Polygon polygon : completedPolygons.values()) {
            points.add(new ArrayList<Point>(polygon.getPoints()));
        }
        return points;
    }

    /**
     * Returns a list of the points in the current Polygon.
     */
    public List<Point> getCurrentPolygonPoints() {
        return currentPolygon.getPoints();
    }

    /**
     * Starts adding a new polygon.
     */
    public void startAddingNewPolygon() {
        applicationState = ApplicationState.ADDING_POLYGON;

        labelPanel.setAddButtonEnabled(false);

        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX(), mouseLocation.getY() + 20);
        toolboxPanel.setLocation(mouseLocation);
        toolboxPanel.setVisible(true);
    }

    /**
     * Renames a polygon.
     * 
     * @param oldName the old name for the polygon
     * @param newName the new name for the polygon
     */
    public void renamePolygon(String oldName, String newName) {
        Polygon polygon = completedPolygons.remove(oldName);
        if (polygon != null) {
            polygon.setName(newName);
            completedPolygons.put(newName, polygon);
        }
    }

    /**
     * Removes a polygon from the image.
     * 
     * @param name the name of the polygon to remove
     */
    public void removePolygon(String name) {
        completedPolygons.remove(name);
        imagePanel.repaint();
    }

    /**
     * Saves the current list of polygons to a file.
     * 
     * @param file the file to save to
     */
    public void saveLabels(File file) {
        try {
            LabelIO.writeLabels(file, new ArrayList<Polygon>(completedPolygons.values()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads in a new list of polygons from a file.
     * 
     * @param file the file to load from
     */
    public void loadLabels() {

        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(appFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File loadFile = chooser.getSelectedFile();

            try {
                completedPolygons = LabelIO.readLabels(loadFile);
                labelPanel.clear();
                for (String name : completedPolygons.keySet()) {
                    labelPanel.addLabel(name);
                }
                imagePanel.repaint();
            } catch (LabelParseException e) {
                JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Opens the image from a file.
     * 
     * @param file the file to open the image from
     */
    public void openImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);

            cancelAddingPolygon();
            completedPolygons.clear();

            imagePanel.setImage(image);
            labelPanel.clear();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(appFrame, "Unable to open image.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called when the "Done" button on the toolbox is clicked.
     */
    public void toolboxDoneButtonClicked() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                finishedAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    // TODO: Rename to "undo", and extend to cover states other than
    // ADDING_POLYGON.
    /**
     * Called when the user wishes to undo their last move when editing a
     * polygon.
     */
    public void undoLastVertex() {
        currentPolygon.removeLastPoint();
        imagePanel.repaint();
    }

    // TODO: Rename to "redo", and extend to cover states other than
    // ADDING_POLYGON.
    /**
     * Called when the user wishes to redo their last move when editing a
     * polygon.
     */
    public void redoLastVertex() {
        currentPolygon.redoPoint();
        imagePanel.repaint();
    }

    /**
     * Called when the "Cancel" button on the toolbar is clicked.
     */
    public void toolboxCancelButtonClicked() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                cancelAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the toolbox window is closed.
     */
    public void toolboxWindowClosed() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                cancelAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Deletes all of the polygons.
     */
    public void deleteAllPolygons() {
        labelPanel.deleteAllPolygons();
        imagePanel.repaint();
    }

    /**
     * Deletes the currently selected polygons.
     */
    public void deleteSelectedPolygons() {
        labelPanel.deleteSelectedPolygons();
        imagePanel.repaint();
    }

    /**
     * Called when the user is finished adding the current polygon, either by
     * clicking on the starting point, double-clicking, or clicking the "Done"
     * button on the toolbox.
     */
    private void finishedAddingPolygon() {
        if (applicationState != ApplicationState.ADDING_POLYGON) {
            return;
        }

        if (currentPolygon.getPoints().size() < 3) {
            JOptionPane.showMessageDialog(appFrame, "A polygon must have 3 or more vertices.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        applicationState = ApplicationState.DEFAULT;
        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

        String name = null;
        boolean hasName = false;
        while (!hasName) {
            String message = "Label Name";
            name = JOptionPane.showInputDialog(appFrame, message);

            // TODO: Should this totally cancel, or only cancel the "done"?
            // Occurs if the user hits the cancel option.
            if (name == null) {
                currentPolygon = new Polygon();
                imagePanel.repaint();

                return;
            }

            name = name.trim();
            if (completedPolygons.containsKey(name)) {
                JOptionPane.showMessageDialog(appFrame, "That name is already in use.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else if (name.isEmpty()) {
                JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                hasName = true;
            }
            //imagePanel.setImage(null);

        }

        currentPolygon.setName(name);
        completedPolygons.put(name, currentPolygon);
        currentPolygon = new Polygon();

        labelPanel.addLabel(name);

        imagePanel.repaint();
    }

    /**
     * Called when the user cancels the polygon that they are currently adding.
     */
    private void cancelAddingPolygon() {
        applicationState = ApplicationState.DEFAULT;
        currentPolygon = new Polygon();

        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

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

        for (Polygon polygon : completedPolygons.values()) {
            for (Point point : polygon.getPoints()) {
                double distanceToTarget = targetPoint.distanceFrom(point);

                if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                    smallestDistance = distanceToTarget;
                    closestPoint = point;
                    closestPolygon = polygon;
                }
            }
        }

        if (smallestDistance >= 0 && smallestDistance < EDITING_THRESHOLD_DISTANCE) {
            currentPoint = closestPoint;
            currentPolygon = closestPolygon;
        }
    }

    public void closeImage() {
        imagePanel.setImage(null);
        labelPanel.clear();

        labelPanel.setAddButtonEnabled(false);
        labelPanel.setLoadButtonEnabled(false);
    }
}
