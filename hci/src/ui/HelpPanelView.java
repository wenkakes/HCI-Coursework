package src.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * View for the help panel.
 * 
 * Actually a subclass of JDialog, in order to get the right interaction with
 * the main frame.
 */
public class HelpPanelView extends JDialog implements ActionListener {
    // JDialog is serializable, so we need some ID to avoid compiler warnings.
    private static final long serialVersionUID = 1L;

    public HelpPanelView(JFrame parentFrame) {
        super(parentFrame);

        JEditorPane editor = new JEditorPane(
            "text/html",
            "<H1>How To Use</H1>" + 
            "<H2><b>Opening and Saving Images</b></H2><br /><br /><u>How do I open an image?</u><br /> " +
            "A <u>collection</u> is a handy way for you to store and label your images. " +
            "To start a new collection, go to File > New Collection.<br /><br />" +
            "You can scroll through the images in the collection using the <u>filmstrip</u> at the bottom " +
            "of the application window.<br /><br />To add images to your collection, go to File > Import Image" +
            "<br /><br /><u>How do I save an image?</u><br />" +
            "You can save the images you have labelled by going to File > Save Image or by pressing Ctrl-S.<br />" +
            "<br /><H2><b>Labelling New Objects</b></H2><br />" +
            "<u>How do I label a new object?</u><br />" +
            "Click the Add New Label (the '+') button in the Labels Panel. This will enable the Toolbox. " +
            "<br />Click anywhere in the Image Area to draw points around the object. " +
            "If you make a mistake, you can use the \"undo\", \"redo\", or \"cancel\" buttons in the Toolbox. " +
            "<br />To finish drawing, you can click the \"ok\" button in the Toolbox, the first point of " +
            "your object, or double click.<br /><br /><H2><b>Editing and Deleting Objects</b></H2><br />" +
            "<u>How do I select an object?</u>" +
            "<br />You can select an object by clicking on its name in the Toolbox. " +
            "You can select multiple objects by clicking on more than one label.<br /><br />" +
            "<u>How do I rename an object?</u> " +
            "<br />You can rename your object by first selecting it, then clicking the Rename button " +
            "(the pencil and paper) in the Toolbox.<br /><br />" +
            "<u>How do I move points in an object?</u><br />" +
            "You can move points by first selecting the object, then clicking on the point and dragging it" +
            " to a new location.<br /><br /><u>How do I add points to an object?</u><br />" +
            "You can add points by first selecting the object, then clicking anywhere on the line between " +
            "two points where you want this new point to be.");
        editor.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editor);
        add(scrollPane, BorderLayout.CENTER);
        setSize(700, 500);
        setVisible(true);
    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
