package src;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import src.utils.Point;

@SuppressWarnings("serial")
public class LabelPanel extends JPanel
                      implements ListSelectionListener {

	private JList list;
	private DefaultListModel listModel;
	private HashMap<String, Polygon> polygons;
    private JButton addButton;
    private JButton deleteButton;

    public LabelPanel() {
        super(new BorderLayout());
        
        
        listModel = new DefaultListModel();
        polygons = new HashMap<String, Polygon>();
        
        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        // Create the button to add a new polygon
        addButton = new JButton("Add New Polygon");
        AddListener addListener = new AddListener(addButton);
        addButton.setActionCommand("Add new Polygon");
        addButton.addActionListener(addListener);
        addButton.setEnabled(true);
        
        // Create the button to delete a polygon
        deleteButton = new JButton("Delete Polygon");
        deleteButton.setActionCommand("Delete Polygon");
        deleteButton.addActionListener(new DeleteListener());
        deleteButton.setEnabled(false);
        
        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(deleteButton);
        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    
    // This listener is for adding a new polygon
    class AddListener implements ActionListener, DocumentListener {
        private boolean alreadyEnabled = true;
        private JButton button;

        public AddListener(JButton button) {
            this.button = button;
        }

        // Required by ActionListener.
        public void actionPerformed(ActionEvent e) {

        	String name = null;
        	boolean hasName = false;

        	while (!hasName) {
        		
	            JFrame frame = new JFrame();
	            String message = "Label Name";
	            name = JOptionPane.showInputDialog(frame, message);
	           
	            if (name == null) {
	              return;
	              
	            } else if (alreadyInList(name)) {
	                JOptionPane.showMessageDialog(new JFrame(), "That name is already in use.", 
	                		"Error", JOptionPane.ERROR_MESSAGE);
	                
	            } else {
	            	hasName = true;
	            }
        	}

            Polygon newPolygon = new Polygon(name, createTestPoints());
            
            // ListModel.addElement(newPolygon);
            listModel.addElement(name);
            polygons.put(name, newPolygon);
            deleteButton.setEnabled(true);

        }

        //This method tests for string equality. You could certainly
        //get more sophisticated about the algorithm.  For example,
        //you might want to ignore white space and capitalization.
        protected boolean alreadyInList(String name) {
            return listModel.contains(name);
        }

        //Required by DocumentListener.
        public void insertUpdate(DocumentEvent e) {
        }

        //Required by DocumentListener.
        public void removeUpdate(DocumentEvent e) {
        }

        //Required by DocumentListener.
        public void changedUpdate(DocumentEvent e) {
        }
    }
    

    // This listener is for deleting a polygon
    class DeleteListener implements ActionListener {
    	
        public void actionPerformed(ActionEvent e) {
            
        	int index = list.getSelectedIndex();
            polygons.remove(listModel.get(index));
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0) { 
            	deleteButton.setEnabled(false);
            	
            } else { //Select an index.
                if (index == listModel.getSize()) {
                    //removed item in last position
                    index--;
                }
            }
        }
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ListDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new LabelPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private static List<Point> createTestPoints() {
		Point pt1 = new Point(0, 0);
		Point pt2 = new Point(0, 5);
		Point pt3 = new Point(5, 5);
		Point pt4 = new Point(5, 0);
		
		List<Point> points = new ArrayList<Point>();
		points.add(pt1);
		points.add(pt2);
		points.add(pt3);
		points.add(pt4);
		
		return points;
	}
	
}