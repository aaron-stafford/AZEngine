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

public class EngineBridgeGenerator
{
    private static final Logger log = Logger.getLogger(EngineBridgeGenerator.class.getName());
    private final String TEMPLATE_CPP = "EngineBridge.cpp.template";
    private final String TEMPLATE_H = "EngineBridge.h.template";
    private final String outputCPPFile = "../../gameplay/control/generated/EngineBridge.cpp"; // move this to the project file.
    private final String outputHFile = "../../gameplay/control/generated/EngineBridge.h"; // move this to the project file.
    private String projectFile;
    Hashtable<String, String> classNames = new Hashtable<String, String>();
    Hashtable<String, String> rootUpdateClasses = new Hashtable<String, String>();
    Hashtable<String, String> touchUpClasses = new Hashtable<String, String>();
    Hashtable<String, String> touchDownClasses = new Hashtable<String, String>();
    Hashtable<String, String> touchMoveClasses = new Hashtable<String, String>();

    public void generate(String projectFile)
    {
      log.log(Level.INFO, "Generating Engine Bridge...");
      try
      {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        NodeList automata = (NodeList) xPath.evaluate("/project/automaton", new InputSource(new FileReader(
            projectFile)), XPathConstants.NODESET);
        log.log(Level.INFO, "found " + automata.getLength() + " node(s) in: " + projectFile);
        for (int i = 0; i < automata.getLength(); i++)
        {
          Element automaton = (Element) automata.item(i);
          String displayName = automaton.getAttribute("displayName");
          String baseClass = automaton.getAttribute("baseClass");
          String addToRootUpdate = automaton.getAttribute("addToRootUpdate");
          if(addToRootUpdate.equalsIgnoreCase("true"))
          {
            classNames.put(baseClass, baseClass);
            rootUpdateClasses.put(baseClass, baseClass);
          }
          String registerTouchDown = automaton.getAttribute("registerTouchDown");
          if(registerTouchDown.equalsIgnoreCase("true"))
          {
            classNames.put(baseClass, baseClass);
            touchDownClasses.put(baseClass, baseClass);
          }
          String registerTouchMove = automaton.getAttribute("registerTouchMove");
          if(registerTouchMove.equalsIgnoreCase("true"))
          {
            classNames.put(baseClass, baseClass);
            touchMoveClasses.put(baseClass, baseClass);
          }
          String registerTouchUp = automaton.getAttribute("registerTouchUp");
          if(registerTouchUp.equalsIgnoreCase("true"))
          {
            classNames.put(baseClass, baseClass);
            touchUpClasses.put(baseClass, baseClass);
          }
        }
      }
      catch(Exception e)
      {
        log.log(Level.SEVERE, "Caught exception while processing the project document", e);
        System.exit(1);
      }


        try
        {
          this.projectFile = projectFile;
          {
            log.log(Level.INFO, "Attempting to generated: " + outputCPPFile);
            String cppCode = null;
            File f = new File(outputCPPFile);
            if (f.exists() && !f.isDirectory())
            {
              log.log(Level.INFO, "Output file already exists. Overwriting.");
            }
            else
            {
              log.log(Level.INFO, "Output file does not exist. Creating new from template.");
            }
            BufferedReader templateReader = getTemplateReader(TEMPLATE_CPP);
            String output = populateTemplate(templateReader);
            writeToFile(output, outputCPPFile);
          }
          {
            log.log(Level.INFO, "Attempting to generated: " + outputHFile);
            String hCode = null;
            File f = new File(outputHFile);
            if (f.exists() && !f.isDirectory())
            {
              log.log(Level.INFO, "Output file already exists. Overwriting.");
            }
            else
            {
              log.log(Level.INFO, "Output file does not exist. Creating new from template.");
            }
            BufferedReader templateReader = getTemplateReader(TEMPLATE_H);
            String output = populateTemplate(templateReader);
            writeToFile(output, outputHFile);
          }
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Caught exception while generating Engine Bridge", e);
            System.exit(1);
        }
    }

    public BufferedReader getTemplateReader(String templateName)
    {
        BufferedReader in = null;
        try
        {
            if (in == null)
            {
                InputStream is = AbstractGenerator.class.getResourceAsStream("/az/"
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
                    else if (key.equals("CONTROLLER_CREATION_START"))
                    {
                        output.append(s + "\n");


                        Enumeration<String> enumKey = classNames.keys();
                        while(enumKey.hasMoreElements())
                        {
                          String className = enumKey.nextElement();
                          output.append("  {\n");
                          output.append("    Automaton* automaton = new " + className + "();\n");
                          output.append("    automaton->InitAutomaton();\n");
                          if(rootUpdateClasses.get(className) != null)
                          {
                            output.append("    m_RootControllers.push_back(automaton);\n");
                          }
                          if(touchDownClasses.get(className) != null)
                          {
                            output.append("    m_TouchDownControllers.push_back(automaton);\n");
                          }
                          if(touchMoveClasses.get(className) != null)
                          {
                            output.append("    m_TouchMoveControllers.push_back(automaton);\n");
                          }
                          if(touchUpClasses.get(className) != null)
                          {
                            output.append("    m_TouchUpControllers.push_back(automaton);\n");
                          }
                          output.append("  }\n");
                        }

                        while (templateReader.ready())
                        {
                            String tempString = templateReader.readLine();
                            Matcher matches = p.matcher(tempString);
                            boolean didMatch = matches.matches();
                            if (didMatch)
                            {
                                String newKey = matches.group(2);

                                if (newKey.equals("CONTROLLER_CREATION_END"))
                                {
                                    output.append(tempString);
                                    break;
                                }
                            }
                        }
                    }
                    else if (key.equals("CONTROLLER_INCLUDES_START"))
                    {
                        output.append(s + "\n");


                        Enumeration<String> enumKey = classNames.keys();
                        while(enumKey.hasMoreElements())
                        {
                          String className = enumKey.nextElement();
                          output.append("#include \"" + className + ".h\"\n");
                        }

                        while (templateReader.ready())
                        {
                            String tempString = templateReader.readLine();
                            Matcher matches = p.matcher(tempString);
                            boolean didMatch = matches.matches();
                            if (didMatch)
                            {
                                String newKey = matches.group(2);

                                if (newKey.equals("CONTROLLER_INCLUDES_END"))
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
