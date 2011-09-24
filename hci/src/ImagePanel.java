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


/**
 * Handles the editing of the image.
 * 
 * @author Michal
 */
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
	public void paint(Graphics g) {
		super.paint(g);
		
		// Draw the image.
		ShowImage();
		
		// Draw on all the completed polygons.
		for(ArrayList<Point> polygon : polygonsList) {
			drawPolygon(polygon);
			finishPolygon(polygon);
		}
		
		// Draw on the current polygon.
		drawPolygon(currentPolygon);
	}

	/**
	 * Draws the image that is being edited.
	 */
	public void ShowImage() {
		Graphics g = this.getGraphics();
		
		if (image != null) {
			g.drawImage(image, 0, 0, null);
		}
	}
	
	/**
	 * Draws an unfinished polygon (i.e. with no line between the last and first vertices).
	 * 
	 * @param polygon the polygon to be drawn
	 */
	public void drawPolygon(ArrayList<Point> polygon) {
		Graphics2D g = (Graphics2D)this.getGraphics();
		g.setColor(Color.GREEN);
		for(int i = 0; i < polygon.size(); i++) {
			Point currentVertex = polygon.get(i);
			if (i != 0) {
				Point prevVertex = polygon.get(i - 1);
				g.drawLine(prevVertex.getX(), prevVertex.getY(), currentVertex.getX(), currentVertex.getY());
			}
			g.fillOval(currentVertex.getX() - 5, currentVertex.getY() - 5, 10, 10);
		}
	}
	
	/**
	 * Draws the last stroke of a polygon (the line between the last and first vertices).
	 * 
	 * @param polygon the polygon to draw the final stroke for
	 */
	public void finishPolygon(ArrayList<Point> polygon) {
		// A polygon with less than 3 vertices is just a line or point and needs no finishing.
		if (polygon.size() >= 3) {
			Point firstVertex = polygon.get(0);
			Point lastVertex = polygon.get(polygon.size() - 1);
		
			Graphics2D g = (Graphics2D)this.getGraphics();
			g.setColor(Color.GREEN);
			g.drawLine(firstVertex.getX(), firstVertex.getY(), lastVertex.getX(), lastVertex.getY());
		}
	}
	
	/**
	 * Finishes editing the current polygon and starts on a new one.
	 */
	public void addNewPolygon() {
		//finish the current polygon if any
		if (currentPolygon != null ) {
			finishPolygon(currentPolygon);
			polygonsList.add(currentPolygon);
		}
		
		currentPolygon = new ArrayList<Point>();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		if (x > image.getWidth() || y > image.getHeight()) {
			return;
		}
		
		Graphics2D g = (Graphics2D) this.getGraphics();
		
		// Left click adds a vertex to the current polygon.
		if (e.getButton() == MouseEvent.BUTTON1) {
			g.setColor(Color.GREEN);
			if (currentPolygon.size() != 0) {
				Point lastVertex = currentPolygon.get(currentPolygon.size() - 1);
				g.drawLine(lastVertex.getX(), lastVertex.getY(), x, y);
			}
			g.fillOval(x-5,y-5,10,10);
			
			currentPolygon.add(new Point(x,y));
			System.out.println(x + " " + y);
		} 
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
