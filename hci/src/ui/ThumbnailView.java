package src.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import src.nonui.AppController;

public class ThumbnailView extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private AppController appController;

    private FilmStrip middle;
    
    public ThumbnailView(AppController appController) {
        super();
        
        this.appController = appController;

        Dimension buttonSize = new Dimension(40, 100);
        Dimension middleSize = new Dimension(700,100);

        JButton left = new JButton("<");
        left.setPreferredSize(buttonSize);
        left.setMaximumSize(buttonSize);
        left.setMinimumSize(buttonSize);
        left.setSize(buttonSize);
        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                middle.left();
            }
        });

        middle = new FilmStrip();
        middle.setPreferredSize(middleSize);
        middle.setMaximumSize(middleSize);
        middle.setMinimumSize(middleSize);
        middle.setSize(middleSize);

        JButton right = new JButton(">");
        right.setPreferredSize(buttonSize);
        right.setMaximumSize(buttonSize);
        right.setMinimumSize(buttonSize);
        right.setSize(buttonSize);
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                middle.right();
            }
        });
        
        this.add(left);
        this.add(middle);
        this.add(right);
        
        setVisible(true);
    }
    
    public void addImage(BufferedImage image) {
        middle.addImage(image);
    }
    
    private class FilmStrip extends JPanel implements MouseListener {
        private static final long serialVersionUID = 1L;
        
        // Storage for the thumbnails. Will probably be very memory hungry, but meh.
        private final List<BufferedImage> images;
        private int index = 0;
        
        public FilmStrip() {
            super();
            
            images = new ArrayList<BufferedImage>();
            
            this.addMouseListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int xcoord = 0;
            for (int i = index; i < index + 4 && i < images.size(); i++) {
                g.drawImage(images.get(i), xcoord, 0, null);
                xcoord += 175;
            }

            int remainingWidth = this.getWidth() - xcoord;
            g.setColor(Color.gray);
            g.fillRect(xcoord, 0, remainingWidth, 100);
        }

        public void addImage(BufferedImage image) {
            // Scale image to have a width of 175.
            int width = image.getWidth();
            int height = image.getHeight();

            if (width > 175) {
                height = (height * 175) / width;
                width = 175;
            } else if (width < 175) {
                height = (height * width) / 175;
                width = 175;
            }
            
            BufferedImage imageThumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
            imageThumbnail.getGraphics().drawImage(scaledImage, 0, 0, this);
            
            images.add(imageThumbnail);
            
            repaint();
        }
        
        public void right() {
            index++;
            repaint();
        }

        public void left() {
            if (index > 0) {
                index--;
            }
            repaint();
        }

        public boolean withinBounds(int x, int y) {
            return x >= 0 && x <= this.getWidth() && y >= 0 && y <= this.getHeight();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (e.getButton() != MouseEvent.BUTTON1 || !withinBounds(x, y)) {
                return;
            }
            
            // Work out which image was clicked on:
            int imageIndex = x / 175 + index;
            if (imageIndex >= images.size()) {
                return;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Ignore.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Ignore.
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Ignore.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Ignore.
        }
    }
}
