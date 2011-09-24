package src;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Box;
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
                f.setLayout(new BorderLayout());  
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel contentPane = new JPanel();
                f.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
                f.setContentPane(contentPane);

                final ImagePanel imagePanel = new ImagePanel(argv[0]);
                f.add(imagePanel, BorderLayout.CENTER);

                JPanel sidePanel = new JPanel();
                GridLayout sidePanelLayout = new GridLayout(2,1);
                sidePanelLayout.setVgap(20);
                sidePanel.setLayout(sidePanelLayout);
                
                // Here is the beginning of the toolbox code
                final JPanel toolboxPanel = new JPanel();

                JButton newPolyButton = new JButton("Done");
                newPolyButton.setMnemonic(KeyEvent.VK_N);
                newPolyButton.setSize(50, 20);
                newPolyButton.setEnabled(true);
                newPolyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.addNewPolygon();
                        toolboxPanel.setVisible(false);
                    }
                });
                newPolyButton.setToolTipText("Finish editing polygon");

                toolboxPanel.add(newPolyButton);

                JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
                undoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.undo();
                    }
                });
                undoButton.setToolTipText("Undo");

                JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
                redoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.redo();
                    }
                });
                undoButton.setToolTipText("Redo");

                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        imagePanel.cancel();
                    }
                });

                toolboxPanel.add(undoButton);
                toolboxPanel.add(redoButton);
                toolboxPanel.add(cancelButton);
                // Here is the end of the toolbox code

               AtomicReference<JPanel> toolBoxReferencePanel = new AtomicReference<JPanel>(toolboxPanel);
               LabelPanel labelPanel = new LabelPanel(toolBoxReferencePanel);  
               
               sidePanel.add(labelPanel);
               sidePanel.add(toolboxPanel);
               //toolboxPanel.setVisible(false);

                f.add(sidePanel, BorderLayout.EAST);
                
                f.pack();
                f.setVisible(true);
            }
        });
    }
}
