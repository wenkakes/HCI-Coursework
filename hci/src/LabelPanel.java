package src;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
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
    private final JButton deleteButton;
    private final JPanel toolBox;

    private final JList list;
    private final DefaultListModel listModel;
    private final HashMap<String, Polygon> polygons;

    public LabelPanel(JPanel toolBox) {
        super(new BorderLayout());

        this.toolBox = toolBox;

        listModel = new DefaultListModel();
        polygons = new HashMap<String, Polygon>();

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        addButton = new JButton("Add New Polygon");
        addButton.addActionListener(new AddListener());
        addButton.setEnabled(true);

        deleteButton = new JButton("Delete Polygon");
        deleteButton.addActionListener(new DeleteListener());
        deleteButton.setEnabled(false);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(deleteButton);

        add(new JLabel("Current Labels"), BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
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

            // TODO: Should not be able to hit new polygon when editing one.

            String name = null;
            boolean hasName = false;

            while (!hasName) {
                JFrame frame = new JFrame();
                String message = "Label Name";
                name = JOptionPane.showInputDialog(frame, message);

                if (name == null) {
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

            Polygon newPolygon = new Polygon(name);

            listModel.addElement(name);
            polygons.put(name, newPolygon);

            deleteButton.setEnabled(true);
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

            polygons.remove(listModel.get(index));
            listModel.remove(index);

            if (listModel.getSize() == 0) {
                deleteButton.setEnabled(false);
            }

            // Make sure that the selected index is still within the list range.
            list.setSelectedIndex(Math.min(index, listModel.getSize() - 1));
        }
    }
}
