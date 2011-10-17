package src.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import src.nonui.AppController;
import src.utils.LabelledImage;

public class ThumbnailView extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private static int COMPONENT_HEIGHT = 130;
    private static int BUTTON_WIDTH = 40;
    private static int THUMBNAIL_WIDTH = 175;
    
    private AppController appController;

    private FilmStrip middle;
    
    public ThumbnailView(AppController appController) {
        super();
        
        this.appController = appController;

        Dimension buttonSize = new Dimension(BUTTON_WIDTH, COMPONENT_HEIGHT);
        Dimension middleSize = new Dimension(700,COMPONENT_HEIGHT);

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
    
    public void addImage(LabelledImage image) {
        middle.addImage(image.getName(), image.getImage());
    }

    public void setImages(List<LabelledImage> labelledImages) {
        clear();
        for (LabelledImage labelledImage : labelledImages) {
            middle.addImage(labelledImage.getName(), labelledImage.getImage());
        }
    }
    
    public void clear() {
        middle.clear();
    }

    public void setImage(String name) {
        appController.setCurrentImage(name);
    }

    public void removeThumbnail(String name) {
        middle.removeThumbnail(name);
    }

    public void setThumbnailImage(String name) {
        middle.setThumbnailImage(name);
    }
    
    private class FilmStrip extends JPanel implements MouseListener {
        private static final long serialVersionUID = 1L;
        
        // Storage for the thumbnails.
        private final Map<String, BufferedImage> thumbnails;
        private int index = 0;
        private int selectedIndex = -1;
        
        public FilmStrip() {
            super();
            
            // Use a linked hashmap to preserve the ordering.
            thumbnails = new LinkedHashMap<String, BufferedImage>();
            
            this.addMouseListener(this);
        }

        public void setThumbnailImage(String imageName) {
            int index = 0;
            for (String name : thumbnails.keySet()) {
                if (name.equals(imageName)) {
                    break;
                }
                index++;
            }
            
            selectedIndex = index;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            List<BufferedImage> images = new ArrayList<BufferedImage>(thumbnails.values());
            int xcoord = 0;
            for (int i = index; i < index + 4 && i < images.size(); i++) {
                g.drawImage(images.get(i), xcoord, 0, null);
                
                // Border.
                if (i == selectedIndex) {
                    Graphics2D graphics2d = (Graphics2D) g;
                    Stroke originalStroke = graphics2d.getStroke();
                    graphics2d.setStroke(new BasicStroke(5.0f));
                    g.setColor(Color.RED);
                    g.drawRect(xcoord, 0, THUMBNAIL_WIDTH, COMPONENT_HEIGHT);
                    graphics2d.setStroke(originalStroke);
                } else {
                    g.setColor(Color.BLACK);
                    g.drawRect(xcoord, 0, THUMBNAIL_WIDTH, COMPONENT_HEIGHT);
                }
                
                xcoord += THUMBNAIL_WIDTH;
            }

            int remainingWidth = this.getWidth() - xcoord;
            g.setColor(Color.gray);
            g.fillRect(xcoord, 0, remainingWidth, COMPONENT_HEIGHT);
        }

        public void addImage(String name, BufferedImage image) {
            // Scale image to have a height of COMPONENT_HEIGHT and a width of 175.
            int width = image.getWidth();
            int height = image.getHeight();

            if (width > THUMBNAIL_WIDTH || height > COMPONENT_HEIGHT) {
                width = (width > THUMBNAIL_WIDTH) 
                        ? THUMBNAIL_WIDTH 
                        : ((width * COMPONENT_HEIGHT) / height);
                height = (height > COMPONENT_HEIGHT)
                        ? COMPONENT_HEIGHT
                        : (height * THUMBNAIL_WIDTH) / width;
            } else if (width < THUMBNAIL_WIDTH || height < COMPONENT_HEIGHT) {
                width = (width < THUMBNAIL_WIDTH)
                        ? THUMBNAIL_WIDTH
                        : ((width * height) / COMPONENT_HEIGHT);
                height = (height < COMPONENT_HEIGHT)
                        ? COMPONENT_HEIGHT
                        : (height * width) / THUMBNAIL_WIDTH;
            }
            
            BufferedImage imageThumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
            imageThumbnail.getGraphics().drawImage(scaledImage, 0, 0, this);
            
            thumbnails.put(name, imageThumbnail);
            selectedIndex = thumbnails.size() - 1;
            
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

        public void clear() {
            index = 0;
            thumbnails.clear();
            
            repaint();
        }

        public void removeThumbnail(String thumbnailName) {
            int index = 0;
            for (String name : thumbnails.keySet()) {
                if (name.equals(thumbnailName)) {
                    break;
                }
                index++;
            }
            
            if (selectedIndex == index) {
                selectedIndex = -1;
            }
            
            thumbnails.remove(thumbnailName);
            
            repaint();
        }

        private boolean withinBounds(int x, int y) {
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
            int imageIndex = x / THUMBNAIL_WIDTH + index;
            if (imageIndex >= thumbnails.size()) {
                return;
            }
            
            // Grab the name.
            List<String> names = new ArrayList<String>(thumbnails.keySet());
            setImage(names.get(imageIndex));
            
            selectedIndex = imageIndex;
            repaint();
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
