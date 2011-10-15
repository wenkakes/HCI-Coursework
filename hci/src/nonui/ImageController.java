package src.nonui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import src.ui.ImagePanelView;
import src.utils.Point;
import src.utils.Polygon;

public class ImageController {
	
    // How far a user can click from a point and still select it (in pixels) 
    private static final double EDITING_THRESHOLD_DISTANCE = 5.0;
    
    private final AppController appController;
    private ImagePanelView imagePanel;

    // Used when adding/editing points.
    Polygon polygonInCreation = new Polygon(); 
    Polygon polygonInEditing = new Polygon();
    private Point currentPoint = null;

    public ImageController(AppController appController) {
        this.appController = appController;
    }

    /**
     * Sets the panel that this controller is for.
     * 
     * @param imagePanel the panel this controller is for
     */
    public void setPanel(ImagePanelView imagePanel) {
        this.imagePanel = imagePanel;
    }

    /**
     * Returns a list of the points of the completed polygons.
     */
    public List<List<Point>> getCompletedPolygonsPoints() {
        Map<String, Polygon> completedPolygons = appController.getCompletedPolygons();        
        List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
        for (Polygon polygon : completedPolygons.values()) {
            points.add(new ArrayList<Point>(polygon.getPoints()));
        }
        return points;
    }

    /**
     * Returns a list of the points of the currently selected polygons.
     */
    public List<List<Point>> getSelectedPolygonsPoints() {
        List<Polygon> selectedPolygons = getSelectedPolygons();
        List<List<Point>> points = new ArrayList<List<Point>>(selectedPolygons.size());
        for (Polygon selectedPolygon : selectedPolygons) {
            points.add(selectedPolygon.getPoints());
        }
        return points;
    }

    /**
     * Returns a list of the points of the polygon that is currently being created.
     */
    public List<Point> getCurrentPolygonPoints() {
    	if (polygonInCreation != null)
    		return polygonInCreation.getPoints();
    	return null;
    }

    /**
     * Returns a list of the points of the polygon that is currently being edited,
     * or null if no polygon is being edited.
     */
    public List<Point> getEditedPolygonPoints() {
        if (appController.getApplicationState() == ApplicationState.EDITING_POLYGON) {
            return polygonInEditing.getPoints();
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
    	System.out.println("State: " + appController.getApplicationState());
        switch (appController.getApplicationState()) {
            
        	case DEFAULT:
                if (doubleClick) {
                    // Double clicking does nothing in the default state.
                    return;
                }
                
            	// If an existing point is recognised as being clicked, 
            	// select it and change the state to EDITING
                selectClosestPoint(x, y);
                break;
                
            case ADDING_POLYGON:
                if (doubleClick) {
                    finishedAddingPolygon();
                } else {
                	if (checkPolygonClosed(x,y)) {
                		finishedAddingPolygon();
                	} else {
                		polygonInCreation.addPoint(new Point(x, y));
                        imagePanel.repaint();	
                	}                 
                }
                break;
                
            case EDITING_POLYGON:
            	
                // If a point is being clicked, select this polygon.              
                if (!selectClosestPoint(x, y)) {
                	addPt2OldPolygon(x,y);
                } else {
                	
                }
            	imagePanel.repaint();
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
            	// If an existing point is recognised as being clicked, 
            	// select it and change the state to EDITING
                selectClosestPoint(x, y);
                break;
                
            case ADDING_POLYGON:
                // Do nothing.
                break;
                
            case EDITING_POLYGON:
            	// If a point is being selected... select it.
                // If a point is not being selected, add a new point.
                if (!selectClosestPoint(x, y)) {
                	//System.out.println("No closest point, add new point instead.");
                	addPt2OldPolygon(x,y);
                	imagePanel.repaint();
                	
                }
                
                break;

                
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
                polygonInCreation = new Polygon();
                imagePanel.repaint();
                break;
                
            case ADDING_POLYGON:
                // Do nothing.
                break;
                
            case EDITING_POLYGON:
                // Implement logic for explicit editing.
                currentPoint = null;
                polygonInCreation = new Polygon();
                break;
                
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the user drags their mouse over the image. 
     * 
     * @param x the x coordinate they have dragged to
     * @param y the y coordinate they have dragged to
     */
    public void imageMouseDrag(int x, int y) {
        switch (appController.getApplicationState()) {
        
            case DEFAULT:
                // Do nothing
                break;
                
            case ADDING_POLYGON:
                // Do nothing.
                break;
                
            case EDITING_POLYGON:
                if (currentPoint != null && polygonInEditing != null) {
                    Point newPoint = new Point(x, y);
                    if (polygonInEditing.replacePoint(currentPoint, newPoint)) {
                    	System.out.println("Replaced a point");
                        currentPoint = newPoint;
                    }
                    imagePanel.repaint();
                }
                break;
                
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Gets a list of the currently selected polygons.
     */
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

        if (polygonInCreation.getPoints().size() < 3) {
            JOptionPane.showMessageDialog(appFrame,
                    "A label must have 3 or more vertices.", "Error", JOptionPane.ERROR_MESSAGE);
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
                polygonInCreation = new Polygon();
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

        polygonInCreation.setName(name);
        appController.getCompletedPolygons().put(name, polygonInCreation);
        polygonInCreation = new Polygon();

        appController.finishedAddingPolygon(name);

        imagePanel.repaint();
        
        if (appController.checkTipsOn()) { 
        	appController.showNewLabelTip();
        }
        
    }

    /**
     * Selects the closest point to a given target point.
     * 
     * @param x the x coordinate of the target
     * @param y the y coordinate of the target
     */
    private boolean selectClosestPoint(int x, int y) {
        Point targetPoint = new Point(x, y);
        Point closestPoint = null;
        Polygon closestPolygon = null;

        double smallestDistance = -1;

        for (Polygon polygon : appController.getCompletedPolygons().values()) {
            for (Point point : polygon.getPoints()) {
                double distanceToTarget = targetPoint.distanceFrom(point);
                if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                    if (isSelected(polygon)) {
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
            polygonInEditing = closestPolygon;
            //System.out.println("Yay, we found a polygon!");
            return true;
        } else {
            appController.setApplicationState(ApplicationState.DEFAULT);
        }
        
        return false;
    }
    
    /*
    private Polygon getClosestPolygon(int x, int y, double threshold) {
        Point targetPoint = new Point(x, y);
        Polygon closestPolygon = null;

        double smallestDistance = -1;

        for (Polygon polygon : appController.getCompletedPolygons().values()) {
            for (Point point : polygon.getPoints()) {
                double distanceToTarget = targetPoint.distanceFrom(point);
                if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                    smallestDistance = distanceToTarget;
                    closestPolygon = polygon;
                }
            }
        }
        
        if (smallestDistance > -1 && smallestDistance < threshold) {
            return closestPolygon;
        } else {
            return null;
        }
    } */

    private void addPt2OldPolygon(int x, int y) {
    	Point targetPoint = new Point(x, y);
    	
    	polygonInCreation = null;
    	
    	for (Polygon polygon : getSelectedPolygons()) { 	
    	    		
	    	List<Point> polygonPoints = polygon.getPoints();
	    	
	    	for (int i = 0; i < polygonPoints.size(); i++) {
	    		Point point1 = polygonPoints.get(i);
	    		Point point2 = polygonPoints.get((i + 1) % polygonPoints.size());
	    		
	    		// y = mx + c
	    		double m = (double)(point1.getY() - point2.getY()) / (double)(point1.getX() - point2.getX());
	    		double c = point1.getY() - (double)(m * point1.getX());
	    		
	    		// plug new point into equation
	    		double expectedY = (m * (double) x) + c; 
	    		double expectedX = ((double) y - c) / m;
	    			    		
	    		if (Math.abs(expectedY - (double) y) < EDITING_THRESHOLD_DISTANCE || 
	    				Math.abs(expectedX - (double) x) < EDITING_THRESHOLD_DISTANCE) {
	    			
	    			if (withinBoundaries(targetPoint, point1, point2) || withinBoundaries(targetPoint, point2, point1)) {
	    				polygon.addPointAt(targetPoint, ((i+1) % polygonPoints.size()));
	    				appController.setApplicationState(ApplicationState.EDITING_POLYGON);
	    				polygonInEditing = polygon;
	    				return;
	    			}
	    		}
	    		
	        }
    	}
    	
    }
    
    private boolean withinBoundaries(Point targetPoint, Point point1, Point point2) {
		if (point1.getX() >= targetPoint.getX() && targetPoint.getX() >= point2.getX()) {
			if ((point1.getY() >= targetPoint.getY() && targetPoint.getY() >= point2.getY()) ||
					(point2.getY() >= targetPoint.getY() && targetPoint.getY() >= point1.getY())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkPolygonClosed(int x, int y) {
        Point targetPoint = new Point(x, y);

        if (polygonInCreation.getPoints().isEmpty()) {
        	return false;
        }
        
        double distanceToTarget = targetPoint.distanceFrom(polygonInCreation.getPoints().get(0));

        if (distanceToTarget < EDITING_THRESHOLD_DISTANCE) {
        		return true;
        }
        
        return false;
    }

    /**
     * Returns true if a given polygon is currently selected.
     * 
     * @param polygon the polygon that may be selected
     */
    private boolean isSelected(Polygon polygon) {
        return getSelectedPolygons().contains(polygon);
    }

    /**
     * Undoes the last added vertex on the current polygon.
     */
    public void undo() {
        polygonInCreation.removeLastPoint();
        imagePanel.repaint();
    }

    /**
     * Redoes the last undone vertex on the current polygon.
     */
    public void redo() {
        polygonInCreation.redoPoint();
        imagePanel.repaint();
    }

    /**
     * Gets the polygon that is currently being edited.
     */
    public Polygon getEditedPolygon() {
        return polygonInEditing;
    }

    /**
     * Cancels the adding of the current polygon.
     */
    public void cancel() {
        polygonInCreation = new Polygon();
        imagePanel.repaint();
    }

    /**
     * Sets the image from a file.
     * 
     * @param bufferedImage the file to open the image from
     */
    public void setImage(BufferedImage image) {
        imagePanel.setImage(image);
    }
}
