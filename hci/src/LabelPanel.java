package src;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import src.utils.Point;

public class LabelPanel extends JPanel implements ListSelectionListener {
    // JFrame is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final JButton addButton;
    private final JButton editButton;
    private final JButton loadButton;
    private final JButton deleteButton;
    private final ToolboxPanel toolBox;
    private final ImagePanel imagePanel;

    private final JList list;
    private final JPanel labelListPane;
    private final DefaultListModel listModel;
    private final HashMap<String, Polygon> polygons;

    public LabelPanel(ToolboxPanel toolBox, ImagePanel imagePanel) {
        super(new BorderLayout());

        this.toolBox = toolBox;
        this.imagePanel = imagePanel;

        listModel = new DefaultListModel();
        polygons = new HashMap<String, Polygon>();

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        addButton = new JButton(new ImageIcon("hci/icons/add.png"));
        addButton.addActionListener(new AddListener());
        addButton.setEnabled(true);

        editButton = new JButton(new ImageIcon("hci/icons/edit.png"));
        editButton.addActionListener(new EditListener());
        editButton.setEnabled(false);
        
        loadButton = new JButton("Load button");
        loadButton.setEnabled(true);
        
        deleteButton = new JButton(new ImageIcon("hci/icons/delete.png"));
        deleteButton.addActionListener(new DeleteListener());
        deleteButton.setEnabled(false);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(editButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(deleteButton);
       
              
        JPanel noLabelsPane = new JPanel();
        noLabelsPane.add(new JLabel("<html>You don't seem to have any labels currently. <br /> Add or load some labels?</html>"));
        noLabelsPane.add(loadButton);
        
        labelListPane = new JPanel(new CardLayout());
        labelListPane.add(noLabelsPane, "NOLABELS");
        labelListPane.add(listScrollPane, "HAVELABELS");
                
        add(new JLabel("Current Labels"), BorderLayout.NORTH);
        
       
        add(labelListPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
        
        CardLayout cl = (CardLayout)(labelListPane.getLayout());
        cl.show(labelListPane, "NOLABELS");
        
        
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    /**
     * Clears the list of labels.
     */
    public void clearLabels() {
        listModel.clear();
        polygons.clear();

        deleteButton.setEnabled(false);
    }

    /**
     * Returns a list of each label, encoded in the following format:
     * 
     * {@literal label_name:x1,y1:x2,y2:}
     */
    public List<String> getLabels() {
        List<String> labels = new ArrayList<String>();

        for (Entry<String, Polygon> entry : polygons.entrySet()) {
            Polygon polygon = entry.getValue();
            StringBuilder sb = new StringBuilder();

            sb.append(polygon.getName());
            sb.append(":");
            for (Point point : polygon.getPoints()) {
                sb.append(point.getX());
                sb.append(",");
                sb.append(point.getY());
                sb.append(":");
            }

            labels.add(sb.toString());
        }

        return labels;
    }

    /**
     * Listener class for adding a new polygon to the image.
     */
    class AddListener implements ActionListener, DocumentListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Once a label is created, it can be edited via the tool box.
            toolBox.setVisible(true);
            addButton.setEnabled(false);
            imagePanel.setEditingPolygon(true);
            
            toolBox.addComponentListener(new ComponentListener() {
            	@Override
                public void componentHidden(ComponentEvent e) {
                    
            		// This checks that it's not already been hidden (to avoid naming multiple times)
            		if (!imagePanel.currentlyEditingPolygon()) {
            			return;
            		}	

            		imagePanel.setEditingPolygon(false);
                    
                    String name = null;
                    boolean hasName = false;

                    while (!hasName) {
                        JFrame frame = new JFrame();
                        String message = "Label Name";
                        name = JOptionPane.showInputDialog(frame, message);

                        if (name == null) {
                        	addButton.setEnabled(true);
                            return;
                        }

                        name = name.trim();

                        if (listModel.contains(name)) {
                            JOptionPane.showMessageDialog(new JFrame(), "That name is already in use.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (name.isEmpty()) {
                            JOptionPane.showMessageDialog(new JFrame(), "Blank names are not allowed.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            hasName = true;
                        }
                    }

                    Polygon newPolygon = new Polygon(name, toolBox.getLastPolygon().getPoints());

                    listModel.addElement(name);
                    polygons.put(name, newPolygon);

                    addButton.setEnabled(true);
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    CardLayout cl = (CardLayout)(labelListPane.getLayout());
                    cl.show(labelListPane, "HAVELABELS");
                	
                }
                
                public void componentResized(ComponentEvent e) {
                }

				@Override
				public void componentMoved(ComponentEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void componentShown(ComponentEvent arg0) {
					imagePanel.setEditingPolygon(true);
					
				}
            });

        }

        @Override
        public void insertUpdate(DocumentEvent e) {
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    }

    /**
     * Listener class for deleting a selected polygon from the image.
     */
    class DeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();

            if (index == -1) {
            	 JOptionPane.showMessageDialog(new JFrame(), "No label selected",
                         "Error", JOptionPane.ERROR_MESSAGE);
            	 return;
            }
            
            polygons.remove(listModel.get(index));
            listModel.remove(index);

            if (listModel.getSize() == 0) {
            	editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                CardLayout cl = (CardLayout)(labelListPane.getLayout());
                cl.show(labelListPane, "NOLABELS");
            }

            // Make sure that the selected index is still within the list range.
            list.setSelectedIndex(Math.min(index, listModel.getSize() - 1));
            imagePanel.polygonsList = new ArrayList<Polygon>(polygons.values());
            imagePanel.repaint();
        }
    }
    
    class EditListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            int index = list.getSelectedIndex();
            if (index == -1) {
           	 JOptionPane.showMessageDialog(new JFrame(), "No label selected",
                        "Error", JOptionPane.ERROR_MESSAGE);
           	 return;
           }
            
            String name = null;
            String currentName = listModel.get(index).toString();

            JFrame frame = new JFrame();
            String message = "New Label Name:";
            name = JOptionPane.showInputDialog(frame, message, currentName);

            if (name == null) {
                return;
            }

            name = name.trim();

            if (listModel.contains(name)) {
                JOptionPane.showMessageDialog(new JFrame(), "That name is already in use. The name was not changed.",
                        "Error", JOptionPane.ERROR_MESSAGE);

            } else if (name.isEmpty()) {
                JOptionPane.showMessageDialog(new JFrame(), "Blank names are not allowed. The name was not changed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
            	listModel.remove(index);
            	listModel.add(index, name);
            	
            	Polygon selectedPolygon = polygons.remove(currentName);
            	polygons.put(name, selectedPolygon);
            }
        }
    }
}
