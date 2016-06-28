/**
 * Class for managing generation of code from a project file.
 **/
package az;

import java.io.File;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectGenerator
{
  private static final Logger log = Logger.getLogger(ProjectGenerator.class.getName());
  private String language = "c++"; // should read this from the command line.
  private String projectFile = null;
  public ProjectGenerator()
  {
    log.log(Level.INFO, "Project generator instantiated");
  }

  public void processArgs(String[] args)
  {
    log.log(Level.INFO, "Processing args...");
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].equals("--generate-from-project"))
      {
        projectFile = args[i + 1];
        break;
      }
      // could probably define the language on the command line.
    }
    if(projectFile == null)
    {
      log.log(Level.SEVERE, "No project file specified");
      System.exit(1);
    }
    else
    {
      log.log(Level.INFO, "Found project file: " + projectFile);
    }
  }

  public void generateCode()
  {
    log.log(Level.INFO, "Generating code.");
      try
      {
        File file = new File(projectFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        if (document == null)
        {
          log.log(Level.SEVERE, "Invalid Document (XML processing)");
          System.exit(1);
        }
        recursivelyGenerateFromNode((Node)document);
        }
        catch(Exception e)
        {
          log.log(Level.SEVERE, "Caught exception while processing the project document", e);
          System.exit(1);
        }
  }

    public void recursivelyGenerateFromNode(Node node)
    {
      log.log(Level.INFO, "Entering recursivelyGenerateFromNode");
      if(node.getNodeName().equals("automaton"))
      {
        log.log(Level.INFO, "Node name = automaton");
        generateFromProjectNode(node);
        // Need to be recursive at this point.
      }
      NodeList list = node.getChildNodes();
      log.log(Level.INFO, "Current node has " + list.getLength() + " children");
 
      for (int i = 0 ; i < list.getLength() ; i++)
      {
        Node innerNode = list.item(i);
        if (innerNode.getNodeType() == Node.ELEMENT_NODE)
        {
          recursivelyGenerateFromNode(innerNode);
        }
      }
    }

    public void generateFromProjectNode(Node node)
    {
      String diagram = ((Element)node).getAttribute("diagram");
      String baseClass = ((Element)node).getAttribute("baseClass");
      if(diagram == null || diagram.equals("") || baseClass == null || baseClass.equals(""))
      {
        return;
      }
      String virtual = ((Element)node).getAttribute("makeVirtual");
      boolean makeVirtual = AbstractGenerator.makeVirtual;
      if(virtual != null && !virtual.equals(""))
      {
        if(virtual.equalsIgnoreCase("true"))
        {
          makeVirtual = true;
        }
        else if(virtual.equalsIgnoreCase("false"))
        {
          makeVirtual = false;
        }
        else
        {
          System.err.println("validation error. makeVirtual not set to true or false. was set to: " + virtual);
          System.exit(1);
        }
      }
      String derivedString = ((Element)node).getAttribute("derived");
      boolean derived = AbstractGenerator.derived;
      if(derivedString != null && !derivedString.equals(""))
      {
        if(derivedString.equalsIgnoreCase("true"))
        {
          derived = true;
        }
        else if(derivedString.equalsIgnoreCase("false"))
        {
          derived = false;
        }
        else
        {
          System.err.println("validation error. derived not set to true or false. was set to: " + derivedString);
          System.exit(1);
        }
      }
      String outputPath = ((Element)node).getAttribute("outputPath");
      if(outputPath == null)
      {
        outputPath = "";
      }
      if(language.equals("c++"))
      {
        CPPGenerator generator = new CPPGenerator();
        generator.init(diagram);
        generator.generateFiles(baseClass, outputPath, makeVirtual, derived);
      }
    }
}
