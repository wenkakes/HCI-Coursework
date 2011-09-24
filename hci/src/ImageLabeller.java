package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImageLabeller { 
    
  public static void main(final String argv[]) {
	  SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    JFrame f = new JFrame();
		    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    
		    JPanel contentPane = new JPanel();
		    f.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		    f.setContentPane(contentPane);
		    
		    final ImagePanel imagePanel = new ImagePanel(argv[0]);
		    f.add(imagePanel);
		    
		    //create toolbox panel
		    JPanel toolboxPanel = new JPanel();

		    //Add button
		    JButton newPolyButton = new JButton("New object");
		    newPolyButton.setMnemonic(KeyEvent.VK_N);
		    newPolyButton.setSize(50, 20);
		    newPolyButton.setEnabled(true);
		    newPolyButton.addActionListener(new ActionListener() {
		    	@Override
		    	public void actionPerformed(ActionEvent e) {
		    		imagePanel.addNewPolygon();
		    	}
		    });
		    newPolyButton.setToolTipText("Click to add new object");

		    toolboxPanel.add(newPolyButton);

		    f.add(toolboxPanel);
		    
		    f.pack();
		    f.setVisible(true);
		}
	});
  }
}
