package src;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class HelpDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	public static BufferedImage image;
	
	public HelpDialog() {
	
		HelpPicPanel helpPic = new HelpPicPanel();
	    JPanel buttonPane = new JPanel();
	    JButton button = new JButton("OK"); 
	    buttonPane.add(button); 
	    button.addActionListener(this);
	    getContentPane().add(helpPic, BorderLayout.CENTER);
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    pack(); 
	    setVisible(true);
	    
	}
	
	public void actionPerformed(ActionEvent e) {
		setVisible(false); 
	    dispose(); 
	}
}
