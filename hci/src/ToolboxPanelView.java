package src;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * View for the toolbox floating panel, handling interactions with the toolbox
 * buttons.
 * 
 * Actually a subclass of JDialog, in order to get the right interaction with
 * the main frame.
 */
public class ToolboxPanelView extends JDialog {
    // JDialog is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private final AppController controller;

    public ToolboxPanelView(JFrame parentFrame, AppController appController) {
        super(parentFrame);

        this.controller = appController;

        setLayout(new FlowLayout());

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

        addWindowListener(new ToolboxWindowListener(controller));

        setFocusableWindowState(false);
        setResizable(false);
        setVisible(false);
        pack();
    }

    /**
     * Listener for the Toolbox, to allow the X button to apply the same effect
     * as "Cancel".
     */
    private static class ToolboxWindowListener implements WindowListener {
        private final AppController controller;

        public ToolboxWindowListener(AppController controller) {
            this.controller = controller;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            controller.cancelDrawingVertex();
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }
    }
}
