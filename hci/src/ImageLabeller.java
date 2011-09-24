package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Main class of the program. Instantiates the JFrame etc etc.
 */
public class ImageLabeller {

    public static void main(final String argv[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel contentPane = new JPanel();
                f.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
                f.setContentPane(contentPane);

                final ImagePanel imagePanel = new ImagePanel(argv[0]);
                f.add(imagePanel);

                JPanel toolboxPanel = new JPanel();

                JButton newPolyButton = new JButton("New object");
                newPolyButton.setMnemonic(KeyEvent.VK_N);
                newPolyButton.setSize(50, 20);
                newPolyButton.setEnabled(true);
                newPolyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.addNewPolygon();
                    }
                });
                newPolyButton.setToolTipText("Click to add new object");

                toolboxPanel.add(newPolyButton);

                JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
                undoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.undo();
                    }
                });

                JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
                redoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.redo();
                    }
                });

                toolboxPanel.add(undoButton);
                toolboxPanel.add(redoButton);

               JPanel labelPanel = new LabelPanel();
                
                f.add(toolboxPanel);
                f.add(labelPanel);

                f.pack();
                f.setVisible(true);
            }
        });
    }
}
