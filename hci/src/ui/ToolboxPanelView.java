package src.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import src.nonui.AppController;

/**
 * View for the toolbox panel.
 */
public class ToolboxPanelView extends JPanel {
    // JPanel is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private static final String ENABLED_TEXT = "<html><center>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Click on the image to add"
            + " points to the label.<br /></center></html>";
    private static final String DISABLED_TEXT = "<html><center>&nbsp;&nbsp;&nbsp;&nbsp;"
            + "Click on the Add New Label button to start adding points.<br /></center></html>";

    // The controller.
    private final AppController controller;

    private JLabel instructions;
    private JButton finishedEditingButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton cancelButton;

    public ToolboxPanelView(AppController appController) {
        this.controller = appController;

        initUI();
    }

    /**
     * Sets up the UI for the toolbox.
     */
    private void initUI() {
        Border toolboxBorder = BorderFactory.createTitledBorder("Toolbox");
        setBorder(toolboxBorder);
        setLayout(new GridLayout(2, 1));

        finishedEditingButton = new JButton(new ImageIcon("hci/icons/done.png"));
        finishedEditingButton.setMnemonic(KeyEvent.VK_N);
        finishedEditingButton.setSize(50, 20);
        finishedEditingButton.setEnabled(true);
        finishedEditingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.toolboxDoneButtonClicked();
            }
        });
        finishedEditingButton.setToolTipText("Done");

        undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.undoLastVertex();
            }
        });
        undoButton.setToolTipText("Undo");

        redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.redoLastVertex();
            }
        });
        redoButton.setToolTipText("Redo");

        cancelButton = new JButton(new ImageIcon("hci/icons/cancel.png"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelAddingLabel();
            }
        });
        cancelButton.setToolTipText("Cancel");

        instructions = new JLabel(DISABLED_TEXT);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalStrut(20));
        buttonPane.add(finishedEditingButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(undoButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(redoButton);
        buttonPane.add(Box.createHorizontalStrut(10));
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createHorizontalStrut(20));

        add(instructions);
        add(buttonPane);
        
        disableToolbox();
    }

    public void enableToolbox() {
        finishedEditingButton.setEnabled(true);
        undoButton.setEnabled(true);
        redoButton.setEnabled(true);
        cancelButton.setEnabled(true);

        instructions.setText(ENABLED_TEXT);
    }

    public void disableToolbox() {
        finishedEditingButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        cancelButton.setEnabled(false);   

        instructions.setText(DISABLED_TEXT);
    }
}
