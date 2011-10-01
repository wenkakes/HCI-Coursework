package src;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class HelpPicPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage helpImage;
	
	public HelpPicPanel() {
        try
        {               
            helpImage = ImageIO.read(new File("hci/images/help.png"));
        }
        catch (IOException e)
        {
            //Not handled.
        }
        Dimension bestSize = new Dimension(helpImage.getWidth(), helpImage.getHeight());
        this.setMinimumSize(bestSize);
        this.setMaximumSize(bestSize);
        this.setPreferredSize(bestSize);
        this.setSize(bestSize);
	}

	
	public void paintComponent(Graphics g) {
		g.drawImage(helpImage, 0, 0, null);
	}
	
}
