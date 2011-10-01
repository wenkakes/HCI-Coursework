package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBarView extends JMenuBar {

    private static final long serialVersionUID = 1L;

    private final AppController controller;

    public MenuBarView(AppController appController) {
        this.controller = appController;
        initUI();
    }

    public final void initUI() {

        // ImageIcon iconNew = new ImageIcon(getClass().getResource("new.png"));
        // ImageIcon iconOpen = new
        // ImageIcon(getClass().getResource("open.png"));
        // ImageIcon iconSave = new
        // ImageIcon(getClass().getResource("save.png"));
        // ImageIcon iconExit = new
        // ImageIcon(getClass().getResource("exit.png"));

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenu imp = new JMenu("Import");
        imp.setMnemonic(KeyEvent.VK_M);

        JMenuItem mproj = new JMenuItem("Import existing project");
        JMenuItem mlabels = new JMenuItem("Import existing labels");
        mproj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

        imp.add(mproj);
        imp.add(mlabels);

        // JMenuItem fileNew = new JMenuItem("New", iconNew);
        JMenuItem fileNew = new JMenuItem("New ____ from Image");
        fileNew.setMnemonic(KeyEvent.VK_N);
        fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Open an image to label");
                int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    controller.openImage(file);
                }
            }
        });

        // JMenuItem fileSave = new JMenuItem("Save", iconSave);
        JMenuItem fileSave = new JMenuItem("Save");
        fileSave.setMnemonic(KeyEvent.VK_S);
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save as...");
                int returnValue = chooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    controller.saveLabels(file);
                }
            }
        });

        JMenuItem fileClose = new JMenuItem("Close");
        fileClose.setMnemonic(KeyEvent.VK_C);
        fileClose.setToolTipText("Close current image");
        
        fileClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                controller.closeImage();
            }

        });

        // JMenuItem fileExit = new JMenuItem("Exit", iconExit);
        JMenuItem fileExit = new JMenuItem("Exit");
        fileExit.setMnemonic(KeyEvent.VK_X);
        fileExit.setToolTipText("Exit application");
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

        fileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }

        });

        file.add(fileNew);
        file.add(imp);
        file.add(fileSave);
        file.addSeparator();
        file.add(fileClose);
        file.add(fileExit);

        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);

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

        edit.add(deleteSelected);
        edit.add(deleteAll);
        
        JMenu help = new JMenu("Help");
        edit.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem howToUse = new JMenuItem("How to Use");
        howToUse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                controller.openImage(new File("hci/images/help.png"));
            }
        });
        
        JMenuItem aboutProgram = new JMenuItem("About");
        help.add(howToUse);
        help.add(aboutProgram);

        this.add(file);
        this.add(edit);
        this.add(help);
    }
}
