package src.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * View for the help panel.
 * 
 * Actually a subclass of JDialog, in order to get the right interaction with
 * the main frame.
 */
public class HelpPanelView extends JDialog implements ActionListener {
    // JDialog is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    public HelpPanelView(JFrame parentFrame) {
        super(parentFrame);

        setTitle("Help");

        // Have to call setSize() for setLocationRelativeTo()
        setSize(720, 600);
        setLocationRelativeTo(parentFrame);

        JPanel buttonPane = new JPanel();
        JButton button = new JButton("OK");
        buttonPane.add(button);
        button.addActionListener(this);

        add(new HelpPicPanel(), BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

    /**
     * Panel that holds the help image.
     */
    private class HelpPicPanel extends JPanel {
        // JPanel is serializable, so we need some ID to avoid compiler
        // warnings.
        private static final long serialVersionUID = 1L;

        private BufferedImage helpImage;

        public HelpPicPanel() {
            try {
                helpImage = ImageIO.read(new File("hci/images/help.png"));
            } catch (IOException e) {
                // TODO: Handle this somehow.
            }

            // Force help panel into correct size.
            Dimension bestSize = new Dimension(helpImage.getWidth(), helpImage.getHeight());
            this.setMinimumSize(bestSize);
            this.setMaximumSize(bestSize);
            this.setPreferredSize(bestSize);
            this.setSize(bestSize);
        }

        @Override
        public void paintComponent(Graphics g) {
            g.drawImage(helpImage, 0, 0, null);
        }

    }
}
