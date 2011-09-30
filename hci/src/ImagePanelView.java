package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import src.utils.Point;

/**
 * View for the image panel. Handles the rendering of the image and the overlaid
 * polygons, and interactions with it.
 */
public class ImagePanelView extends JPanel implements MouseListener, MouseMotionListener {
    // JPanel is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final AppController controller;

    // Image that is being worked on.
    private BufferedImage image = null;

    public ImagePanelView(AppController appController) {
        this.controller = appController;

        setVisible(true);

        Dimension panelSize = new Dimension(800, 600);
        setSize(panelSize);
        setMinimumSize(panelSize);
        setPreferredSize(panelSize);
        setMaximumSize(panelSize);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // TODO: This should probably not call into the controller. At any rate, it
    // needs tidied.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            g.drawString("LOL EDIT AN IMAGE NOOB", this.getWidth() / 2, this.getHeight() / 2);
        } else {
            g.drawImage(image, 0, 0, null);

            Graphics2D graphics2D = (Graphics2D) g;

            List<List<Point>> completedPolygonsPoints = controller.getCompletedPolygonsPoints();
            for (List<Point> points : completedPolygonsPoints) {
                drawPolygon(points, graphics2D);
                finishPolygon(points, graphics2D);
            }

            List<Point> currentPolygonPoints = controller.getCurrentPolygonPoints();
            if (currentPolygonPoints != null) {
                drawPolygon(currentPolygonPoints, graphics2D);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (image == null || e.getButton() != MouseEvent.BUTTON1 || x > image.getWidth()
                || y > image.getHeight()) {
            return;
        }

        controller.imageMouseClick(x, y, e.getClickCount() == 2);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (image == null || e.getButton() != MouseEvent.BUTTON1 || x > image.getWidth()
                || y > image.getHeight()) {
            return;
        }

        controller.imageMousePress(x, y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        controller.imageMouseReleased();
    }

    /**
     * Sets the image that is to be rendered in the panel.
     * 
     * @param image the image to draw
     */
    public void setImage(BufferedImage image) {
        this.image = image;

        // Scale if necessary
        // TODO: Rewrite this.
        if (image != null && image.getWidth() > 800 || image.getHeight() > 600) {
            int newWidth = image.getWidth() > 800 ? 800 : (image.getWidth() * 600)
                    / image.getHeight();
            int newHeight = image.getHeight() > 600 ? 600 : (image.getHeight() * 800)
                    / image.getWidth();
            System.out.println("SCALING TO " + newWidth + "x" + newHeight);
            Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
            image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(scaledImage, 0, 0, this);
        }

        repaint();
    }

    /**
     * Draws an unfinished polygon (i.e. with no line between the last and first
     * vertices).
     * 
     * @param points the points of the polygon to be drawn
     * @param graphics2d
     */
    private static void drawPolygon(List<Point> points, Graphics2D graphics2d) {
        graphics2d.setColor(Color.GREEN);
        for (int i = 0; i < points.size(); i++) {
            Point currentVertex = points.get(i);
            if (i != 0) {
                Point prevVertex = points.get(i - 1);
                graphics2d.drawLine(prevVertex.getX(), prevVertex.getY(), currentVertex.getX(),
                        currentVertex.getY());
            }
            graphics2d.fillOval(currentVertex.getX() - 5, currentVertex.getY() - 5, 10, 10);
        }
    }

    /**
     * Draws the last stroke of a polygon (the line between the last and first
     * vertices).
     * 
     * @param points the points of the polygon to draw the final stroke for
     * @param graphics2d
     */
    private static void finishPolygon(List<Point> points, Graphics2D graphics2d) {
        // A polygon with less than 3 vertices is just a line or point and needs
        // no finishing.
        if (points.size() >= 3) {
            Point firstVertex = points.get(0);
            Point lastVertex = points.get(points.size() - 1);

            graphics2d.setColor(Color.GREEN);
            graphics2d.drawLine(firstVertex.getX(), firstVertex.getY(), lastVertex.getX(),
                    lastVertex.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        controller.imageMouseDrag(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
