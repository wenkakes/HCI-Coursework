package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import src.utils.Point;

/**
 * Handles the editing of the image.
 */
public class ImagePanel extends JPanel implements MouseListener {
    // JFrame is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    // Image that is being worked on.
    BufferedImage image = null;

    // List of the current polygons vertices.
    Polygon currentPolygon = null;

    // List of all completed polygons for the current image.
    ArrayList<Polygon> polygonsList = null;

    ToolboxPanel toolboxPanel;
    
    // Are we editing a polygon?
    boolean editingPolygon = false;
    
    public ImagePanel() {
        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));
        polygonsList = new ArrayList<Polygon>();

        this.setVisible(true);

        Dimension panelSize = new Dimension(800, 600);
        this.setSize(panelSize);
        this.setMinimumSize(panelSize);
        this.setPreferredSize(panelSize);
        this.setMaximumSize(panelSize);

        addMouseListener(this);
    }

    public ImagePanel(String imageName, ToolboxPanel toolboxPanel) {
        this();
        
        try {
            image = ImageIO.read(new File(imageName));
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
            
            this.toolboxPanel = toolboxPanel;
            
        } catch (IOException ioe) {
            // Do nothing - we just dont instantiate the window.
            // TODO(Stephen): Show an error message.
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(image, 0, 0, null);

        Graphics2D graphics2D = (Graphics2D) g;

        for (Polygon polygon : polygonsList) {
            drawPolygon(polygon, graphics2D);
            finishPolygon(polygon, graphics2D);
        }

        drawPolygon(currentPolygon, graphics2D);
    }

    /**
     * Finishes editing the current polygon and starts on a new one.
     */
    public void addNewPolygon() {
        if (currentPolygon != null) {
            polygonsList.add(currentPolygon);
            toolboxPanel.setLastPolygon(currentPolygon);
            
        }
        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));

        repaint();
    }

    /**
     * Draws an unfinished polygon (i.e. with no line between the last and first
     * vertices).
     * 
     * @param polygon the polygon to be drawn
     * @param graphics2d
     */
    public void drawPolygon(Polygon polygon, Graphics2D graphics2d) {
        
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
    public void finishPolygon(Polygon polygon, Graphics2D graphics2d) {
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

        if (x > image.getWidth() || y > image.getHeight()) {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() > 1) {
            	toolboxPanel.setVisible(false);
                addNewPolygon();
                
            } else {
            	if (editingPolygon) {
                currentPolygon.addPoint(new Point(x, y));
                repaint();
            	}
            }
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

    public void undo() {
        if (currentPolygon != null) {
            currentPolygon.removeLastPoint();
        }
        repaint();
    }

    public void redo() {
        if (currentPolygon != null) {
            currentPolygon.redoPoint();
        }
        repaint();
    }

    public void cancel() {
        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));
        repaint();
    }

    public void setImageFile(File selectedFile) {
        try {
            image = ImageIO.read(selectedFile);
            // if (image != null && image.getWidth() > 800 || image.getHeight()
            // > 600) {
            // int newWidth = image.getWidth() > 800 ? 800 : (image.getWidth() *
            // 600)
            // / image.getHeight();
            // int newHeight = image.getHeight() > 600 ? 600 :
            // (image.getHeight() * 800)
            // / image.getWidth();
            // System.out.println("SCALING TO " + newWidth + "x" + newHeight);
            // Image scaledImage = image.getScaledInstance(newWidth, newHeight,
            // Image.SCALE_FAST);
            // image = new BufferedImage(newWidth, newHeight,
            // BufferedImage.TYPE_INT_RGB);
            // image.getGraphics().drawImage(scaledImage, 0, 0, this);
            // }
        } catch (IOException ioe) {
            // Do nothing - we just dont instantiate the window.
            // TODO(Stephen): Show an error message.
        }

        currentPolygon = new Polygon(Long.toString(System.currentTimeMillis()));
        polygonsList.clear();
        repaint();
    }

	public void setEditingPolygon(boolean b) {
		editingPolygon = b;
		
	}

	public boolean currentlyEditingPolygon() {
		return editingPolygon;
	}
}
