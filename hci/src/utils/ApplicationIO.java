package src.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles application-related IO, including writing labels to a file and reading them
 * back.
 */
public final class ApplicationIO {
    public static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile();
        }
    }; 
    public static final FileFilter DIRECTORY_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }; 

    private ApplicationIO() {
        // Non-instantiable.
    }
    

    /**
     * Saves a {@link LabelledImage}.
     * 
     * @param labelledImage the image to save
     */
    public static void saveImage(String rootDirectory, String collectionName, 
            LabelledImage labelledImage) throws IOException {
        String labelName = labelledImage.getName() + ".labels";
        File labelFile = new File(rootDirectory + "/Collections/" + collectionName +
                "/labels/" + labelName);
        
        writeLabels(labelFile, labelledImage.getLabels());
    }
    
    /**
     * Copies a file from one location to another.
     * 
     * @param sourceFile the source file to copy from
     * @param destFile the destination file to copy to
     * 
     * @throws IOException if an IO-related exception occurs while trying to copy
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;

        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
    
    /**
     * Writes a given collection name and image name to the settings file.
     * 
     * @param rootDirectory the location of the root ImageLabeller directory
     * @param collectionName the name of the collection to write
     * @param imageName the name of the image to write
     */
    public static boolean writeToSettingsFile(String rootDirectory, String collectionName, 
            String imageName) {
        File settingsFile = new File(rootDirectory + "/.settings");
        if (!settingsFile.canWrite()) {
            // TODO: Log better.
            System.err.println("Cannot write to Settings file.");
            return false;
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile, false));

            out.write(collectionName);
            if (!collectionName.isEmpty()) {
                out.newLine();
                out.write(imageName);
                if (!imageName.isEmpty()) {
                    out.newLine();
                }
            }
            out.close();
        } catch (IOException e) {
            // TODO: Log better
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Loads data from the setting file, returning a pair of the collection
     * name and (optionally) the image name.
     * 
     * @param rootDirectory the location of the root ImageLabeller directory
     * 
     * @return a list of the collection name and either the image name if it
     *         exists, or null otherwise
     */
    public static List<String> loadSettingsFile(String rootDirectory) {
        File settingsFile = new File(rootDirectory + "/.settings");
        if (!settingsFile.exists() || !settingsFile.canRead()) {
            return null;
        }
        
        String collectionName;
        String imageName;

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(settingsFile));
            collectionName = in.readLine();
        } catch (IOException e) {
            return null;
        }

        try {
            imageName = in.readLine();
        } catch (IOException e) {
            // An image name isn't compulsory, so we can ignore this exception.
            imageName = null;
        }
        
        List<String> collectionInformation = new ArrayList<String>(2);
        collectionInformation.add(collectionName);
        collectionInformation.add(imageName);
        
        return collectionInformation;
    }

    public static Map<String, LabelledImage> openCollection(File collectionRoot) {
        Map<String, LabelledImage> collectionEntries = new LinkedHashMap<String, LabelledImage>();
        
        File imageDir = new File(collectionRoot.getAbsolutePath() + "/images");
        File labelsDir = new File(collectionRoot.getAbsolutePath() + "/labels");
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        };
        
        File[] imageFiles = imageDir.listFiles(fileFilter);
        File[] labelFiles = labelsDir.listFiles(fileFilter);
        
        if (imageFiles == null || imageFiles.length == 0) {
            return collectionEntries;
        }
        
        for (int i = 0; i < imageFiles.length; i++) {
            File imageFile = imageFiles[i];
            String imageName = stripExtension(imageFile.getName());
            String extension = getExtension(imageFile.getName());
            
            BufferedImage image;
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
                System.err.println("Unable to load image file: " + imageFile.getName());
                continue;
            }
            
            Map<String, Polygon> labels = null;
            for (int j = 0; j < labelFiles.length; j++) {
                File labelFile = labelFiles[j];
                if (imageName.equals(stripExtension(labelFile.getName()))) {
                    try {
                        labels = ApplicationIO.readLabels(labelFile);
                    } catch (LabelParseException e) {
                        System.err.println("Unable to read labels");
                    }
                    break;
                }
            }
            
            LabelledImage labelledImage;
            if (labels != null) {
                labelledImage = new LabelledImage(imageName + extension, image, 
                        new ArrayList<Polygon>(labels.values()));
            } else {
                labelledImage = new LabelledImage(imageName + extension, image);
            }
            
            collectionEntries.put(imageName, labelledImage);
        }
        
        return collectionEntries;
    }

    /**
     * Writes out a list of {@link Polygon}s to a specified file. The output
     * file is written in XML.
     * 
     * @param file the file to write to
     * @param polygons the polygons that are to be recorded in the file
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    public static void writeLabels(File file, List<Polygon> polygons) throws IOException {
        StreamResult result = new StreamResult(new FileWriter(file));

        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            // If this happens without us actually configuring the parser, the
            // world is doomed anyway.
            throw new IllegalStateException();
        }
        Node mainNode = document.createElement("ImageLabels");

        for (Polygon polygon : polygons) {
            Node polygonNode = document.createElement("Label");
            Node nameNode = document.createElement("Name");
            nameNode.setTextContent(polygon.getName());

            Node pointsNode = document.createElement("Points");
            for (Point point : polygon.getPoints()) {
                Node pointNode = document.createElement("Point");

                Node xNode = document.createElement("x");
                Node yNode = document.createElement("y");
                xNode.setTextContent(Integer.toString(point.getX()));
                yNode.setTextContent(Integer.toString(point.getY()));

                pointNode.appendChild(xNode);
                pointNode.appendChild(yNode);

                pointsNode.appendChild(pointNode);
            }

            polygonNode.appendChild(nameNode);
            polygonNode.appendChild(pointsNode);

            mainNode.appendChild(polygonNode);
        }

        document.appendChild(mainNode);

        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerFactoryConfigurationError e) {
            // As above - no configuration has been done, so we're doomed if
            // this is thrown.
            throw new IllegalStateException();
        } catch (TransformerConfigurationException e) {
            // As above - no configuration has been done, so we're doomed if
            // this is thrown.
            throw new IllegalStateException();
        }

        /*
         * Uncomment the following to pretty up the XML. Not turned on by
         * default as parsing whitespace filled XML in Java requires that you
         * set up a schema (or it reads in the blank whitespace nodes), and I'm
         * not that dedicated -_-.
         */
        // transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        // "2");

        DOMSource source = new DOMSource(document);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            // It's not really an IO exception, but close enough.
            throw new IOException(e.getMessage());
        }

        result.getWriter().close();
    }

    /**
     * Reads in an XML document from a specified file, and returns a hashmap of
     * polygon names to {@link Polygon}s, extracted from the file.
     * 
     * @param file the file to read the data from
     * 
     * @throws LabelParseException if an error occurs while parsing
     */
    public static Map<String, Polygon> readLabels(File file) throws LabelParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;

        try {
            document = factory.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            // Naughty to catch plain Exception, but we are only currently
            // interested in giving the user a "bad parse" error.
            throw new LabelParseException("Unable to parse file.");
        }

        NodeList labels = document.getDocumentElement().getChildNodes();

        Map<String, Polygon> polygons = new HashMap<String, Polygon>();

        for (int i = 0; i < labels.getLength(); i++) {
            Node label = labels.item(i);

            NodeList labelNodes = label.getChildNodes();
            if (labelNodes.getLength() != 2) {
                throw new LabelParseException("Unable to parse file.");
            }

            String name = labelNodes.item(0).getTextContent();

            NodeList pointsNodes = labelNodes.item(1).getChildNodes();
            if (pointsNodes.getLength() < 1) {
                throw new LabelParseException("Unable to parse file.");
            }

            List<Point> points = new ArrayList<Point>(pointsNodes.getLength());
            for (int index = 0; index < pointsNodes.getLength(); index++) {
                Node pointNode = pointsNodes.item(index);

                int x;
                int y;

                try {
                    x = Integer.parseInt(pointNode.getChildNodes().item(0).getTextContent());
                    y = Integer.parseInt(pointNode.getChildNodes().item(1).getTextContent());
                } catch (NumberFormatException nfe) {
                    throw new LabelParseException("Unable to parse file.");
                }

                points.add(new Point(x, y));
            }

            polygons.put(name, new Polygon(name, points));
        }

        return polygons;
    }

    public static String stripExtension(String name) {
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex < 0) {
            return name;
        }
        return name.substring(0, extensionIndex);
    }

    public static String getExtension(String name) {
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex < 0) {
            return "";
        }
        return name.substring(extensionIndex, name.length());
    }

    /**
     * Represents an error that occurs while parsing a label file.
     */
    public static class LabelParseException extends ParseException {
        private static final long serialVersionUID = 1L;

        public LabelParseException(String message) {
            super(message, -1);
        }
    }
}
