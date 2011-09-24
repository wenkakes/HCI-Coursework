package src;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class of the program - handles display of the main GUI window.
 * 
 * @author Michal
 */
public class ImageLabeller extends JFrame {
	
	// JFrame is serializable, so we need some ID to avoid compiler warnings.
	private static final long serialVersionUID = 1L;
	
	// The main application panel, ancestor of all other panels.
	JPanel appPanel = null;
	
	// Panels for the toolbox and the image being edited.
	JPanel toolboxPanel = null;
	ImagePanel imagePanel = null;
	
	public ImageLabeller(String fileName) {
		super();
		setupGUI(fileName);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		imagePanel.paint(g); //update image panel
	}
	
	/**
	 * Finishes the Polygon that is being edited, and starts a new one.
	 */
	public void addNewPolygon() {
		imagePanel.addNewPolygon();
	}
	
	/**
	 * Sets up the application window.
	 * 
	 * @param imageFilename the name of the image to be loaded for editing
	 */
	public void setupGUI(String imageFilename) {
		this.addWindowListener(new WindowAdapter() {
		  	public void windowClosing(WindowEvent event) {
		  		//here we exit the program (maybe we should ask if the user really wants to do it?)
		  		//maybe we also want to store the polygons somewhere? and read them next time
		  		System.out.println("Bye bye!");
		    	System.exit(0);
		  	}
		});

		//setup main window panel
		appPanel = new JPanel();
		this.setLayout(new BoxLayout(appPanel, BoxLayout.X_AXIS));
		this.setContentPane(appPanel);
		
        //Create and set up the image panel.
		imagePanel = new ImagePanel(imageFilename);
		imagePanel.setOpaque(true); //content panes must be opaque
		
        appPanel.add(imagePanel);

        //create toolbox panel
        toolboxPanel = new JPanel();
        
        //Add button
		JButton newPolyButton = new JButton("New object");
		newPolyButton.setMnemonic(KeyEvent.VK_N);
		newPolyButton.setSize(50, 20);
		newPolyButton.setEnabled(true);
		newPolyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	addNewPolygon();
			}
		});
		newPolyButton.setToolTipText("Click to add new object");
		
		toolboxPanel.add(newPolyButton);
		
		//add toolbox to window
		appPanel.add(toolboxPanel);
		
		//display all the stuff
		this.pack();
        this.setVisible(true);
	}
	
	/**
	 * Runs the program.
	 * 
	 * @param argv the list of arguments, of which the first should be the name of the image to
	 *             load
	 */
	public static void main(String argv[]) {
		new ImageLabeller(argv[0]);
	}
}
