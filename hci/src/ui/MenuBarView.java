package src.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
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
    private final AppController controller;

    // Menu items that can be enabled/disabled.
    private JMenuItem closeCollection;
    private JMenuItem importImage;
    private JMenuItem openImage;
    private JMenuItem saveImage;
    private JMenuItem closeImage;
    private JMenuItem addPolygon;
    private JMenuItem renameSelected;
    private JMenuItem deleteSelected;
    private JMenuItem deleteAll;

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
     * <li>New Collection</li>
     * <li>Close Collection</li>
     * <li>Open Collection</li>
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

        JMenuItem newCollection = new JMenuItem("New Collection");
        newCollection.setMnemonic(KeyEvent.VK_N);
        newCollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.newCollection();
            }
        });

        closeCollection = new JMenuItem("Close Collection");
        closeCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.closeCollection();
            }
        });

        JMenuItem openCollection = new JMenuItem("Open Collection");
        openCollection.setMnemonic(KeyEvent.VK_O);
        openCollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openCollection();
            }
        });

        importImage = new JMenuItem("Import Image");
        importImage.setMnemonic(KeyEvent.VK_I);
        importImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        importImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.importImage();
            }
        });

        openImage = new JMenuItem("Open Image");
        openImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openImage();
            }
        });

        // TODO: Rename this? (it saves labels, not images).
        saveImage = new JMenuItem("Save Image");
        saveImage.setMnemonic(KeyEvent.VK_S);
        saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.save();
            }
        });

        closeImage = new JMenuItem("Close Image");
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

        fileMenu.add(newCollection);
        fileMenu.add(closeCollection);
        fileMenu.add(openCollection);
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
     * <li>Add New Label</li>
     * <li>Rename Label</li>
     * <li>Delete Selected Label(s)</li>
     * <li>Delete All Labels</li>
     * </ul>
     */
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        addPolygon = new JMenuItem("Add New Label");
        addPolygon.setMnemonic(KeyEvent.VK_L);
        addPolygon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        addPolygon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.startAddingNewPolygon();
            }
        });
        
        renameSelected = new JMenuItem("Rename Selected Label");
        renameSelected.setMnemonic(KeyEvent.VK_R);
        renameSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        renameSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.renameSelectedPolygon();
            }
        });

        deleteSelected = new JMenuItem("Delete Selected Label(s)");
        deleteSelected.setMnemonic(KeyEvent.VK_D);
        deleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        deleteSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteSelectedPolygons();
            }
        });

        deleteAll = new JMenuItem("Delete All Labels");
        deleteAll.setMnemonic(KeyEvent.VK_A);
        deleteAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK
                | ActionEvent.SHIFT_MASK));
        deleteAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteAllPolygons();
            }
        });

        editMenu.add(addPolygon);
        editMenu.add(renameSelected);
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
                JOptionPane.showMessageDialog(parentFrame, "<html> Image Labeller v1.2 <br />"
                        + "A 4th year Human-Computer Interaction project </html>", "About",
                        JOptionPane.INFORMATION_MESSAGE);

            }
        });

        JCheckBoxMenuItem quickTips = new JCheckBoxMenuItem("Enable Quick Tips?");
        quickTips.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		controller.setTipsOn(true);
        	}
        });
        
        help.add(quickTips);
        help.add(howToUse);
        help.add(aboutProgram);

        return help;
    }

    public void setCloseCollectionEnabled(boolean enabled) {
        closeCollection.setEnabled(enabled);
    }

    public void setImportImageEnabled(boolean enabled) {
        importImage.setEnabled(enabled);
    }

    public void setOpenImageEnabled(boolean enabled) {
        openImage.setEnabled(enabled);
    }

    public void setSaveImageEnabled(boolean enabled) {
        saveImage.setEnabled(enabled);
    }

    public void setCloseImageEnabled(boolean enabled) {
        closeImage.setEnabled(enabled);
    }

    public void setAddPolygonEnabled(boolean enabled) {
        addPolygon.setEnabled(enabled);
    }

    public void setRenamePolygonEnabled(boolean enabled) {
        renameSelected.setEnabled(enabled);
    }

    public void setDeleteSelectedLabelEnabled(boolean enabled) {
        deleteSelected.setEnabled(enabled);
    }

    public void setDeleteAllLabelsEnabled(boolean enabled) {
        deleteAll.setEnabled(enabled);
    }
}
