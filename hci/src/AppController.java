package src;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import src.utils.LabelIO;
import src.utils.LabelIO.LabelParseException;
import src.utils.Point;
import src.utils.Polygon;

/**
 * Main controller class for the application.
 */
public class AppController {

    // The core Swing components that make up the application.
    private final JFrame appFrame = new JFrame("Image Labeller");
    private final ImagePanelView imagePanel = new ImagePanelView(this);
    private final LabelPanelView labelPanel = new LabelPanelView(this);
    private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(this);
    private final MainMenuPanelView menuPanel = new MainMenuPanelView(this);

    // The current polygon.
    Polygon currentPolygon = new Polygon();

    // All completed polygons for the current image.
    private Map<String, Polygon> polygons = new HashMap<String, Polygon>();

    // Whether or not we are editing a polygon.
    private boolean editingPolygon = false;

    public AppController(String imageName) {
        appFrame.setLayout(new BorderLayout());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel sidePanel = new JPanel();
        GridLayout sidePanelLayout = new GridLayout(2, 1);
        sidePanelLayout.setVgap(20);
        sidePanel.setLayout(sidePanelLayout);

        sidePanel.add(labelPanel);
        sidePanel.add(toolboxPanel);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout());
        containerPanel.add(imagePanel);
        containerPanel.add(sidePanel);

        toolboxPanel.setVisible(false);

        appFrame.add(menuPanel, BorderLayout.NORTH);
        appFrame.add(containerPanel, BorderLayout.CENTER);

        appFrame.pack();
        appFrame.setVisible(true);

        // Load the default image and set it.
        try {
            BufferedImage image = ImageIO.read(new File(imageName));
            imagePanel.setImage(image);
        } catch (IOException e) {
            labelPanel.setAddButtonEnabled(false);
            labelPanel.setLoadButtonEnabled(false);
        }
    }

    /**
     * Called when the user is finished editing the current polygon, either by
     * double-clicking or clicking the "Done" button on the toolbox.
     */
    public void finishEditingPolygon() {
        if (!editingPolygon) {
            return;
        }
        editingPolygon = false;

        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

        String name = null;
        boolean hasName = false;
        while (!hasName) {
            String message = "Label Name";
            name = JOptionPane.showInputDialog(appFrame, message);

            // Occurs if the user hits the cancel option.
            // TODO: Should this totally cancel, or only cancel the "done"?
            if (name == null) {
                currentPolygon = new Polygon();
                imagePanel.repaint();

                return;
            }

            name = name.trim();
            if (polygons.containsKey(name)) {
                JOptionPane.showMessageDialog(new JFrame(), "That name is already in use.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (name.isEmpty()) {
                JOptionPane.showMessageDialog(new JFrame(), "Blank names are not allowed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                hasName = true;
            }
        }

        currentPolygon.setName(name);
        polygons.put(name, currentPolygon);
        currentPolygon = new Polygon();

        labelPanel.addLabel(name);

        imagePanel.repaint();
    }

    /**
     * Called when the user wishes to undo their last move when editing a
     * polygon.
     */
    public void undoLastVertex() {
        currentPolygon.removeLastPoint();
        imagePanel.repaint();
    }

    /**
     * Called when the user wishes to redo their last move when editing a
     * polygon.
     */
    public void redoLastVertex() {
        currentPolygon.redoPoint();
        imagePanel.repaint();
    }

    /**
     * Called when the user cancels the polygon that they are currently drawing.
     */
    public void cancelDrawingVertex() {
        editingPolygon = false;
        currentPolygon = new Polygon();

        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

        imagePanel.repaint();
    }

    /**
     * Called when the image is clicked on.
     * 
     * @param x the x-coordinate of the mouse click
     * @param y the y-coordinate of the mouse click
     */
    public void imageClick(int x, int y) {
        if (editingPolygon) {
            currentPolygon.addPoint(new Point(x, y));
            imagePanel.repaint();
        }
    }

    /**
     * Returns a list of the points of each completed polygon.
     */
    public List<List<Point>> getCompletedPolygonsPoints() {
        List<List<Point>> points = new ArrayList<List<Point>>(polygons.size());
        for (Polygon polygon : polygons.values()) {
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
     * Start editing a new polygon.
     */
    public void startEditingNewPolygon() {
        labelPanel.setAddButtonEnabled(false);
        toolboxPanel.setVisible(true);

        editingPolygon = true;
    }

    /**
     * Renames a polygon.
     * 
     * @param oldName the old name for the polygon
     * @param newName the replacement name for the polygon
     */
    public void renamePolygon(String oldName, String newName) {
        Polygon polygon = polygons.remove(oldName);
        if (polygon != null) {
            polygon.setName(newName);
            polygons.put(newName, polygon);
        }
    }

    /**
     * Removes a polygon from the image.
     * 
     * @param name the name of the polygon to remove
     */
    public void removePolygon(String name) {
        polygons.remove(name);
        imagePanel.repaint();
    }

    /**
     * Saves the current list of polygons to a file.
     * 
     * @param file the file to save to
     */
    public void saveLabels(File file) {
        try {
            LabelIO.writeLabels(file, new ArrayList<Polygon>(polygons.values()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads in a new list of polygons from a file.
     * 
     * @param file the file to load from
     */
    public void loadLabels(File file) {
        try {
            polygons = LabelIO.readLabels(file);
            labelPanel.clear();
            for (String name : polygons.keySet()) {
                labelPanel.addLabel(name);
            }
            imagePanel.repaint();
        } catch (LabelParseException e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
