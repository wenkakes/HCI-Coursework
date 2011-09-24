package src;

import javax.swing.JPanel;

public class ToolboxPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private Polygon lastPolygon;
	
	public Polygon getLastPolygon() {
		return lastPolygon;
	}
	
	public void setLastPolygon(Polygon p) {
		lastPolygon = p;
	}
}