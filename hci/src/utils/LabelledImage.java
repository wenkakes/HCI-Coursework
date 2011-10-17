package src.utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelledImage {
    private final String name;
    private final String imageExtension;
    private final BufferedImage image;
    private Map<String, Polygon> labels;
    
    public LabelledImage(String name, BufferedImage image) {
        this.name = LabelIO.stripExtension(name);
        this.imageExtension = LabelIO.getExtension(name);
        this.image = image;
        this.labels = new HashMap<String, Polygon>();
    }
    
    public LabelledImage(String name, BufferedImage image, List<Polygon> labels) {
        this.name = LabelIO.stripExtension(name);
        this.imageExtension = LabelIO.getExtension(name);
        this.image = image;
        this.labels = listToMap(labels);
    }

    public String getName() {
        return name;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public List<Polygon> getLabels() {
        return mapToList(labels);
    }

    public List<String> getLabelNames() {
        return new ArrayList<String>(labels.keySet());
    }

    public void setLabels(Map<String, Polygon> labels) {
        // TODO: Deep copy?
        this.labels = labels;
    }
    
    public void setLabels(List<Polygon> labels) {
        this.labels = listToMap(labels);
    }
    
    public void addLabel(Polygon label) {
        // TODO: Deep copy?
        labels.put(label.getName(), label);
    }

    public void renameLabel(String oldName, String newName) {
        Polygon polygon = labels.remove(oldName);
        if (polygon != null) {
            polygon.setName(newName);
            labels.put(newName, polygon);
        }
    }

    public Polygon removeLabel(String name) {
        return labels.remove(name);
    }

    public Polygon getLabel(String name) {
        // TODO: Deep copy?
        return labels.get(name);
    }
    
    private static Map<String, Polygon> listToMap(List<Polygon> list) {
        Map<String, Polygon> map = new HashMap<String, Polygon>(list.size());
        for (Polygon polygon : list) {
            String polygonName = polygon.getName();
            // No need to deep copy this, points are immutable.
            List<Point> points = new ArrayList<Point>(polygon.getPoints());
            map.put(polygonName, new Polygon(polygonName, points));
        }
        return map;
    }
    
    private static List<Polygon> mapToList(Map<String, Polygon> map) {
        List<Polygon> list = new ArrayList<Polygon>(map.size());
        for (Polygon polygon : map.values()) {
            // No need to deep copy this, points are immutable.
            List<Point> points = new ArrayList<Point>(polygon.getPoints());
            list.add(new Polygon(polygon.getName(), points));
        }
        return list;
    }

    public Map<String, Polygon> getLabelsMap() {
        // TODO: Temporary method.
        return labels;
    }

    public String getExtension() {
        return imageExtension;
    }
}
