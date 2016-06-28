package az;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractGenerator
{
    private static final Logger log = Logger.getLogger(AbstractGenerator.class.getName());
    protected class Transition
    {
        String startStateId;
        String eventId;
        String endStateId;

        public Transition(String startStateId, String eventId, String endStateId)
        {
            this.startStateId = startStateId;
            this.eventId = eventId;
            this.endStateId = endStateId;
        }
    }

    static Hashtable<String, Integer> stateIndex = new Hashtable<String, Integer>();
    static Hashtable<String, Integer> eventIndex = new Hashtable<String, Integer>();
    static Hashtable<String, String> codeBlocks = new Hashtable<String, String>();
    static ArrayList<Transition> transitions = new ArrayList<Transition>();

    static String inputFile = null;
    static String CLASS_NAME = null;
    static String STATE_METHOD_PREFIX = "AZ_";
    static ArrayList<String> codeTransitions = new ArrayList<String>();
    static String initialState = null;
    static boolean makeVirtual = false;
    // Derived from Automaton? Or Automaton built in?
    // Preference is for the class to be derived from an Automaton.
    // Probably should be default, other case is not-derived.
    static boolean derived = true;

    public void init(String diagram)
    {
        inputFile = diagram;
        stateIndex.clear();
        eventIndex.clear();
        codeBlocks.clear();
        transitions.clear();
        codeTransitions.clear();
        populateIndexes();
        generateDatabase();
    }

    public void populateIndexes()
    {
        try
        {
            File fXmlFile = new File(inputFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :"
                    + doc.getDocumentElement().getNodeName());

            Element e = doc.getDocumentElement();

            initialState = e.getAttribute("initial");

            NodeList nList = doc.getElementsByTagName("state");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    String stateID = eElement.getAttribute("id");

                    Integer exists = stateIndex.get(stateID);

                    if (exists == null)
                    {
                        stateIndex.put(stateID, stateIndex.keySet().size() + 1);
                    }

                    System.out.println("State id: " + stateID);

                    // If there is an onentry attribute, add it's value to the
                    // database.
                    NodeList onEntryNodes = eElement
                            .getElementsByTagName("onentry");

                    for (int j = 0; j < onEntryNodes.getLength(); j++)
                    {
                        Node onEntryNode = onEntryNodes.item(j);

                        NodeList nl = onEntryNode.getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++)
                        {
                            if (nl.item(i).getNodeType() == Element.COMMENT_NODE)
                            {
                                Comment comment = (Comment) nl.item(i);
                                codeBlocks.put(stateID, comment.getData());
                            }
                        }
                    }

                    NodeList transiationNodes = eElement
                            .getElementsByTagName("transition");

                    for (int j = 0; j < transiationNodes.getLength(); j++)
                    {
                        Node transitionNode = transiationNodes.item(j);

                        if (transitionNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element transitionElement = (Element) transitionNode;

                            String eventID = transitionElement
                                    .getAttribute("event");
                            if (eventID != null && !eventID.trim().equals(""))
                            {
                                System.out.println("Event : " + eventID);

                                exists = eventIndex.get(eventID);

                                if (exists == null)
                                {
                                    eventIndex.put(eventID, eventIndex.keySet()
                                            .size() + 1);
                                }

                                System.out.println("Target : "
                                        + transitionElement
                                                .getAttribute("target"));
                            }
                        }
                    }
                }

                System.out.println();
            }

            nList = doc.getElementsByTagName("final");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    String stateID = eElement.getAttribute("id");

                    Integer exists = stateIndex.get(stateID);

                    if (exists == null)
                    {
                        stateIndex.put(stateID, stateIndex.keySet().size() + 1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // List all states with their new ids
        System.out.println("States ID mappings:");

        Set<String> stateIDSet = stateIndex.keySet();
        TreeSet<String> orderedSet = new TreeSet<String>(stateIDSet);

        for (String stateID : orderedSet)
        {
            System.out.println(stateID + ": " + stateIndex.get(stateID));
        }

        // List all events with their new ids
        System.out.println("event ID mappings:");

        Set<String> eventIDSet = eventIndex.keySet();
        TreeSet<String> orderedEventSet = new TreeSet<String>(eventIDSet);

        for (String eventID : orderedEventSet)
        {
            System.out.println(eventID + ": " + eventIndex.get(eventID));
        }
    }

    public void generateDatabase()
    {
        try
        {
            File fXmlFile = new File(inputFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :"
                    + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("state");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    String stateID = eElement.getAttribute("id");

                    Integer exists = stateIndex.get(stateID);

                    if (exists == null)
                    {
                        System.err.println("Index for state with name: "
                                + stateID + " is unknown.");
                        continue;
                    }

                    System.out.println("State id: " + stateID);

                    NodeList transiationNodes = eElement
                            .getElementsByTagName("transition");

                    for (int j = 0; j < transiationNodes.getLength(); j++)
                    {
                        Node transitionNode = transiationNodes.item(j);

                        if (transitionNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element transitionElement = (Element) transitionNode;

                            String eventID = transitionElement
                                    .getAttribute("event");

                            if (eventID != null && !eventID.trim().equals(""))
                            {
                                System.out.println("Event : " + eventID);

                                exists = eventIndex.get(eventID);

                                if (exists == null)
                                {
                                    System.err
                                            .println("Index for event with name: "
                                                    + eventID + " is unknown.");
                                    continue;
                                }
                                else
                                {
                                    String target = transitionElement
                                            .getAttribute("target");

                                    transitions.add(new Transition(stateID, eventID, target));

                                    codeTransitions
                                            .add("stateMachine.InsertTransition(0, "
                                                    + stateID
                                                    + ", "
                                                    + eventID
                                                    + ", " + target + ");");
                                }
                            }
                        }
                    }
                }

                System.out.println();
            }

            nList = doc.getElementsByTagName("final");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {

                    Element eElement = (Element) nNode;

                    String stateID = eElement.getAttribute("id");

                    Integer exists = stateIndex.get(stateID);

                    if (exists == null)
                    {
                        stateIndex.put(stateID, stateIndex.keySet().size() + 1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // List all states with their new ids
        System.out.println("States ID mappings:");

        Set<String> stateIDSet = stateIndex.keySet();
        TreeSet<String> orderedSet = new TreeSet<String>(stateIDSet);

        for (String stateID : orderedSet)
        {
            System.out.println(stateID + ": " + stateIndex.get(stateID));
        }

        // List all events with their new ids
        System.out.println("event ID mappings:");

        Set<String> eventIDSet = eventIndex.keySet();
        TreeSet<String> orderedEventSet = new TreeSet<String>(eventIDSet);

        for (String eventID : orderedEventSet)
        {
            System.out.println(eventID + ": " + eventIndex.get(eventID));
        }
    }

    public abstract StringBuffer genFile(BufferedReader templateReader,
            boolean makeVirtual);
    
    public StringBuffer genFile(String template, boolean makeVirtual)
    {
        BufferedReader reader = new BufferedReader(new StringReader(template));
        return genFile(reader, makeVirtual);
    }

    public abstract void generateFiles(String className, String outputPath, boolean makeVirtual, boolean derived);

    public String generate(String template, String className, boolean makeVirtual)
    {
        AbstractGenerator.CLASS_NAME = className;
        BufferedReader in = null;

        try
        {
            if (in == null)
            {
                InputStream is = AbstractGenerator.class.getResourceAsStream("/az/"
                        + template);
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }

            StringBuffer output = genFile(in, makeVirtual);

            in.close();

            return output.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }
    
    public String generateFromExisting(String existingFile, String className, boolean makeVirtual)
    {
        AbstractGenerator.CLASS_NAME = className;
        BufferedReader in = null;

        try
        {
            if (in == null)
            {
                FileInputStream is = new FileInputStream(new File(existingFile));
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }

            StringBuffer output = genFile(in, makeVirtual);

            in.close();

            return output.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static void writeToFile(String code, String filename)
    {
        try
        {
            PrintWriter out = new PrintWriter(filename);
            out.println(code.toString());
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
