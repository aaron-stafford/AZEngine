package az;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

public class GlobalEventsGenerator
{
    private static final Logger log = Logger.getLogger(GlobalEventsGenerator.class.getName());
    private Hashtable<String, Integer> globalEventIndex = new Hashtable<String, Integer>();
    private Hashtable<String, Integer> engineEvents = null;
    final int EVENTS_INDEX_START = 20; // reserved engine event range 0 - 19
    int localEventsCounter = 0;
    private final String TEMPLATE_H = "GlobalEvents.h.template";
    private final String outputHFile = "../../gameplay/control/generated/GlobalEvents.h"; // move this to the project file.
    private String projectFile = null;
    private String[] orderedEventNames;

    public GlobalEventsGenerator(String projectFile, Hashtable<String, Integer> engineEvents)
    {
      this.projectFile = projectFile;
      this.engineEvents = engineEvents;
    }

    public Hashtable<String, Integer> getGlobalEventIndex()
    {
      try
      {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        NodeList automata = (NodeList) xPath.evaluate("//automaton", new InputSource(new FileReader(
            projectFile)), XPathConstants.NODESET);
        log.log(Level.INFO, "found " + automata.getLength() + " automaton node(s) in: " + projectFile);
        for (int i = 0; i < automata.getLength(); i++)
        {
          Element automaton = (Element) automata.item(i);
          String diagram = automaton.getAttribute("diagram");
          processDiagram(diagram);
        }
        createdOrderedEventNames();
        return globalEventIndex;
      }
      catch(Exception e)
      {
        log.log(Level.SEVERE, "Caught exception while processing the project document", e);
        System.exit(1);
      }
      return null;
    }

    private void createdOrderedEventNames()
    {
      orderedEventNames = new String[EVENTS_INDEX_START + localEventsCounter + 1];
      Set<String> engineEventNames = engineEvents.keySet();
      TreeSet<String> engineEventSet = new TreeSet<String>(
              engineEventNames);
      for (String eventName : engineEventSet)
      {
        orderedEventNames[engineEvents.get(eventName)] = eventName;
      }
      Set<String> globalEventNames = globalEventIndex.keySet();
      TreeSet<String> globalEventSet = new TreeSet<String>(
              globalEventNames);
      for (String eventName : globalEventSet)
      {
        orderedEventNames[globalEventIndex.get(eventName)] = eventName;
      }
    }

    public void generate()
    {
      log.log(Level.INFO, "Generating " + projectFile + "...");
      try
      {
        BufferedReader templateReader = getTemplateReader(TEMPLATE_H);
        String output = populateTemplate(templateReader);
        writeToFile(output, outputHFile);
      }
      catch(Exception e)
      {
        log.log(Level.SEVERE, "Caught exception while processing the project document", e);
        System.exit(1);
      }
  }

  private void processDiagram(String diagram)
  {
      log.log(Level.INFO, "Processing " + diagram + "...");
      try
      {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        NodeList transitions = (NodeList) xPath.evaluate("//*[local-name()='transition']", new InputSource(new FileReader(
            diagram)), XPathConstants.NODESET);
        log.log(Level.INFO, "found " + transitions.getLength() + " transition node(s) in: " + diagram);
        for (int i = 0; i < transitions.getLength(); i++)
        {
          Element transition = (Element) transitions.item(i);
          String event = transition.getAttribute("event");
          if(engineEvents.get(event) == null)
          {
            if(globalEventIndex.get(event) == null)
            {
              globalEventIndex.put(event, EVENTS_INDEX_START + localEventsCounter + 1);
             localEventsCounter++;
            }
          }
        }
      }
      catch(Exception e)
      {
        log.log(Level.SEVERE, "Caught exception while processing: " + diagram, e);
        System.exit(1);
      }
  }

  private void writeHeader()
  {

  }

    public String populateTemplate(BufferedReader templateReader)
    {
        StringBuffer output = new StringBuffer();
        boolean notFirstLine = false;

        try
        {
            while (templateReader.ready())
            {
                String s = templateReader.readLine();

                if(s == null)
                {
                    break;
                }

                if(notFirstLine)
                {
                  output.append("\n");
                }
                else
                {
                  notFirstLine = true;
                }

                Pattern p = Pattern.compile(".*(\\$\\{(.*)\\}).*");
                Matcher m = p.matcher(s);
                boolean b = m.matches();
                if (b)
                {
                    String key = m.group(2);
                    String matchedString = Pattern.quote(m.group(1));

                    if (key.equals("PREAMBLE_START"))
                    {
                        output.append(s + "\n");
                        output.append("// Autogenerated from " + projectFile + " using AZEngine.\n");

                        while (templateReader.ready())
                        {
                            String tempString = templateReader.readLine();
                            Matcher matches = p.matcher(tempString);
                            boolean didMatch = matches.matches();
                            if (didMatch)
                            {
                                String newKey = matches.group(2);

                                if (newKey.equals("PREAMBLE_END"))
                                {
                                    output.append(tempString);
                                    break;
                                }
                            }
                        }
                    }
                    else if (key.equals("INPUT_INDEXES_START"))
                    {
                        output.append(s + "\n");

                        Set<String> eventNames = globalEventIndex.keySet();
                        TreeSet<String> orderedEventSet = new TreeSet<String>(
                                eventNames);

                        for (String eventName : orderedEventSet)
                        {
                             output.append("  static const int " + eventName + " = "
                                 + globalEventIndex.get(eventName) + ";\n");
                        }

                        while (templateReader.ready())
                        {
                            String tempString = templateReader.readLine();
                            Matcher matches = p.matcher(tempString);
                            boolean didMatch = matches.matches();
                            if (didMatch)
                            {
                                String newKey = matches.group(2);

                                if (newKey.equals("INPUT_INDEXES_END"))
                                {
                                    output.append(tempString);
                                    break;
                                }
                            }
                        }
                    }
                    else if (key.equals("DEBUG_INPUT_INDEXES_START"))
                    {
                        output.append(s + "\n");
                        output.append("static std::string myArray[" + orderedEventNames.length + "] = {" );
                        for(String eventName : orderedEventNames)
                        {
                          output.append("\"" + eventName + "\",");
                        }
                        output.append("};\n");
                        while (templateReader.ready())
                        {
                            String tempString = templateReader.readLine();
                            Matcher matches = p.matcher(tempString);
                            boolean didMatch = matches.matches();
                            if (didMatch)
                            {
                                String newKey = matches.group(2);
                                if (newKey.equals("DEBUG_INPUT_INDEXES_END"))
                                {
                                    output.append(tempString);
                                    break;
                                }
                            }
                        }
                    }
                }
                else
                {
                    output.append(s);
                }
            }
            return output.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public BufferedReader getTemplateReader(String templateName)
    {
        BufferedReader in = null;
        try
        {
            if (in == null)
            {
                InputStream is = GlobalEventsGenerator.class.getResourceAsStream("/az/"
                        + templateName);
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }
            return in;
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Caught exception while fetching template reader", e);
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
            log.log(Level.INFO, "Finished writing: " + filename);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Caught exception while attempting to write Engine Bridge", e);
            System.exit(1);
        }
    }
}
