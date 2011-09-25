package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import src.utils.Point;
import src.utils.Polygon;

/**
 * View for the image panel. Handles the rendering of the image and the overlaid
 * polygons.
 */
public class ImagePanelView extends JPanel implements MouseListener {
    // JFrame is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    // The controller
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
    }

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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            g.drawString("LOL EDIT AN IMAGE NOOB", this.getWidth() / 2, this.getHeight() / 2);
        } else {
            g.drawImage(image, 0, 0, null);

            Graphics2D graphics2D = (Graphics2D) g;

            List<Polygon> completedPolygons = controller.getCompletedPolygons();
            for (Polygon polygon : completedPolygons) {
                drawPolygon(polygon, graphics2D);
                finishPolygon(polygon, graphics2D);
            }

            Polygon currentPolygon = controller.getCurrentPolygon();
            if (currentPolygon != null) {
                drawPolygon(currentPolygon, graphics2D);
            }

        }
    }

    /**
     * Draws an unfinished polygon (i.e. with no line between the last and first
     * vertices).
     * 
     * @param polygon the polygon to be drawn
     * @param graphics2d
     */
    private static void drawPolygon(Polygon polygon, Graphics2D graphics2d) {

        List<Point> points = polygon.getPoints();
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
     * @param polygon the polygon to draw the final stroke for
     * @param graphics2d
     */
    private static void finishPolygon(Polygon polygon, Graphics2D graphics2d) {
        List<Point> points = polygon.getPoints();
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
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (image == null || e.getButton() != MouseEvent.BUTTON1 || x > image.getWidth()
                || y > image.getHeight()) {
            return;
        }

        if (e.getClickCount() != 2) {
            controller.imageClick(x, y);
        } else {
            controller.finishEditingPolygon();
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }
}
