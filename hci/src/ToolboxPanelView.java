package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ToolboxPanelView extends JPanel {
    // JFrame is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final AppController controller;

    public ToolboxPanelView(AppController appController) {
        this.controller = appController;

        // Setup finished editing button.
        JButton finishedEditingButton = new JButton("Done");
        finishedEditingButton.setMnemonic(KeyEvent.VK_N);
        finishedEditingButton.setSize(50, 20);
        finishedEditingButton.setEnabled(true);
        finishedEditingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.finishEditingPolygon();
            }
        });
        finishedEditingButton.setToolTipText("Finish editing polygon");

        // Undo button
        JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.undoLastVertex();
            }
        });
        undoButton.setToolTipText("Undo");

        // Redo button
        JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.redoLastVertex();
            }
        });
        undoButton.setToolTipText("Redo");

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelDrawingVertex();
            }
        });

        add(finishedEditingButton);
        add(undoButton);
        add(redoButton);
        add(cancelButton);
    }

}
