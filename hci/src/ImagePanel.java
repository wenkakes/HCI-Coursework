package src;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import src.utils.*;

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

public class ImagePanel extends JPanel implements MouseListener {
	// JFrame is serializable, so we need some ID to avoid compiler warnings.
	private static final long serialVersionUID = 1L;

	// Image that is being worked on.
	BufferedImage image = null;

	// List of the current polygons vertices.
	ArrayList<Point> currentPolygon = null;

	// List of all completed polygons for the current image.
	ArrayList<ArrayList<Point>> polygonsList = null;
	
	public ImagePanel() {
		currentPolygon = new ArrayList<Point>();
		polygonsList = new ArrayList<ArrayList<Point>>();

		this.setVisible(true);

		Dimension panelSize = new Dimension(800, 600);
		this.setSize(panelSize);
		this.setMinimumSize(panelSize);
		this.setPreferredSize(panelSize);
		this.setMaximumSize(panelSize);
		
		addMouseListener(this);
	}
	
	public ImagePanel(String imageName) {
		this();
		
		try {
			image = ImageIO.read(new File(imageName));
			if (image != null && image.getWidth() > 800 || image.getHeight() > 600) {
				int newWidth = image.getWidth() > 800 ? 800 : (image.getWidth() * 600)/image.getHeight();
				int newHeight = image.getHeight() > 600 ? 600 : (image.getHeight() * 800)/image.getWidth();
				System.out.println("SCALING TO " + newWidth + "x" + newHeight );
				Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
				image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
				image.getGraphics().drawImage(scaledImage, 0, 0, this);
			}
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
		
		for (ArrayList<Point> polygon : polygonsList) {
			drawPolygon(polygon, graphics2D);
			finishPolygon(polygon, graphics2D);
		}
		
		drawPolygon(currentPolygon, graphics2D);
	}

	/**
	 * Finishes editing the current polygon and starts on a new one.
	 */
	public void addNewPolygon() {
		if (currentPolygon != null ) {
			polygonsList.add(currentPolygon);
		}
		currentPolygon = new ArrayList<Point>();
		
		repaint();
	}
	
	/**
	 * Draws an unfinished polygon (i.e. with no line between the last and first vertices).
	 * 
	 * @param polygon the polygon to be drawn
	 * @param graphics2d 
	 */
	public void drawPolygon(ArrayList<Point> polygon, Graphics2D graphics2d) {
		graphics2d.setColor(Color.GREEN);
		for(int i = 0; i < polygon.size(); i++) {
			Point currentVertex = polygon.get(i);
			if (i != 0) {
				Point prevVertex = polygon.get(i - 1);
				graphics2d.drawLine(prevVertex.getX(), prevVertex.getY(), currentVertex.getX(), currentVertex.getY());
			}
			graphics2d.fillOval(currentVertex.getX() - 5, currentVertex.getY() - 5, 10, 10);
		}
	}

	/**
	 * Draws the last stroke of a polygon (the line between the last and first vertices).
	 * 
	 * @param polygon the polygon to draw the final stroke for
	 * @param graphics2d 
	 */
	public void finishPolygon(ArrayList<Point> polygon, Graphics2D graphics2d) {
		// A polygon with less than 3 vertices is just a line or point and needs no finishing.
		if (polygon.size() >= 3) {
			Point firstVertex = polygon.get(0);
			Point lastVertex = polygon.get(polygon.size() - 1);

			graphics2d.setColor(Color.GREEN);
			graphics2d.drawLine(firstVertex.getX(), firstVertex.getY(), lastVertex.getX(), lastVertex.getY());
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
			currentPolygon.add(new Point(x,y));
			repaint();
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
