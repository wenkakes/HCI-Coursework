package src.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import src.nonui.AppController;


public class TipsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private AppController controller;
	
    public TipsDialog(JFrame parentFrame, AppController appController) {
        super(parentFrame);

        this.controller = appController;
        initUI();
    }

    private void initUI() {
    	setLayout(new GridLayout(2, 1));
    	add(createInstructionsPanel());
    	add(createButtonPanel());
 	    	
        setFocusableWindowState(false);
        setResizable(false);
        setVisible(false);
        pack();
    	
	}

    protected JPanel createInstructionsPanel() {
    	
    	JPanel panel = new JPanel();
    	
    	JLabel instructions = new JLabel("<html><center>You have selected a label. <br />" +
    			"Click and drag the points to move them around, or <br />" +
    			"click on the lines between points to add more.<html>");
    	
    	panel.add(instructions);
    	
    	return panel;
    }
    
    protected JPanel createButtonPanel() {
   	
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

            	controller.setTipsOn(false);
                setVisible(false);
            }
        });

        buttons.add(okButton);
        buttons.add(cancelButton);
        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        return buttons;
    }

}