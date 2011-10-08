package src.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import src.nonui.AppController;

/**
 * View for the menu bar.
 */
public class MenuBarView extends JMenuBar {
    // JMenuBar is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    private JFrame parentFrame;
    private final AppController controller;;

    public MenuBarView(JFrame parentFrame, AppController appController) {
        this.parentFrame = parentFrame;
        this.controller = appController;
        initUI();
    }

    /**
     * Sets up the menu items.
     */
    private void initUI() {
        add(createFileMenu());
        add(createEditMenu());
        add(createHelpMenu());
    }

    /**
     * Creates a file menu with the following options:
     * 
     * <ul>
     * <li>New Project</li>
     * <li>Close Project</li>
     * <li>Open Project</li>
     * <li>Import Image</li>
     * <li>Open Image</li>
     * <li>Save Image</li>
     * <li>Close Image</li>
     * <li>Exit</li>
     * </ul>
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newProject = new JMenuItem("New Project");
        newProject.setMnemonic(KeyEvent.VK_N);
        newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.newProject();
            }
        });

        JMenuItem closeProject = new JMenuItem("Close Project");
        closeProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.closeProject();
            }
        });

        JMenuItem openProject = new JMenuItem("Open Project");
        openProject.setMnemonic(KeyEvent.VK_O);
        openProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openProject();
            }
        });

        JMenuItem importImage = new JMenuItem("Import Image");
        importImage.setMnemonic(KeyEvent.VK_I);
        importImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        importImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.importImage();
            }
        });

        JMenuItem openImage = new JMenuItem("Open Image");
        openImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openImage();
            }
        });

        // TODO: Rename this? (it saves labels, not images).
        JMenuItem saveImage = new JMenuItem("Save Image");
        saveImage.setMnemonic(KeyEvent.VK_S);
        saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.save();
            }
        });

        JMenuItem closeImage = new JMenuItem("Close Image");
        closeImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.closeImage();
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(newProject);
        fileMenu.add(closeProject);
        fileMenu.add(openProject);
        fileMenu.addSeparator();

        fileMenu.add(importImage);
        fileMenu.add(openImage);
        fileMenu.add(saveImage);
        fileMenu.add(closeImage);
        fileMenu.addSeparator();

        fileMenu.add(exit);

        return fileMenu;
    }

    /**
     * Creates an edit menu with the following options:
     * 
     * <ul>
     * <li>Delete Selected Label(s)</li>
     * <li>Delete All Labels</li>
     * </ul>
     */
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem deleteSelected = new JMenuItem("Delete Selected Label(s)");
        deleteSelected.setMnemonic(KeyEvent.VK_D);
        deleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        deleteSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteSelectedPolygons();
            }
        });

        JMenuItem deleteAll = new JMenuItem("Delete All Labels");
        deleteAll.setMnemonic(KeyEvent.VK_A);
        deleteAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK
                | ActionEvent.SHIFT_MASK));
        deleteAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteAllPolygons();
            }
        });

        editMenu.add(deleteSelected);
        editMenu.add(deleteAll);

        return editMenu;
    }

    /**
     * Creates a help menu with the following options:
     * 
     * <ul>
     * <li>How to Use</li>
     * <li>About</li>
     * </ul>
     */
    private JMenu createHelpMenu() {
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);

        JMenuItem howToUse = new JMenuItem("How to Use");
        howToUse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        howToUse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HelpPanelView helpDialog = new HelpPanelView(parentFrame);
                helpDialog.setVisible(true);
            }
        });

        JMenuItem aboutProgram = new JMenuItem("About");
        aboutProgram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(parentFrame, "<html> Image Labeller v1.1 <br />"
                        + "A 4th year Human-Computer Interaction project </html>", "About",
                        JOptionPane.INFORMATION_MESSAGE);

            }
        });

        help.add(howToUse);
        help.add(aboutProgram);

        return help;
    }
}
