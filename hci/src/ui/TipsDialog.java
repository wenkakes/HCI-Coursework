package src.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import src.nonui.AppController;

public class TipsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private AppController controller;
	private String tipText;
	
    public TipsDialog(JFrame parentFrame, AppController appController, TipType tipType) {
        super(parentFrame);

        this.controller = appController;
        initUI(tipType);
    }

    /**
     * Sets up the tip dialog.
     * 
     * @param tipType the type of tip to show text for
     */
    private void initUI(TipType tipType) {
    	setLayout(new GridLayout(2, 1));

    	JPanel panel = new JPanel(); 
    	
    	switch (tipType) {
    		case SELECTED_LABEL:
    			tipText = "<html><center>You have selected a label. <br /><br />" +
    			"Click and drag the points to move them around, or <br />" +
    			"click on the lines between points to add more. <br />" +
    			"To rename the label, click the 'rename' button below the list." +
    			"</center><html>";
    			break;
    			
    		case CREATED_LABEL:
    			tipText = "<html><center>You've just made a label. <br /><br />" +
    			"You can select this label by clicking on it in the label list.<br />" +
    			"By selecting the label, you can change its points or name. </center></html>";
    			break;
    			
    		default:
    			// Stuff goes here
    			break;
    	}
    	
    	JLabel instructions = new JLabel(tipText);
    	
    	panel.add(instructions);
    	
    	add(panel);
    	add(createButtonPanel());
 	    	
        setFocusableWindowState(false);
        setResizable(false);
        setVisible(false);
        pack();
    	
	}
    
    /**
     * Creates the button panel for the dialog.
     */
    private JPanel createButtonPanel() {
    	JPanel buttons = new JPanel();
    	
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setVisible(false);
            }
        });

        final JButton cancelButton = new JButton("Don't show me tips");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	controller.setTipsEnabled(false);
                setVisible(false);
            }
        });

        buttons.add(okButton);
        buttons.add(cancelButton);
        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        return buttons;
    }

    /**
     * An enum for the different types of tips.
     */
    public enum TipType {
        SELECTED_LABEL,
        CREATED_LABEL
    }
}