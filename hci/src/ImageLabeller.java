package src;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Launcher for the application. Sets the {@link UIManager.LookAndFeelInfo} of
 * the application and instantiates the controller.
 */
public class ImageLabeller {

    public static void main(String argv[]) {
        setLookAndFeel();

        // The user may pass in an initial image name at the command line.
        final String imageName = (argv.length > 0) ? argv[0] : "";

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AppController(imageName);
            }
        });
    }

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
}
