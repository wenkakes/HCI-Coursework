package src.utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LabelledImage {
    private final String name;
    private final BufferedImage image;
    private List<Polygon> labels;
    
    public LabelledImage(String name, BufferedImage image) {
        this.name = name;
        this.image = image;
        this.labels = new ArrayList<Polygon>();
    }
    
    public LabelledImage(String name, BufferedImage image, List<Polygon> labels) {
        this.name = name;
        this.image = image;
        this.labels = deepCopy(labels);
    }
    
    public String getName() {
        return name;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public List<Polygon> getLabels() {
        return deepCopy(labels);
    }
    
    public void setLabels(List<Polygon> labels) {
        this.labels = labels;
    }
    
    public void addLabel(Polygon label) {
        labels.add(label);
    }

    private List<Polygon> deepCopy(List<Polygon> inList) {
        List<Polygon> copy = new ArrayList<Polygon>(inList.size());
        for (Polygon polygon : inList) {
            // No need to deep copy, points are immutable.
            List<Point> points = new ArrayList<Point>(polygon.getPoints());
            copy.add(new Polygon(polygon.getName(), points));
        }
        
        return copy;
    }
}
