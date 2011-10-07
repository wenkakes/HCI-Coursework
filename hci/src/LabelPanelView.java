package src;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import src.utils.Polygon;

/**
 * View for the polygon label panel, handling the refreshing of the label list
 * and interactions with it and the panel buttons.
 */
public class LabelPanelView extends JPanel {
    // JPanel is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final JFrame appFrame;
    private final AppController controller;

    // The label list and its backing model.
    private JPanel labelListPane;
    private final DefaultListModel listModel;
    private final JList list;

    public JPopupMenu rightClickMenu;

    private JButton addButton;
    private JButton editButton;
    private JButton loadButton;
    private JButton deleteButton;

    public LabelPanelView(JFrame frame, AppController appController) {
        // super(new BorderLayout());
        Border labelBorder = BorderFactory.createTitledBorder("Labels");
        this.setBorder(labelBorder);
        this.setLayout(new BorderLayout());

        // this.setLocation(x, y);
        this.appFrame = frame;
        this.controller = appController;

        rightClickMenu = new JPopupMenu();
        JMenuItem renameLabel = new JMenuItem("Rename");
        renameLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renameSelectedPolygon();
            }
        });
        JMenuItem deleteLabel = new JMenuItem("Delete");
        deleteLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedPolygons();
            }
        });
        rightClickMenu.add(renameLabel);
        rightClickMenu.add(deleteLabel);

        // Set up the backing data structures for the label list.
        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        list.addMouseListener(new LabelListMouseListener());
        JScrollPane listScrollPane = new JScrollPane(list);

        // Create the buttons.
        addButton = new JButton(new ImageIcon("hci/icons/small/add.png"));
        addButton.addActionListener(new AddListener());
        addButton.setEnabled(true);

        editButton = new JButton(new ImageIcon("hci/icons/small/edit.png"));
        editButton.addActionListener(new EditListener());
        editButton.setEnabled(false);

        deleteButton = new JButton(new ImageIcon("hci/icons/small/delete.png"));
        deleteButton.addActionListener(new DeleteListener());
        deleteButton.setEnabled(false);

        loadButton = new JButton("Load Labels");
        loadButton.addActionListener(new LoadListener());
        loadButton.setEnabled(true);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(editButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(deleteButton);

        // Create the panel to be shown if there are labels.
        JPanel noLabelsPane = new JPanel();
        noLabelsPane.add(new JLabel(
                "<html>You don't seem to have any labels currently. <br /> Add or load some "
                        + "labels?</html>"));
        noLabelsPane.add(loadButton);

        labelListPane = new JPanel(new CardLayout());
        labelListPane.add(noLabelsPane, "NOLABELS");
        labelListPane.add(listScrollPane, "HAVELABELS");

        add(labelListPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);

        CardLayout cl = (CardLayout) (labelListPane.getLayout());
        cl.show(labelListPane, "NOLABELS");
        this.setVisible(true);
    }

    /**
     * Add a label to the list of labels.
     * 
     * @param name the name of the label to add
     */
    public void addLabel(String name) {
        listModel.addElement(name);

        // Once a label has been added, renaming/deleting is possible.
        setEditButtonEnabled(true);
        setDeleteButtonEnabled(true);
        showLabelList();
    }

    /**
     * Clears the list of labels.
     */
    public void clear() {
        listModel.clear();

        setAddButtonEnabled(true);
        setEditButtonEnabled(false);
        setDeleteButtonEnabled(false);
        setLoadButtonEnabled(true);

        hideLabelList();
    }

    /**
     * Sets whether or not the add label button should be enabled.
     * 
     * @param enabled whether to enable or disable the add label button
     */
    public void setAddButtonEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
    }

    /**
     * Sets whether or not the edit label button should be enabled.
     * 
     * @param enabled whether to enable or disable the edit label button
     */
    public void setEditButtonEnabled(boolean enabled) {
        editButton.setEnabled(enabled);
    }

    /**
     * Sets whether or not the delete label button should be enabled.
     * 
     * @param enabled whether to enable or disable the delete label button
     */
    public void setDeleteButtonEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }

    /**
     * Sets whether or not the load labels button should be enabled.
     * 
     * @param enabled whether to enable or disable the load labels button
     */
    public void setLoadButtonEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
    }

    /**
     * Allows the user to rename the currently selected polygon.
     */
    public void renameSelectedPolygon() {
        int index = list.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(appFrame, "No label selected", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = null;
        String currentName = listModel.get(index).toString();

        String message = "New Label Name:";
        name = JOptionPane.showInputDialog(appFrame, message, currentName);

        // If the user hits cancel, we do nothing.
        if (name == null) {
            return;
        }

        name = name.trim();
        if (listModel.contains(name)) {
            JOptionPane.showMessageDialog(appFrame,
                    "That name is already in use. The name was not changed.", "Error",
                    JOptionPane.ERROR_MESSAGE);

        } else if (name.isEmpty()) {
            JOptionPane.showMessageDialog(appFrame,
                    "Blank names are not allowed. The name was not changed.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            listModel.remove(index);
            listModel.add(index, name);

            controller.renamePolygon(currentName, name);
        }
    }

    /**
     * Deletes the currently selected polygons.
     */
    public void deleteSelectedPolygons() {
        int[] indices = list.getSelectedIndices();

        if (indices.length == 0) {
            JOptionPane.showMessageDialog(appFrame, "No label(s) selected", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = indices.length - 1; i >= 0; i--) {
            controller.removePolygon((String) listModel.get(indices[i]));
            listModel.remove(indices[i]);
        }

        if (listModel.getSize() == 0) {
            setEditButtonEnabled(false);
            setDeleteButtonEnabled(false);
            hideLabelList();
        }

        // Make sure that the selected index is still within the list range.
        list.setSelectedIndex(Math.min(indices[0], listModel.getSize() - 1));
        editButton.setEnabled(list.getSelectedIndices().length == 1);
        controller.highlightSelected(getSelectedNames());

    }

    /**
     * Deletes all of the polygons.
     */
    public void deleteAllPolygons() {
        for (int i = 0; i < listModel.getSize(); i++) {
            controller.removePolygon((String) listModel.get(i));
        }
        listModel.clear();
        controller.highlightSelected(getSelectedNames());

        setEditButtonEnabled(false);
        setDeleteButtonEnabled(false);
        hideLabelList();

    }

    /**
     * Show the list of labels.
     */
    private void showLabelList() {
        CardLayout cl = (CardLayout) (labelListPane.getLayout());
        cl.show(labelListPane, "HAVELABELS");
    }

    /**
     * Hides the list of labels.
     */
    private void hideLabelList() {
        CardLayout cl = (CardLayout) (labelListPane.getLayout());
        cl.show(labelListPane, "NOLABELS");
    }

    /**
     * Listener class for adding a new polygon to the image.
     */
    private class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.startAddingNewPolygon();
        }
    }

    /**
     * Listener class for editing the name of a label.
     */
    private class EditListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            renameSelectedPolygon();
        }
    }

    /**
     * Listener class for deleting a selected label/polygon from the image.
     */
    private class DeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteSelectedPolygons();
        }
    }

    /**
     * Listener class for loading labels from a file.
     */
    private class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.loadLabels();
        }
    }

    private class LabelListMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            // Only allow renaming of single items at once.
            editButton.setEnabled(list.getSelectedIndices().length == 1);

            controller.highlightSelected(getSelectedNames());

            if (e.getButton() == MouseEvent.BUTTON3 && list.getSelectedIndices().length == 1) {
                rightClickMenu.show(LabelPanelView.this, e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }
    }

    public List<Polygon> getSelectedPolygons() {
        int[] indices = list.getSelectedIndices();
        List<Polygon> selectedPolygons = new ArrayList<Polygon>(indices.length);
        Polygon selectedPolygon;
        for (int i = indices.length - 1; i >= 0; i--) {
            selectedPolygon = controller.getPolygon(((String) listModel.get(indices[i])));
            selectedPolygons.add(selectedPolygon);
        }
        return selectedPolygons;
    }

    public List<String> getSelectedNames() {
        List<Integer> indices = new ArrayList<Integer>(list.getSelectedIndices().length);
        for (int index : list.getSelectedIndices()) {
            indices.add(index);
        }

        List<String> names = new ArrayList<String>(list.getSelectedIndices().length);
        for (int i = 0; i < listModel.getSize(); i++) {
            if (indices.contains(i)) {
                names.add((String) listModel.get(i));
            }
        }

        return names;
    }
}
