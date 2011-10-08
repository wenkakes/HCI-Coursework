package src;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import src.nonui.AppController;

/**
 * Launcher for the application. Sets the {@link UIManager.LookAndFeelInfo} of
 * the application and instantiates the main controller.
 */
public class ImageLabeller {

    /**
     * The entry point for the application.
     * 
     * @param argv the arguments to the program, which are ignored
     */
    public static void main(String argv[]) {
        setLookAndFeel();

        // Folder setup.
        try {
            setupApplicationFolders();
        } catch (IOException e) {
            // TODO: Bail.
            System.err.println("IOException caught: " + e.getMessage());
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AppController();
            }
        });
    }

    /**
     * Attempts to set the program look and feel to "Nimbus".
     * 
     * @see <a href="http://download.oracle.com/javase/6/docs/technotes/guides/jweb/otherFeatures/nimbus_laf.html">Nimbus</a>
     */
    private static void setLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore - just use the default theme.
        }
    }

    /**
     * Attempts to set up the folders for the application.
     * 
     * @throws IOException if unable to create folder, etc
     */
    private static void setupApplicationFolders() throws IOException {
        String userHome = System.getProperty("user.home");

        // The main $HOME/ImageLabeller directory.
        File imageLabellerDir = new File(userHome + "/ImageLabeller");
        if (!imageLabellerDir.exists() && !imageLabellerDir.mkdir()) {
            throw new IOException("Unable to create ImageLabeller directory at "
                    + imageLabellerDir.getAbsolutePath());
        }

        // The .settings file.
        File settingsFile = new File(imageLabellerDir.getAbsolutePath() + "/.settings");
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                throw new IOException("Unable to create settings file at "
                        + settingsFile.getAbsolutePath());
            }
        }

        // The Projects directory.
        File projectsDir = new File(imageLabellerDir.getAbsolutePath() + "/Projects");
        if (!projectsDir.exists() && !projectsDir.mkdir()) {
            throw new IOException("Unable to create Projects directory at "
                    + projectsDir.getAbsolutePath());
        }
    }
}
