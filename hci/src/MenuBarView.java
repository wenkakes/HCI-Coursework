package src;

import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class MenuBarView extends JMenuBar {

    private static final long serialVersionUID = 1L;

    private final AppController controller;

    public MenuBarView(AppController appController) {
        this.controller = appController;
        initUI();
    }

    public final void initUI() {
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem newProject = new JMenuItem("New Project");
        newProject.setMnemonic(KeyEvent.VK_N);
        newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                controller.newProject();
            }
        });

        JMenuItem closeProject = new JMenuItem("Close Project");
        closeProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
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

        JMenuItem saveImage = new JMenuItem("Save Image");
        saveImage.setMnemonic(KeyEvent.VK_S);
        saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        JMenuItem closeImage = new JMenuItem("Close Image");

        JMenuItem exit = new JMenuItem("Exit");

        file.add(newProject);
        file.add(closeProject);
        file.add(openProject);
        file.addSeparator();
        file.add(importImage);
        file.add(openImage);
        file.add(saveImage);
        file.add(closeImage);
        file.addSeparator();
        file.add(exit);

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
        // howToUse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        howToUse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                HelpDialog helpDialog = new HelpDialog();

                // TODO: Create in middle of screen, not at a mouse location
                // offset -_-.
                java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                mouseLocation.setLocation(mouseLocation.getX(), mouseLocation.getY() + 20);

                helpDialog.setLocation(mouseLocation);
                helpDialog.setVisible(true);

            }
        });

        JMenuItem aboutProgram = new JMenuItem("About");
        aboutProgram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame parent = new JFrame();
                parent.setTitle("About");
                JOptionPane.showMessageDialog(parent, "<html> Image Labeller v1.1 <br />"
                        + "A 4th year Human-Computer Interaction project </html>");

            }
        });

        help.add(howToUse);
        help.add(aboutProgram);

        this.add(file);
        this.add(edit);
        this.add(help);
    }
}
