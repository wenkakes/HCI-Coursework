package src;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class LabelPanelView extends JPanel {
    // JFrame is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final AppController controller;

    private final DefaultListModel listModel;
    private final JList list;

    private JButton addButton;
    private JButton editButton;
    private JButton loadButton;
    private JButton deleteButton;

    private JPanel labelListPane;

    public LabelPanelView(AppController appController) {
        super(new BorderLayout());

        this.controller = appController;

        listModel = new DefaultListModel();

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        addButton = new JButton(new ImageIcon("hci/icons/add.png"));
        addButton.addActionListener(new AddListener());
        addButton.setEnabled(true);

        editButton = new JButton(new ImageIcon("hci/icons/edit.png"));
        editButton.addActionListener(new EditListener());
        editButton.setEnabled(false);

        deleteButton = new JButton(new ImageIcon("hci/icons/delete.png"));
        deleteButton.addActionListener(new DeleteListener());
        deleteButton.setEnabled(false);

        loadButton = new JButton("Load button");
        // loadButton.addActionListener(new LoadListener());
        loadButton.setEnabled(true);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(editButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(deleteButton);

        JPanel noLabelsPane = new JPanel();
        noLabelsPane
                .add(new JLabel(
                        "<html>You don't seem to have any labels currently. <br /> Add or load some labels?</html>"));
        noLabelsPane.add(loadButton);

        labelListPane = new JPanel(new CardLayout());
        labelListPane.add(noLabelsPane, "NOLABELS");
        labelListPane.add(listScrollPane, "HAVELABELS");

        add(new JLabel("Current Labels"), BorderLayout.NORTH);

        add(labelListPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);

        CardLayout cl = (CardLayout) (labelListPane.getLayout());
        cl.show(labelListPane, "NOLABELS");
    }

    public void setAddButtonEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
    }

    /**
     * Listener class for adding a new polygon to the image.
     */
    class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.addPolygonButtonPressed();
        }
    }

    public void showLabelList() {
        CardLayout cl = (CardLayout) (labelListPane.getLayout());
        cl.show(labelListPane, "HAVELABELS");
    }

    public void addLabel(String name) {
        listModel.addElement(name);
    }

    public void setEditButtonEnabled(boolean enabled) {
        editButton.setEnabled(enabled);
    }

    public void setDeleteButtonEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }

    class EditListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            if (index == -1) {
                JOptionPane.showMessageDialog(new JFrame(), "No label selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(new JFrame(),
                        "That name is already in use. The name was not changed.", "Error",
                        JOptionPane.ERROR_MESSAGE);

            } else if (name.isEmpty()) {
                JOptionPane.showMessageDialog(new JFrame(),
                        "Blank names are not allowed. The name was not changed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                listModel.remove(index);
                listModel.add(index, name);

                controller.renamePolygon(currentName, name);
            }
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
                JOptionPane.showMessageDialog(new JFrame(), "No label selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            controller.removePolygon((String) listModel.get(index));
            listModel.remove(index);

            if (listModel.getSize() == 0) {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                CardLayout cl = (CardLayout) (labelListPane.getLayout());
                cl.show(labelListPane, "NOLABELS");
            }

            // Make sure that the selected index is still within the list range.
            list.setSelectedIndex(Math.min(index, listModel.getSize() - 1));
        }
    }

    public void setLoadButtonEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
    }
}
