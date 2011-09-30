package src.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Handles label-related IO, including writing labels to a file and reading them
 * back.
 */
public final class LabelIO {

    private LabelIO() {
        // Non-instantiable.
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

    /**
     * Represents an error that occurs while parsing the label file.
     */
    public static class LabelParseException extends ParseException {
        private static final long serialVersionUID = 1L;

        public LabelParseException(String message) {
            super(message, -1);
        }
    }
}
