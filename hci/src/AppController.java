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

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import src.utils.Point;

/**
 * Main controller class for the application.
 */
public class AppController {

    // The core Swing components that make up the application.
    private final JFrame appFrame = new JFrame("Image Labeller");
    private final ImagePanelView imagePanel = new ImagePanelView(this);
    private final LabelPanelView labelPanel = new LabelPanelView(this);
    private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(this);

    // The current polygon.
    Polygon currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));

    // All completed polygons for the current image.
    private final HashMap<String, Polygon> polygons = new HashMap<String, Polygon>();

    // Whether or not we are editing a polygon.
    private boolean editingPolygon = false;

    public AppController(String imageName) {
        setLookAndFeel();

        // Set up the main application frame.
        appFrame.setLayout(new BorderLayout());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TODO: What does the sidepanel do?
        JPanel sidePanel = new JPanel();
        GridLayout sidePanelLayout = new GridLayout(2, 1);
        sidePanelLayout.setVgap(20);
        sidePanel.setLayout(sidePanelLayout);

        sidePanel.add(labelPanel);
        sidePanel.add(toolboxPanel);

        // TODO: What does the container panel do?
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout());
        containerPanel.add(imagePanel);
        containerPanel.add(sidePanel);

        toolboxPanel.setVisible(false);

        // TODO: Rename
        // TODO: Reimplement open/save
        JPanel somePanel = new JPanel();
        somePanel.add(new JButton("Open"));
        somePanel.add(new JButton("Save"));

        appFrame.add(somePanel, BorderLayout.NORTH);
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

    private void setLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore - just use the default theme.
        }
    }

    // These methods deal with the toolbox options.
    public void finishEditingPolygon() {
        // This checks that it's not already been hidden (to avoid
        // naming multiple times)
        if (!editingPolygon) {
            return;
        }
        editingPolygon = false;

        String name = null;
        boolean hasName = false;

        while (!hasName) {
            String message = "Label Name";
            name = JOptionPane.showInputDialog(appFrame, message);

            // 'Cancel'
            if (name == null) {
                currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));
                labelPanel.setAddButtonEnabled(true);
                imagePanel.repaint();
                toolboxPanel.setVisible(false);
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

        labelPanel.addLabel(name);

        polygons.put(name, currentPolygon);
        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));

        imagePanel.repaint();

        labelPanel.setAddButtonEnabled(true);
        labelPanel.setEditButtonEnabled(true);
        labelPanel.setDeleteButtonEnabled(true);
        labelPanel.showLabelList();

        toolboxPanel.setVisible(false);
    }

    public void undoLastVertex() {
        currentPolygon.removeLastPoint();
        imagePanel.repaint();
    }

    public void redoLastVertex() {
        currentPolygon.redoPoint();
        imagePanel.repaint();
    }

    public void cancelDrawingVertex() {
        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));
        editingPolygon = false;
        toolboxPanel.setVisible(false);
        labelPanel.setAddButtonEnabled(true);
        imagePanel.repaint();
    }

    // These methods deal with the image panel events.
    public void imageClick(int x, int y) {
        if (editingPolygon) {
            currentPolygon.addPoint(new Point(x, y));
            imagePanel.repaint();
        }
    }

    public List<Polygon> getCompletedPolygons() {
        // TODO: Copy individual polygons?
        return new ArrayList<Polygon>(polygons.values());
    }

    public Polygon getCurrentPolygon() {
        // TODO: Copy the polygon?
        return currentPolygon;
    }

    public void addPolygonButtonPressed() {
        labelPanel.setAddButtonEnabled(false);
        toolboxPanel.setVisible(true);

        editingPolygon = true;
    }

    public void renamePolygon(String oldName, String newName) {
        Polygon polygon = polygons.get(oldName);
        polygons.remove(oldName);
        if (polygon != null) {
            polygon.setName(newName);
            polygons.put(newName, polygon);
        }
    }

    public void removePolygon(String name) {
        polygons.remove(name);
        imagePanel.repaint();
    }
}
