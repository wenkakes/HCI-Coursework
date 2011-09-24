package src;


import java.util.ArrayList;
import java.util.List;

import src.utils.Point;

public class Polygon {
	
	private String name;
	private ArrayList<Point> points;
	private int pointIndex;
		
	public Polygon(String name, ArrayList<Point> points) {
		this.name = name;
		this.points = points;
		pointIndex = points.size() - 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Point> getPoints() {
		return points.subList(0, pointIndex + 1);
	}

	public void addPoint(Point p) {
		
		ArrayList<Point> newPoints = new ArrayList<Point>(pointIndex + 2);
		for (int i = 0; i < pointIndex + 1; i++) {
			newPoints.add(points.get(i));
		}
		newPoints.add(p);
		pointIndex++;
		
		
		points = newPoints;
	}

	public void removeLastPoint() {
		pointIndex--;
	}

	public void redoPoint() {
		if (canRedo())
			pointIndex++;	
	}

	public boolean canRedo() {
		return points.size() - pointIndex > 1;
	}


}