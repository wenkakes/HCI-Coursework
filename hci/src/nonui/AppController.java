package src.nonui;

import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileSystemView;

import src.ui.ImagePanelView;
import src.ui.LabelPanelView;
import src.ui.MenuBarView;
import src.ui.ToolboxPanelView;
import src.utils.DirectoryRestrictedFileSystemView;
import src.utils.LabelIO;
import src.utils.LabelIO.LabelParseException;
import src.utils.Point;
import src.utils.Polygon;

/**
 * Main controller class for the application.
 */
public class AppController {
    // The main folder for the application.
    private static final String MAIN_FOLDER = System.getProperty("user.home") + "/ImageLabeller";

    // The sub-controllers.
    private final ImageController imageController = new ImageController(this);

    // The core Swing components that make up the application.
    private final JFrame appFrame = new JFrame("Image Labeller");
    private final ImagePanelView imagePanel = new ImagePanelView(imageController);
    private final MenuBarView menuBar = new MenuBarView(appFrame, this);
    private final LabelPanelView labelPanel = new LabelPanelView(appFrame, this);
    private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(appFrame, this);

    // The model.
    private Map<String, Polygon> completedPolygons = new HashMap<String, Polygon>();

    // The application state.
    private ApplicationState applicationState = ApplicationState.DEFAULT;

    // The current collection.
    private String currentCollectionName;
    private String currentImageName;

    public AppController() {
        appFrame.setLayout(new FlowLayout());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        appFrame.add(imagePanel);
        appFrame.add(labelPanel);
        appFrame.setJMenuBar(menuBar);

        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setResizable(false);

        imageController.setPanel(imagePanel);

        loadSettingsFile();
        setMenuItemsEnabled();

        // Show tooltips fast.
        ToolTipManager.sharedInstance().setInitialDelay(100);
    }

    /**
     * Sets whether or not different menu items should be enabled.
     */
    private void setMenuItemsEnabled() {
        boolean collectionOpened = currentCollectionName != null;
        boolean imageOpened = currentImageName != null;

        // File menu.
        menuBar.setCloseCollectionEnabled(collectionOpened);
        menuBar.setImportImageEnabled(collectionOpened);
        menuBar.setOpenImageEnabled(collectionOpened);
        menuBar.setSaveImageEnabled(imageOpened);
        menuBar.setCloseImageEnabled(imageOpened);

        // Edit menu.
        menuBar.setAddPolygonEnabled(imageOpened && applicationState == ApplicationState.DEFAULT);
        menuBar.setRenamePolygonEnabled(completedPolygons.size() > 0);
        menuBar.setDeleteSelectedLabelEnabled(completedPolygons.size() > 0);
        menuBar.setDeleteAllLabelsEnabled(completedPolygons.size() > 0);
    }

    /**
     * Returns a list of the points of each completed polygon.
     */
    public List<List<Point>> getCompletedPolygonsPoints() {
        List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
        for (Polygon polygon : completedPolygons.values()) {
            points.add(new ArrayList<Point>(polygon.getPoints()));
        }
        return points;
    }

    /**
     * Starts adding a new polygon.
     */
    public void startAddingNewPolygon() {
        applicationState = ApplicationState.ADDING_POLYGON;

        labelPanel.setAddButtonEnabled(false);

        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX(), mouseLocation.getY() + 20);
        toolboxPanel.setLocation(mouseLocation);
        toolboxPanel.setVisible(true);
    }

    /**
     * Renames a polygon.
     * 
     * @param oldName the old name for the polygon
     * @param newName the new name for the polygon
     */
    public void renamePolygon(String oldName, String newName) {
        Polygon polygon = completedPolygons.remove(oldName);
        if (polygon != null) {
            polygon.setName(newName);
            completedPolygons.put(newName, polygon);
        }
    }

    /**
     * Removes a polygon from the image.
     * 
     * @param name the name of the polygon to remove
     */
    public void removePolygon(String name) {
        Polygon removedPolygon = completedPolygons.remove(name);
        if (removedPolygon == imageController.getEditedPolygon()) {
            applicationState = ApplicationState.DEFAULT;
        }
        imagePanel.repaint();

        setMenuItemsEnabled();
    }

    /**
     * Saves the current list of polygons to a file.
     * 
     * @param file the file to save to
     */
    public void saveLabels(File file) {
        try {
            LabelIO.writeLabels(file, new ArrayList<Polygon>(completedPolygons.values()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads in a new list of polygons from a file.
     * 
     * @param file the file to load from
     */
    public void loadLabels() {
        FileSystemView fsv = new DirectoryRestrictedFileSystemView(new File(MAIN_FOLDER
                + "/Collections/" + currentCollectionName + "/labels"));
        JFileChooser chooser = new JFileChooser(fsv.getHomeDirectory(), fsv);
        int returnValue = chooser.showOpenDialog(appFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File loadFile = chooser.getSelectedFile();

            try {
                completedPolygons = LabelIO.readLabels(loadFile);
                labelPanel.clear();
                for (String name : completedPolygons.keySet()) {
                    labelPanel.addLabel(name);
                }
                imagePanel.repaint();
            } catch (LabelParseException e) {
                JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        setMenuItemsEnabled();
    }

    /**
     * Called when the "Done" button on the toolbox is clicked.
     */
    public void toolboxDoneButtonClicked() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                imageController.finishedAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    public void undoLastVertex() {
        imageController.undo();
    }

    public void redoLastVertex() {
        imageController.redo();
    }

    /**
     * Called when the "Cancel" button on the toolbar is clicked.
     */
    public void toolboxCancelButtonClicked() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                cancelAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Called when the toolbox window is closed.
     */
    public void toolboxWindowClosed() {
        switch (applicationState) {
            case DEFAULT:
                // TODO: Throw/show appropriate error, as this shouldn't happen.
                break;
            case ADDING_POLYGON:
                cancelAddingPolygon();
                break;
            case EDITING_POLYGON:
                // TODO: Implement explicit editing of polygons.
                break;
            default:
                // TODO: Throw/show appropriate error.
        }
    }

    /**
     * Deletes all of the polygons.
     */
    public void deleteAllPolygons() {
        labelPanel.deleteAllPolygons();
        imagePanel.repaint();
    }

    /**
     * Deletes the currently selected polygons.
     */
    public void deleteSelectedPolygons() {
        labelPanel.deleteSelectedPolygons();
        imagePanel.repaint();
    }

    /**
     * Called when the user is finished adding the current polygon, either by
     * clicking on the starting point, double-clicking, or clicking the "Done"
     * button on the toolbox.
     * 
     * @param newPolygonName
     */
    public void finishedAddingPolygon(String newPolygonName) {
        applicationState = ApplicationState.DEFAULT;

        labelPanel.setAddButtonEnabled(true);
        labelPanel.addLabel(newPolygonName);

        toolboxPanel.setVisible(false);

        setMenuItemsEnabled();
    }

    /**
     * Called when the user cancels the polygon that they are currently adding.
     */
    private void cancelAddingPolygon() {
        applicationState = ApplicationState.DEFAULT;

        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

        imageController.cancel();
    }

    public Polygon getPolygon(String name) {
        return completedPolygons.get(name);
    }

    public void closeImage() {
        currentImageName = null;

        imagePanel.setImage(null);
        labelPanel.clear();

        labelPanel.setAddButtonEnabled(false);
        labelPanel.setLoadButtonEnabled(false);

        setMenuItemsEnabled();
    }

    public void highlightSelected(List<String> highlightedNames) {
        if (applicationState == ApplicationState.EDITING_POLYGON
                && !highlightedNames.contains(imageController.getEditedPolygon().getName())) {
            applicationState = ApplicationState.DEFAULT;
        }

        imagePanel.repaint();
    }

    public List<List<Point>> getSelectedPolygonsPoints() {
        List<Polygon> selectedPolygons = getSelectedPolygons();
        List<List<Point>> points = new ArrayList<List<Point>>(selectedPolygons.size());
        for (Polygon selectedPolygon : selectedPolygons) {
            points.add(selectedPolygon.getPoints());
        }
        return points;
    }

    public List<Polygon> getSelectedPolygons() {
        List<String> selectedNames = labelPanel.getSelectedNames();
        List<Polygon> selectedPolygons = new ArrayList<Polygon>(selectedNames.size());

        for (String name : selectedNames) {
            selectedPolygons.add(completedPolygons.get(name));
        }

        return selectedPolygons;
    }

    public void newCollection() {
        // Prompt for collection name.
        String newCollectionName = null;
        boolean hasName = false;

        File collectionsDir = new File(MAIN_FOLDER + "/Collections");
        if (!collectionsDir.exists() && !collectionsDir.mkdir()) {
            // TODO: Error somehow. This is bad enough that we could crash out.
            System.err.println("Cannot open Collections directory.");
            return;
        }

        FileFilter directoryFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        File directories[] = collectionsDir.listFiles(directoryFilter);

        while (!hasName) {
            String message = "Collection Name";
            newCollectionName = JOptionPane.showInputDialog(appFrame, message);

            hasName = true;

            // Occurs if the user hits the cancel option.
            if (newCollectionName == null) {
                return;
            }

            newCollectionName = newCollectionName.trim();

            if (newCollectionName.isEmpty()) {
                JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                hasName = false;
                continue;
            }

            // Check for duplicates.
            for (int i = 0; i < directories.length; i++) {
                if (newCollectionName.equals(directories[i].getName())) {
                    // TODO: Give the option to overwrite.
                    JOptionPane.showMessageDialog(appFrame, "That name is already in use.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    hasName = false;
                    break;
                }
            }
        }

        // Create folders for new collection.
        File newCollectionDir = new File(collectionsDir.getAbsolutePath() + "/" 
                + newCollectionName);
        File imageFolder = new File(newCollectionDir.getAbsolutePath() + "/images");
        File labelsFolder = new File(newCollectionDir.getAbsolutePath() + "/labels");
        if (!newCollectionDir.mkdir() || !imageFolder.mkdir() || !labelsFolder.mkdir()) {
            // TODO: Error
            System.err.println("Unable to create director for new collection.");
            return;
        }

        // Set the current collection.
        currentCollectionName = newCollectionName;

        // Clear everything interface wise, etc.
        closeImage();

        // Update the .settings file.
        writeToSettingsFile(currentCollectionName, "");

        setMenuItemsEnabled();
    }

    public void closeCollection() {
        if (currentCollectionName == null) {
            return;
        }

        // TODO: Confirm?

        currentCollectionName = null;
        closeImage();

        // Update the .settings file.
        writeToSettingsFile("", "");

        setMenuItemsEnabled();
    }

    public void openCollection() {
        // Ask user to choose collection.
        File collectionsDir = new File(MAIN_FOLDER + "/Collections");
        if (!collectionsDir.exists() && !collectionsDir.mkdir()) {
            // TODO: Error somehow.
            System.err.println("Cannot open Collections directory.");
            return;
        }

        FileFilter directoryFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        File collections[] = collectionsDir.listFiles(directoryFilter);
        if (collections.length == 0) {
            JOptionPane.showMessageDialog(appFrame, "There are no collections. Please create one.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String collectionNames[] = new String[collections.length];
        for (int i = 0; i < collectionNames.length; i++) {
            collectionNames[i] = collections[i].getName();
        }

        String newCollectionName = (String) JOptionPane.showInputDialog(appFrame,
                "Choose a collection to open", "Open Collection", JOptionPane.QUESTION_MESSAGE, 
                null, collectionNames, collectionNames[0]);

        if (newCollectionName == null) {
            // User hit cancel.
            return;
        }

        // Close current collection.
        closeImage();

        // Open other collection.
        // TODO: Open images, etc.
        currentCollectionName = newCollectionName;

        // Update settings file.
        writeToSettingsFile(currentCollectionName, "");

        setMenuItemsEnabled();
    }

    private boolean writeToSettingsFile(String collectionName, String imageName) {
        File settingsFile = new File(MAIN_FOLDER + "/.settings");
        if (!settingsFile.canWrite()) {
            System.err.println("Cannot write to Settings file.");
            return false;
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile, false));

            out.write(collectionName);
            if (!collectionName.isEmpty()) {
                out.newLine();

                out.write(imageName);
                if (!imageName.isEmpty()) {
                    out.newLine();
                }
            }
            out.close();
        } catch (IOException e) {
            // TODO: Error
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void importImage() {
        if (currentCollectionName == null) {
            return;
        }

        // User chooses image from file dialog.
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose an image to import");
        int returnValue = chooser.showOpenDialog(appFrame);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File imageFile = chooser.getSelectedFile();
        String imageName = imageFile.getName();

        // Check for filename conflict. If so, prompt user to overwrite, rename,
        // or cancel.
        File imagesDirectory = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + "/images");
        if (!imagesDirectory.exists() && !imagesDirectory.mkdir()) {
            // TODO: Error
            System.err.println("Cannot open images directory for collection.");
            return;
        }

        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        };
        File images[] = imagesDirectory.listFiles(fileFilter);
        String imageNames[] = new String[images.length];
        for (int i = 0; i < imageNames.length; i++) {
            imageNames[i] = images[i].getName();
        }

        Object[] options = { "Cancel", "Rename New Image", "Overwrite Old Image" };

        // Cannot just use getNameFromUser, as this is a complex case.
        boolean hasName = false;
        while (!hasName) {
            for (int i = 0; i < images.length; i++) {
                if (imageName.equals(images[i].getName())) {
                    int result = JOptionPane.showOptionDialog(appFrame,
                            "Cannot import image due to duplicate name.", "Unable to import image",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            options, options[2]);
                    if (result == 0) {
                        // Cancel.
                        return;
                    } else if (result == 1) {
                        // Rename
                        imageName = getNameFromUser("Image Name", imageNames, false);
                        if (imageName == null) {
                            // User cancelled.
                            return;
                        }

                        break;
                    } else {
                        // Overwrite
                        // TODO: Delete labels file.
                        break;
                    }
                }
            }

            hasName = true;
        }

        // Copy the image over.
        File destFile = new File(imagesDirectory.getAbsolutePath() + "/" + imageName);
        try {
            copyFile(imageFile, destFile);
        } catch (IOException e) {
            // TODO: Error
            System.err.println("IOException when importing image.");
            return;
        }

        // Open image up, no labels.
        cancelAddingPolygon();
        imageController.setImage(destFile);
        currentImageName = imageName;

        writeToSettingsFile(currentCollectionName, currentImageName);

        setMenuItemsEnabled();

        // TODO: STEPHEN
        completedPolygons.clear();
        labelPanel.clear();
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;

        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());

        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }

    }

    private String getNameFromUser(String message, String[] disallowedNames, boolean allowEmpty) {
        String name = null;
        boolean hasName = false;

        while (!hasName) {
            name = JOptionPane.showInputDialog(appFrame, message);

            if (name == null) {
                // User cancelled.
                return null;
            }

            name = name.trim();
            if (name.isEmpty() && !allowEmpty) {
                JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // Check for duplicates.
            hasName = true;
            for (int i = 0; i < disallowedNames.length; i++) {
                if (name.equals(disallowedNames[i])) {
                    JOptionPane.showMessageDialog(appFrame, "That name is already in use.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    hasName = false;
                }
            }
        }

        return name;
    }

    public void openImage() {
        if (currentCollectionName == null) {
            return;
        }

        // User chooses image
        File imagesDirectory = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + "/images");
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        };

        File images[] = imagesDirectory.listFiles(fileFilter);
        if (images.length == 0) {
            JOptionPane.showMessageDialog(appFrame, "Collection has no images. Please import some.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String imageNames[] = new String[images.length];
        for (int i = 0; i < imageNames.length; i++) {
            imageNames[i] = images[i].getName();
        }


        FileSystemView fsv = new DirectoryRestrictedFileSystemView(imagesDirectory);
        JFileChooser chooser = new JFileChooser(fsv.getHomeDirectory(), fsv);
        int result = chooser.showOpenDialog(appFrame);
        if (result != JFileChooser.APPROVE_OPTION) {
            // User hit cancel.
            return;
        }

        // Open image up
        File imageFile = chooser.getSelectedFile();
        imageController.setImage(imageFile);
        currentImageName = imageFile.getName();

        writeToSettingsFile(currentCollectionName, currentImageName);

        setMenuItemsEnabled();

        // TODO: STEPHEN
        completedPolygons.clear();
        labelPanel.clear();
    }

    public void save() {
        if (currentImageName == null) {
            // TODO: Shouldnt be able to save if no image.
            return;
        }

        // Get current image name, turn into labels name.
        String labelName = removeExtension(currentImageName) + ".labels";

        // Save labels file
        File labelFile = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + "/labels/"
                + labelName);
        if (labelFile.exists()) {
            // TODO: Check with user?
        }

        saveLabels(labelFile);
    }

    private String removeExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    /**
     * Loads the data from the settings file.
     */
    private void loadSettingsFile() {
        File settingsFile = new File(MAIN_FOLDER + "/.settings");
        if (!settingsFile.exists() || !settingsFile.canRead()) {
            // Just ignore it.
            return;
        }

        String collectionName = null;
        String imageName = null;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(settingsFile));
            collectionName = in.readLine();
        } catch (IOException e) {
            return;
        }

        try {
            imageName = in.readLine();
        } catch (IOException e) {
            // Image names aren't needed.
        }

        // TODO: Check collection exists.

        currentCollectionName = collectionName;
        currentImageName = imageName;
        if (imageName != null) {
            File imageFile = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + "/images/"
                    + currentImageName);
            imageController.setImage(imageFile);

            // TODO: STEPHEN
            completedPolygons.clear();
            labelPanel.clear();
        }
    }

    public Map<String, Polygon> getCompletedPolygons() {
        return completedPolygons;
    }

    public List<String> getSelectedNames() {
        return labelPanel.getSelectedNames();
    }

    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public JFrame getAppFrame() {
        return appFrame;
    }

    public void setApplicationState(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public void renameSelectedPolygon() {
        labelPanel.renameSelectedPolygon();
    }

    public void selectPolygon(Polygon polygon) {
        labelPanel.selectPolygon(polygon.getName());
    }
}
