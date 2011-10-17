package src.nonui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileSystemView;

import src.ui.ImagePanelView;
import src.ui.LabelPanelView;
import src.ui.MenuBarView;
import src.ui.ThumbnailView;
import src.ui.TipsDialog;
import src.ui.ToolboxPanelView;
import src.utils.DirectoryRestrictedFileSystemView;
import src.utils.LabelIO;
import src.utils.LabelIO.LabelParseException;
import src.utils.LabelledImage;
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
    private final TipsDialog selectedLabelTip = new TipsDialog(appFrame, this, 1);
    private final TipsDialog newLabelTip = new TipsDialog(appFrame, this, 2);
    private final ThumbnailView thumbnailPanel = new ThumbnailView(this);

    // The application state.
    private ApplicationState applicationState = ApplicationState.DEFAULT;
    private boolean tipsEnabled = true;

    // The current collection.
    private String currentCollectionName = null;
    private LabelledImage currentImage = null;
    private Map<String, LabelledImage> collectionImages = null;

    public AppController() {
        appFrame.setLayout(new FlowLayout());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel leftPanel = new JPanel();
        BorderLayout leftPanelLayout = new BorderLayout();
        leftPanelLayout.setVgap(5);
        leftPanel.setLayout(leftPanelLayout);
        leftPanel.add(imagePanel, BorderLayout.CENTER);
        leftPanel.add(thumbnailPanel, BorderLayout.SOUTH);
        
        appFrame.add(leftPanel);
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
     * Opens a new collection, prompting the user for a name.
     */
    public void newCollection() {
        File collectionsDir = new File(MAIN_FOLDER + "/Collections");
        if (!collectionsDir.exists() && !collectionsDir.mkdir()) {
            // TODO: Error somehow. This is bad enough that we could crash out.
            System.err.println("Cannot open Collections directory.");
            return;
        }
        File directories[] = collectionsDir.listFiles(LabelIO.DIRECTORY_FILTER);

        // Get a name for the new collection.
        String newCollectionName = "";
        boolean hasName = false;
        while (!hasName) {
            String message = "Collection Name";
            newCollectionName = JOptionPane.showInputDialog(appFrame, message, newCollectionName);

            // Occurs if the user hits the cancel option.
            if (newCollectionName == null) {
                return;
            }

            newCollectionName = newCollectionName.trim();
            hasName = true;
            
            // TODO: Check for invalid characters.
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
        // TODO: Move to IO class?
        File newCollectionDir = new File(collectionsDir.getAbsolutePath() + "/" 
                + newCollectionName);
        File imageFolder = new File(newCollectionDir.getAbsolutePath() + "/images");
        File labelsFolder = new File(newCollectionDir.getAbsolutePath() + "/labels");
        if (!newCollectionDir.mkdir() || !imageFolder.mkdir() || !labelsFolder.mkdir()) {
            // TODO: Error
            System.err.println("Unable to create director for new collection.");
            return;
        }
        
        applicationState = ApplicationState.DEFAULT;
        currentCollectionName = newCollectionName;
        currentImage = null;
        collectionImages = new HashMap<String, LabelledImage>();
        
        // Reset the interface.
        imageController.setImage(null);
        labelPanel.disableLabelPanel();
        thumbnailPanel.clear();
        setMenuItemsEnabled();
        
        writeToSettingsFile(currentCollectionName, "");
    }

    /**
     * Closes the current collection.
     */
    public void closeCollection() {
        if (currentCollectionName == null) {
            return;
        }
        
        applicationState = ApplicationState.DEFAULT;
        currentCollectionName = null;
        currentImage = null;
        collectionImages = null;
        
        imageController.setImage(null);
        thumbnailPanel.clear();
        labelPanel.disableLabelPanel();
        
        setMenuItemsEnabled();
        
        writeToSettingsFile("", "");
    }
    
    /**
     * Opens a previously existing collection of the user's choice.
     */
    public void openCollection() {
        File collectionsDir = new File(MAIN_FOLDER + "/Collections");
        if (!collectionsDir.exists() && !collectionsDir.mkdir()) {
            // TODO: Error somehow. This is bad enough that we could crash out.
            System.err.println("Cannot open Collections directory.");
            return;
        }

        File collections[] = collectionsDir.listFiles(LabelIO.DIRECTORY_FILTER);
        if (collections.length == 0) {
            JOptionPane.showMessageDialog(appFrame, "There are no collections. Please create one.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String collectionNames[] = new String[collections.length];
        for (int i = 0; i < collectionNames.length; i++) {
            collectionNames[i] = collections[i].getName();
        }

        // Ask user to choose collection.
        String openedCollectionName = (String) JOptionPane.showInputDialog(appFrame,
                "Choose a collection to open", "Open Collection", JOptionPane.QUESTION_MESSAGE, 
                null, collectionNames, collectionNames[0]);

        if (openedCollectionName == null) {
            // User hit cancel.
            return;
        }

        applicationState = ApplicationState.DEFAULT;
        currentCollectionName = openedCollectionName;
        // TODO: Provide a way for user to open a default image?
        currentImage = null; 
        collectionImages = LabelIO.openCollection(
                new File(MAIN_FOLDER + "/Collections/" + currentCollectionName));
        
        thumbnailPanel.setImages(new ArrayList<LabelledImage>(collectionImages.values()));
        // TODO: Provide a way for user to open a default image?
        imageController.setImage(null);
        
        setMenuItemsEnabled();

        // TODO: Provide a way for user to open a default image?
        writeToSettingsFile(currentCollectionName, "");
    }

    /**
     * Imports an image into the current collection.
     */
    public void importImage() {
        if (currentCollectionName == null) {
            return;
        }

        // User chooses image from file dialog.
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose an image to import");
        chooser.setMultiSelectionEnabled(true);
        int returnValue = chooser.showOpenDialog(appFrame);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] imageFiles = chooser.getSelectedFiles();
        for (int i = 0; i < imageFiles.length; i++) {
            importImageFile(imageFiles[i]);
        }
    }

    /**
     * Saves the current image.
     */
    public void saveImage() {
        if (currentImage != null) {
            if (saveImage(currentImage)) {
                JOptionPane.showMessageDialog(appFrame, "The current image was saved.", 
                        "Images Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Saves all of the images in the current collection.
     */
    public void saveAllImages() {
        if (collectionImages.size() == 0) {
            return;
        }
        
        boolean savedOkay = true;
        for (LabelledImage labelledImage : collectionImages.values()) {
            savedOkay = saveImage(labelledImage);
        }
        
        if (savedOkay) {
            JOptionPane.showMessageDialog(appFrame, "All images were saved.", "Images Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } 
    }

    /**
     * Removes the current image from the collection.
     */
    public void removeImage() {
        if (currentImage == null) {
            return;
        }

        applicationState = ApplicationState.DEFAULT;
        
        LabelledImage removedImage = collectionImages.remove(currentImage.getName());
        thumbnailPanel.removeThumbnail(removedImage.getName());
        
        File imageFile = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + 
                "/images/" + removedImage.getName() + removedImage.getExtension());
        File labelFile = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + 
                "/labels/" + removedImage.getName() + ".labels");
        if (!imageFile.delete() || !labelFile.delete()) {
            // TODO: Warn user.
        }
        
        currentImage = null;
        imagePanel.setImage(null);
        labelPanel.disableLabelPanel();
        
        setMenuItemsEnabled();
        
        writeToSettingsFile(currentCollectionName, "");
    }
    
    /**
     * Open one of the collection images.
     * 
     * @param name the name of the image to open
     */
    public void setCurrentImage(String name) {
        applicationState = ApplicationState.DEFAULT;
        
        currentImage = collectionImages.get(name);
        
        imageController.setImage(currentImage.getImage());
        labelPanel.clear();
        for (Polygon polygon : currentImage.getLabels()) {
            labelPanel.addLabel(polygon.getName());
        }
        
        setMenuItemsEnabled();
        writeToSettingsFile(currentCollectionName, currentImage.getName());
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
     * Renames the currently selected polygon.
     */
    public void renameSelectedPolygon() {
        labelPanel.renameSelectedPolygon();
    }

    /**
     * Deletes the currently selected polygons.
     */
    public void deleteSelectedPolygons() {
        labelPanel.deleteSelectedPolygons();
        imagePanel.repaint();
    }

    /**
     * Deletes all of the polygons.
     */
    public void deleteAllPolygons() {
        labelPanel.deleteAllPolygons();
        imagePanel.repaint();
    }
    
    /**
     * Checks if the user has tips enabled or not.
     */
    public boolean areTipsOn() {
        return tipsEnabled;
    }

    /**
     * Sets whether or not tips are enabled.
     * 
     * @param enabled whether to enable tips or not
     */
    public void setTipsEnabled(boolean enabled) {
        if (enabled) {
            JOptionPane.showMessageDialog(appFrame, "Tips are now on.");
        } else {
            JOptionPane.showMessageDialog(appFrame, "<html>Tips are now off. <br />" +
                    "Remember, you can always turn tips on <br />" +
                    "again from the Help menu.</html>");
        }
        
        tipsEnabled = enabled;
        menuBar.setTipsEnabled(tipsEnabled);
    }
    
    // ---------------------------------------------------------------------------

    /**
     * Returns a list of the points of each completed polygon.
     */
    public List<List<Point>> getCompletedPolygonsPoints() {
        List<List<Point>> points = new ArrayList<List<Point>>(currentImage.getLabels().size());
        for (Polygon polygon : currentImage.getLabels()) {
            points.add(new ArrayList<Point>(polygon.getPoints()));
        }
        return points;
    }

    /**
     * Renames a polygon.
     * 
     * @param oldName the old name for the polygon
     * @param newName the new name for the polygon
     */
    public void renamePolygon(String oldName, String newName) {
        currentImage.renameLabel(oldName, newName);
    }

    /**
     * Removes a polygon from the image.
     * 
     * @param name the name of the polygon to remove
     */
    public void removePolygon(String name) {
        Polygon removedPolygon = currentImage.removeLabel(name);
        if (removedPolygon == imageController.getEditedPolygon()) {
            applicationState = ApplicationState.DEFAULT;
        }
        imagePanel.repaint();

        setMenuItemsEnabled();
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
                currentImage.setLabels(LabelIO.readLabels(loadFile));
                labelPanel.clear();
                for (String name : currentImage.getLabelNames()) {
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
        return currentImage.getLabel(name);
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
            selectedPolygons.add(currentImage.getLabel(name));
        }

        return selectedPolygons;
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

    private String getNameFromUser(String message, Collection<String> takenNames, 
            boolean allowEmpty) {
        String chosenName = null;
        boolean hasName = false;

        while (!hasName) {
            chosenName = JOptionPane.showInputDialog(appFrame, message);

            if (chosenName == null) {
                // User cancelled.
                return null;
            }

            // Strip any extension from the name.
            chosenName = LabelIO.stripExtension(chosenName.trim());
            if (chosenName.isEmpty() && !allowEmpty) {
                JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // Check for duplicates.
            hasName = true;
            for (String name : takenNames) {
                if (chosenName.equals(name)) {
                    JOptionPane.showMessageDialog(appFrame, "That name is already in use.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    hasName = false;
                }
            }
        }

        return chosenName;
    }

    /**
     * Loads the data from the settings file.
     */
    private void loadSettingsFile() {
        File settingsFile = new File(MAIN_FOLDER + "/.settings");
        if (!settingsFile.exists() || !settingsFile.canRead()) {
            // Just ignore the settings file.
            return;
        }

        String collectionName = null;
        String imageName = null;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(settingsFile));
            collectionName = in.readLine();
        } catch (IOException e) {
            // Just ignore the settings file.
            return;
        }

        try {
            imageName = in.readLine();
        } catch (IOException e) {
            // An image name isn't compulsory, so we can skip this exception.
        }

        // TODO: Check collection exists.
        currentCollectionName = collectionName;
        File collectionRoot = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName);
        collectionImages = LabelIO.openCollection(collectionRoot);
        thumbnailPanel.setImages(new ArrayList<LabelledImage>(collectionImages.values()));
        
        currentImage = collectionImages.get(imageName);
        if (currentImage != null) {
            imageController.setImage(currentImage.getImage());

            labelPanel.clear();
            for (Polygon polygon : currentImage.getLabels()) {
                labelPanel.addLabel(polygon.getName());
            }
        }
    }

    public Map<String, Polygon> getCompletedPolygons() {
        return (currentImage != null) ? currentImage.getLabelsMap() : null;
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

    public void selectPolygon(Polygon polygon) {
        labelPanel.selectPolygon(polygon.getName());
    }
        
    /**
     * Sets whether or not different menu items should be enabled.
     */
    private void setMenuItemsEnabled() {
        boolean collectionOpened = currentCollectionName != null;
        boolean collectionhasImages = collectionImages != null && collectionImages.size() > 0;
        boolean imageOpened = currentImage != null;
        boolean imageHasLabels = imageOpened && currentImage.getLabels().size() > 0;

        // File menu.
        menuBar.setCloseCollectionEnabled(collectionOpened);
        menuBar.setImportImageEnabled(collectionOpened);
        menuBar.setSaveImageEnabled(imageOpened);
        menuBar.setSaveAllImagesEnabled(collectionhasImages);
        menuBar.setRemoveImageEnabled(imageOpened);

        // Edit menu.
        menuBar.setAddPolygonEnabled(imageOpened && applicationState == ApplicationState.DEFAULT);
        menuBar.setRenamePolygonEnabled(imageHasLabels);
        menuBar.setDeleteSelectedLabelEnabled(imageHasLabels);
        menuBar.setDeleteAllLabelsEnabled(imageHasLabels);
    }

	public void showSelectedLabelTip() {
        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX() - 200, mouseLocation.getY() + 20);
        selectedLabelTip.setLocation(mouseLocation);
		selectedLabelTip.setVisible(true);
	}

	public void showNewLabelTip() {
        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX() - 200, mouseLocation.getY() + 20);
        newLabelTip.setLocation(mouseLocation);
		newLabelTip.setVisible(true);
		
	}
	
    /**
     * Imports an image from a given file into the current collection, and sets that image
     * as the currently editing image.
     *  
     * @param imageFile the file to import
     */
    private void importImageFile(File imageFile) {
        File imagesDirectory = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + 
                "/images");
        File labelsDirectory = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName + 
                "/labels");
        
        // Check for filename conflict. If so, prompt user to overwrite, rename,
        // or cancel.
        String importedImageName = LabelIO.stripExtension(imageFile.getName());
        String extension = LabelIO.getExtension(imageFile.getName());
        
        if (collectionImages.get(importedImageName) != null) {
            String[] options = { "Cancel", "Rename New Image", "Overwrite Old Image" };
            
            int result = JOptionPane.showOptionDialog(appFrame, 
                    "Cannot import image \"" + importedImageName + "\" due to duplicate name.",
                    "Unable to import image", JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
            
            if (result == 0) {
                // User cancelled.
                return;
            } else if (result == 1) {
                // User choose to rename.
                importedImageName = getNameFromUser("Image Name", collectionImages.keySet(), false);
                if (importedImageName == null) {
                    // User cancelled.
                    return;
                }
            } else {
                // User choose to overwrite.
                collectionImages.remove(importedImageName);
                thumbnailPanel.removeThumbnail(importedImageName);
                File labelFile = new File(labelsDirectory.getAbsolutePath() + "/" +
                        importedImageName + ".labels");
                if (labelFile.exists()) {
                    labelFile.delete();
                }
            }
        }

        // Copy the image into the collection folder.
        File destFile = new File(imagesDirectory.getAbsolutePath() + "/" + importedImageName + 
                extension);
        try {
            copyFile(imageFile, destFile);
        } catch (IOException e) {
            // TODO: Error
            System.err.println("IOException when importing image.");
            return;
        }
        
        // TODO: Move to IO class.
        BufferedImage importedImage;
        try {
            importedImage = ImageIO.read(destFile);
        } catch (IOException e) {
            System.err.println("error");
            return;
        }
        
        applicationState = ApplicationState.DEFAULT;
        currentImage = new LabelledImage(importedImageName + extension, importedImage);
        collectionImages.put(currentImage.getName(), currentImage);
        
        thumbnailPanel.addImage(currentImage);
        imageController.setImage(currentImage.getImage());
        labelPanel.clear();

        setMenuItemsEnabled();

        writeToSettingsFile(currentCollectionName, currentImage.getName());
    }

    /**
     * Saves a {@link LabelledImage}.
     * 
     * @param labelledImage the image to save
     */
    private boolean saveImage(LabelledImage labelledImage) {
        String labelName = labelledImage.getName() + ".labels";
        File labelFile = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName +
                "/labels/" + labelName);
        
        try {
            LabelIO.writeLabels(labelFile, labelledImage.getLabels());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
}
