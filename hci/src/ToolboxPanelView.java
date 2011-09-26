package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * View for the toolbox panel, handling interactions with the toolbox buttons.
 */
public class ToolboxPanelView extends JPanel {
    // JPanel is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final AppController controller;

    public ToolboxPanelView(AppController appController) {
        this.controller = appController;

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

        JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.undoLastVertex();
            }
        });
        undoButton.setToolTipText("Undo");

        JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.redoLastVertex();
            }
        });
        redoButton.setToolTipText("Redo");

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelDrawingVertex();
            }
        });
        cancelButton.setToolTipText("Cancel editing polygon");

        add(finishedEditingButton);
        add(undoButton);
        add(redoButton);
        add(cancelButton);
    }
}
