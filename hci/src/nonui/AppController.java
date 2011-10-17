package src.nonui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import src.ui.ImagePanelView;
import src.ui.LabelPanelView;
import src.ui.MenuBarView;
import src.ui.ThumbnailView;
import src.ui.TipsDialog;
import src.ui.TipsDialog.TipType;
import src.ui.ToolboxPanelView;
import src.utils.ApplicationIO;
import src.utils.ApplicationIO.LabelParseException;
import src.utils.LabelledImage;
import src.utils.Point;
import src.utils.Polygon;

/**
 * Main controller class for the application.
 */
public class AppController {
    // The main folder for the application.
    private static final String MAIN_FOLDER = System.getProperty("user.home") + "/ImageLabeller";
    
    private static final String NO_COLLECTION = "Please create or open a collection.";
    private static final String NO_IMAGES = "Please import an image to label.";

    // The application frame.
    private final JFrame appFrame = new JFrame("Image Labeller");
    
    // The sub-controllers.
    private final ImageController imageController = new ImageController(this, appFrame);

    // The core Swing components that make up the application.
    private final ImagePanelView imagePanel = new ImagePanelView(imageController);
    private final MenuBarView menuBar = new MenuBarView(appFrame, this);
    private final LabelPanelView labelPanel = new LabelPanelView(appFrame, this);
    private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(appFrame, this);
    private final TipsDialog selectedLabelTip = new TipsDialog(appFrame, this, 
            TipType.SELECTED_LABEL);
    private final TipsDialog newLabelTip = new TipsDialog(appFrame, this, TipType.CREATED_LABEL);
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
        setUIComponentsState();

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
        File directories[] = collectionsDir.listFiles(ApplicationIO.DIRECTORY_FILTER);
        

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
        collectionImages = new LinkedHashMap<String, LabelledImage>();
        
        // Reset the interface.
        imageController.setImage(null);
        labelPanel.disableLabelPanel();
        thumbnailPanel.clear();
        setUIComponentsState();
        
        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, currentCollectionName, "");
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
        
        setUIComponentsState();
        
        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, "", "");
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

        File collections[] = collectionsDir.listFiles(ApplicationIO.DIRECTORY_FILTER);
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
        collectionImages = ApplicationIO.openCollection(
                new File(MAIN_FOLDER + "/Collections/" + currentCollectionName));
        currentImage = (collectionImages.size() > 0) ? getLastCollectionImage() : null; 

        thumbnailPanel.setImages(new ArrayList<LabelledImage>(collectionImages.values()));
        if (collectionImages.size() > 0) {
            setCurrentImage(currentImage.getName());
        } else {
            imageController.setImage(null);
        }
        
        setUIComponentsState();

        // TODO: Provide a way for user to open a default image?
        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, currentCollectionName, "");
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
            try {
                ApplicationIO.saveImage(MAIN_FOLDER, currentCollectionName, currentImage);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(appFrame, "Unable to save image.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(appFrame, "The current image was saved.", 
                    "Images Saved", JOptionPane.INFORMATION_MESSAGE);
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
            try {
                ApplicationIO.saveImage(MAIN_FOLDER, currentCollectionName, labelledImage);
            } catch (IOException e) {
                // Just send one warning for multiple failed images.
                savedOkay = false;   
            } 
        }
        
        if (savedOkay) {
            JOptionPane.showMessageDialog(appFrame, "All images were saved.", "Images Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(appFrame, "Error while trying to save images. " 
                    + "Some images may not have been saved.", "Error", JOptionPane.ERROR_MESSAGE);
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
        
        setUIComponentsState();
        
        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, currentCollectionName, "");
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
     * Renames a polygon.
     * 
     * @param oldName the old name for the polygon
     * @param newName the new name for the polygon
     */
    public void renamePolygon(String oldName, String newName) {
        currentImage.renameLabel(oldName, newName);
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
    
    /**
     * Opens one of the collection images to the main editor panel.
     * 
     * @param name the name of the image to open
     */
    public void setCurrentImage(String name) {
        applicationState = ApplicationState.DEFAULT;
        
        currentImage = collectionImages.get(name);
        if (currentImage == null) {
            return;
        }
        
        imageController.setImage(currentImage.getImage());
        thumbnailPanel.setThumbnailImage(currentImage.getName());
        labelPanel.clear();
        for (Polygon polygon : currentImage.getLabels()) {
            labelPanel.addLabel(polygon.getName());
        }
        
        setUIComponentsState();
        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, currentCollectionName, 
                currentImage.getName());
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

        setUIComponentsState();
    }

    /**
     * Imports a list of labels from a file.
     * 
     * @param file the file to load from
     */
    public void importLabels() {
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(appFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File loadFile = chooser.getSelectedFile();

            try {
                currentImage.setLabels(ApplicationIO.readLabels(loadFile));
                labelPanel.clear();
                for (String name : currentImage.getLabelNames()) {
                    labelPanel.addLabel(name);
                }
                imagePanel.repaint();
            } catch (LabelParseException e) {
                JOptionPane.showMessageDialog(appFrame, "Unable to parse chosen file.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        setUIComponentsState();
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

    /**
     * Undoes the last added vertex.
     */
    public void undoLastVertex() {
        imageController.undo();
    }

    /**
     * Redoes the last "undone" vertex.
     */
    public void redoLastVertex() {
        imageController.redo();
    }

    /**
     * Called when the user is finished adding the current polygon.
     * 
     * @param newPolygonName the name of the new polygon
     */
    public void finishedAddingPolygon(String newPolygonName) {
        applicationState = ApplicationState.DEFAULT;

        labelPanel.setAddButtonEnabled(true);
        labelPanel.addLabel(newPolygonName);

        toolboxPanel.setVisible(false);

        setUIComponentsState();
    }

    /**
     * Called when the user cancels the polygon that they are currently adding.
     */
    public void cancelAddingPolygon() {
        applicationState = ApplicationState.DEFAULT;

        labelPanel.setAddButtonEnabled(true);
        toolboxPanel.setVisible(false);

        imageController.cancel();
    }

    /**
     * Highlights the currently selected labels on the main image.
     */
    public void highlightSelected() {
        // Selecting a label normally resets any editing the user is doing,
        // unless they have re-selected a label that was already selected.
        if (applicationState == ApplicationState.EDITING_POLYGON
                && !getSelectedNames().contains(imageController.getEditedPolygon().getName())) {
            applicationState = ApplicationState.DEFAULT;
        }

        // Calling repaint forces the ImageController to fetch the currently selected
        // labels and draw them on.
        imagePanel.repaint();
    }
    
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

    // TODO: Remove the call to this.
    /**
     * Returns the map of the completed polygons.
     */
    public Map<String, Polygon> getCompletedPolygons() {
        return (currentImage != null) ? currentImage.getLabelsMap() : null;
    }

    /**
     * Returns the names of the currently selected labels.
     */
    public List<String> getSelectedNames() {
        return labelPanel.getSelectedNames();
    }

    /**
     * Returns the current application state.
     */
    public ApplicationState getApplicationState() {
        return applicationState;
    }

    /**
     * Sets the current application state.
     * 
     * @param applicationState the state to set
     */
    public void setApplicationState(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    /**
     * Shows the tooltip for when the user selects a label.
     */
	public void showSelectedLabelTip() {
        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX() - 200, mouseLocation.getY() + 20);
        selectedLabelTip.setLocation(mouseLocation);
		selectedLabelTip.setVisible(true);
	}

	/**
	 * Shows the tooltip for when the user adds a new label.
	 */
	public void showNewLabelTip() {
        java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        mouseLocation.setLocation(mouseLocation.getX() - 200, mouseLocation.getY() + 20);
        newLabelTip.setLocation(mouseLocation);
		newLabelTip.setVisible(true);
		
	}
	
    /**
     * Requests a name from the user, disallowing a set of "taken" names and
     * disallowing empty names as well if requested.
     * 
     * @param message the message to display to the user when asking for the name
     * @param takenNames the set of disallowed choices
     * @param allowEmpty whether or not to allow empty names
     */
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
            chosenName = ApplicationIO.stripExtension(chosenName.trim());
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
        String importedImageName = ApplicationIO.stripExtension(imageFile.getName());
        String extension = ApplicationIO.getExtension(imageFile.getName());
        
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
            ApplicationIO.copyFile(imageFile, destFile);
        } catch (IOException e) {
            // TODO: Error
            System.err.println("Unable to import image.");
            return;
        }
        
        BufferedImage importedImage;
        try {
            importedImage = ImageIO.read(destFile);
        } catch (IOException e) {
            // TODO: Error better.
            System.err.println("Unable to import image.");
            return;
        }
        
        applicationState = ApplicationState.DEFAULT;
        currentImage = new LabelledImage(importedImageName + extension, importedImage);
        collectionImages.put(currentImage.getName(), currentImage);
        
        thumbnailPanel.addImage(currentImage);
        imageController.setImage(currentImage.getImage());
        labelPanel.clear();

        setUIComponentsState();

        ApplicationIO.writeToSettingsFile(MAIN_FOLDER, currentCollectionName, 
                currentImage.getName());
    }

    /**
     * Loads the data from the settings file.
     */
    private void loadSettingsFile() {
        // Oh what I would do for a Pair class in Java...
        List<String> collectionInformation = ApplicationIO.loadSettingsFile(MAIN_FOLDER);
        if (collectionInformation == null) {
            // Failed to read settings file - just ignore it.
            return;
        }

        // TODO: Check collection exists.
        currentCollectionName = collectionInformation.get(0);
        File collectionRoot = new File(MAIN_FOLDER + "/Collections/" + currentCollectionName);
        collectionImages = ApplicationIO.openCollection(collectionRoot);
        thumbnailPanel.setImages(new ArrayList<LabelledImage>(collectionImages.values()));
        
        currentImage = collectionImages.get(collectionInformation.get(1));
        // If the settings file didn't specify an image, try and load a default.
        if (currentImage == null && collectionImages.size() > 0) {
            currentImage = getLastCollectionImage();
        }
        
        if (currentImage != null) {
            setCurrentImage(currentImage.getName());

            labelPanel.clear();
            for (Polygon polygon : currentImage.getLabels()) {
                labelPanel.addLabel(polygon.getName());
            }
        }
    }
     
    /**
     * Helper method to get the last element of the collection. Makes some sense to iterate over
     * a map in this case because we use a LinkedHashMap.
     */
    private LabelledImage getLastCollectionImage() {
        LabelledImage image = null;
        Iterator<Entry<String, LabelledImage>> i = collectionImages.entrySet().iterator();
        while (i.hasNext()) {
            image = i.next().getValue();
        }
        
        return image;
    }

    /**
     * Sets the state of various UI components.
     */
    private void setUIComponentsState() {
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
        
        // Image default text.
        if (!collectionOpened) {
            imageController.setDefaultText(NO_COLLECTION);
        } else {
            imageController.setDefaultText(NO_IMAGES);
        }
    }
}
