package src;

import javax.swing.SwingUtilities;

/**
 * Main class of the program.
 */
public class ImageLabeller {

    public static void main(String argv[]) {
        // The user may pass in an initial image name at the command line.
        final String imageName = (argv.length > 0) ? argv[0] : "";

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AppController(imageName);
            }
        });
    }
}
