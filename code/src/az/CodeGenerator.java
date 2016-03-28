/**
 * Tool for generating code implementation files from scxml files.
 * 
 * By default, this code will not overwrite existing implementation files unless 
 * instructed to do so via the --force-overwrite flag.
 * 
 * By default, generated classes are derived from Automaton so that there is a 
 * common way to manage them. This can be overridden with the automaton code 
 * existing as part of the class, by using the --not-derived flag.
 * 
 * By default, generated classes are assumed to be final. If you prefer to 
 * separate the implementation of the states from these generated files, use 
 * the --make-virtual flag to make all state methods virtual.
 */
package az;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class CodeGenerator extends AZGenericMachineGenerator
{
    static String CLASS_NAME_PREFIX = "AZ";
    static String TEMPLATE_NAME = null;
    static String TEMPLATE_H = "h.template";
    static String TEMPLATE_CPP = "cpp.template";
    static String TEMPLATE_DERIVED_H = "h.derived.template";
    static String TEMPLATE_DERIVED_CPP = "cpp.derived.template";
    static String OUTPUT_PATH = "";
    static String STATE_DIAGRAM = null;
    static ArrayList<String> fileTypes = new ArrayList<String>();
    static String projectFile;

    // Overwrite existing implementation files?
    static boolean forceOverwrite = false;

    // Derived from Automaton? Or Automaton built in?
    // Preference is for the class to be derived from an Automaton.
    // Probably should be default, other case is not-derived.
    static boolean derived = true;

    static boolean makeVirtual = false;
    
    static boolean outputAutomaton = false;
    
    private static String getPackageFileAsString(String filename)
    {
        String content = null;
        try
        {
            InputStream is = CodeGenerator.class.getResourceAsStream("/az/"
                    + filename);
            InputStreamReader reader = new InputStreamReader(is);
            Scanner scanner = new Scanner(reader);
            content = scanner.useDelimiter("\\A").next();
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return content;
    }

    public static String getPredefinedTemplate(String template)
    {
        return getPackageFileAsString(template);
    }

    public static String getTemplateFromFile(String template)
    {
        String content = null;

        try
        {
            FileInputStream is = new FileInputStream(template);
            Scanner scanner = new Scanner(is);
            content = scanner.useDelimiter("\\Z").next();
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return content;
    }

    // TODO: Implement the method that takes all parameters.
    public static String generate(String diagram, String template, String className, boolean makeVirtual)
    {
        // TODO: Check if the class has already been created. Only create it if it
        // hasn't been.
        CodeGenerator jenny = new CodeGenerator();
        CodeGenerator.inputFile = diagram;
        CodeGenerator.CLASS_NAME = className;
        jenny.init();
        BufferedReader in = null;

        try
        {
            if (in == null)
            {
                InputStream is = CodeGenerator.class.getResourceAsStream("/az/"
                        + template);
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }

            StringBuffer output = jenny.genFile(in, makeVirtual);

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
    
    public static String generateDefault(String diagram, String template, String className)
    {
        return generate(diagram, template, className, makeVirtual);
    }

    public static String generateCPP(String diagram, String className, boolean makeVirtual, boolean derived)
    {
        if(derived)
        {
          return generate(diagram, TEMPLATE_DERIVED_CPP, className, makeVirtual);
        }
        else
        {
          return generate(diagram, TEMPLATE_CPP, className, makeVirtual);
        }
    }

    public static String generateDefaultCPP(String diagram, String className)
    {
        return generateDefault(diagram, TEMPLATE_CPP, className);
    }

    public static String generateH(String diagram, String className, boolean makeVirtual, boolean derived)
    {
        if(derived)
        {
          return generate(diagram, TEMPLATE_DERIVED_H, className, makeVirtual);
        }
        else
        {
          return generate(diagram, TEMPLATE_H, className, makeVirtual);
        }
    }

    public static String generateDefaultH(String diagram, String className)
    {
        return generateDefault(diagram, TEMPLATE_H, className);
    }
    
    public static void generateCPPFiles(String diagram, String className, String outputPath, boolean makeVirtual, boolean derived)
    {
        String fileBaseName = outputPath;
        if(fileBaseName == null)
        {
            fileBaseName = "";
        }
        if(!fileBaseName.equals("") && !fileBaseName.endsWith(File.separator))
        {
            fileBaseName += File.separator;
        }
        String cppCode = generateCPP(diagram, className, makeVirtual, derived);
        writeToFile(cppCode, fileBaseName + className + ".cpp");
        String hCode = generateH(diagram, className, makeVirtual, derived);
        writeToFile(hCode, fileBaseName + className + ".h");
    }

    public static void generateDefaultCPPFiles(String diagram, String className, String outputPath)
    {
        String cppCode = generateDefaultCPP(diagram, className);
        writeToFile(cppCode, className + ".cpp");
        String hCode = generateDefaultH(diagram, className);
        writeToFile(hCode, className + ".h");
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

    public static void generateFromProject(String projectFile)
    {
      try
      {
        File file = new File(projectFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        generateFromProject(document);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        System.exit(1);
      }
    }

    public static void generateFromProject(Document projectDocument)
    {
      if (projectDocument == null)
      {
        System.err.println("Invalid projectDocument");
        System.exit(1);
      }
      recursivelyGenerateFromNode((Node)projectDocument);
    }

    public static void recursivelyGenerateFromNode(Node node)
    {
      if(node.getNodeName().equals("automaton"))
      {
        generateFromProjectNode(node);
        // Need to be recursive at this point.
      }
      NodeList list = node.getChildNodes();
 
      for (int i = 0 ; i < list.getLength() ; i++)
      {
        Node innerNode = list.item(i);
        if (innerNode.getNodeType() == Node.ELEMENT_NODE)
        {
          recursivelyGenerateFromNode(innerNode);
        }
      }
    }

    public static void generateFromProjectNode(Node node)
    {
      String diagram = ((Element)node).getAttribute("diagram");
      String baseClass = ((Element)node).getAttribute("baseClass");
      if(diagram == null || diagram.equals("") || baseClass == null || baseClass.equals(""))
      {
        return;
      }
      String virtual = ((Element)node).getAttribute("makeVirtual");
      boolean makeVirtual = CodeGenerator.makeVirtual;
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
      boolean derived = CodeGenerator.derived;
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
      generateCPPFiles(diagram, baseClass, outputPath, makeVirtual, derived);
    }

    public static void main(String[] args)
    {
        processArgs(args);

        CodeGenerator jenny = new CodeGenerator();
        CodeGenerator.inputFile = STATE_DIAGRAM;
        jenny.init();

        for (String fileType : fileTypes)
        {
            try
            {
                String outputFile = OUTPUT_PATH + CLASS_NAME + fileType;
                File f = new File(outputFile);

                if (f.exists() && !f.isDirectory())
                {
                    if (TEMPLATE_NAME != null)
                    {
                        System.err
                                .println("Output file exists. Cannot use template with an existing file. Either remove the custom template from the command line or delete the existing output file.");
                        System.exit(1);
                    }

                    if (!forceOverwrite)
                    {
                        System.err
                                .println("Output file exists. Use: --force-overwrite to overwrite.");
                        System.err.println("Existing file is: " + outputFile);
                        System.exit(1);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String templateName = TEMPLATE_NAME;
            if (fileType.equals(".h"))
            {
                String outputFile = OUTPUT_PATH + CLASS_NAME + ".h";

                String template = null;

                if (templateName != null)
                {
                    template = getTemplateFromFile(templateName);
                    
                    if(template == null)
                    {
                        System.err.println("Failed to read template from file");
                        System.exit(1);
                    }
                }
                else
                {
                    templateName = TEMPLATE_H;

                    if (derived)
                    {
                        templateName = TEMPLATE_DERIVED_H;
                    }

                    File f = new File(outputFile);
                    if (f.exists() && !f.isDirectory())
                    {
                        System.out.println("Using existing file as template: " + templateName);
                        // Use existing file as template.
                        template = getTemplateFromFile(templateName);
                    }

                    if (template == null)
                    {
                        template = getPredefinedTemplate(templateName);
                    }
                    
                    if(template == null)
                    {
                        System.err.println("Failed to read default template: " + templateName);
                        System.exit(1);
                    }
                }

                System.err.println("Converting template: " + templateName
                        + " into: " + outputFile);
                StringBuffer output = jenny.genFile(template, makeVirtual);

                if (output != null)
                {
                    writeToFile(output.toString(), outputFile);
                }
            }
            else if (fileType.equals(".cpp"))
            {
                String outputFile = OUTPUT_PATH + CLASS_NAME + ".cpp";

                String template = null;

                if (templateName != null)
                {
                    template = getTemplateFromFile(templateName);
                }
                else
                {
                    templateName = TEMPLATE_CPP;

                    if (derived)
                    {
                        templateName = TEMPLATE_DERIVED_CPP;
                    }

                    File f = new File(outputFile);
                    if (f.exists() && !f.isDirectory())
                    {
                        System.out.println("Using existing file as template");
                        // Use existing file as template.
                        template = getTemplateFromFile(templateName);
                    }

                    if (template == null)
                    {
                        template = getPredefinedTemplate(templateName);
                    }
                }

                System.out.println("Converting template: " + templateName
                        + " into: " + outputFile);

                StringBuffer output = jenny.genFile(template, makeVirtual);

                if (output != null)
                {
                    writeToFile(output.toString(), outputFile);
                }
                else
                {
                    System.err.println("Error: No output was generated");
                    System.exit(1);
                }
            }
            else
            {
                System.err.println("No valid file type specified");
                System.exit(1);
            }
        }
    }

    private static void processArgs(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("--output-path"))
            {
                String outputPath = args[i + 1];
                
                if(outputPath.length() > 0)
                { 
                    OUTPUT_PATH = outputPath;
                    
                    if(!outputPath.endsWith(File.separator))
                    {
                        OUTPUT_PATH += File.separator;
                    }
                }

                i++;
            }
            else if (args[i].equals("--diagram"))
            {
                STATE_DIAGRAM = args[i + 1];
                i++;
            }
            else if (args[i].equals("--template"))
            {
                TEMPLATE_NAME = args[i + 1];
                i++;
            }
            else if (args[i].equals("--class-name"))
            {
                CLASS_NAME = args[i + 1];
                i++;
            }
            else if (args[i].equals("--class-name-prefix"))
            {
                CLASS_NAME_PREFIX = args[i + 1];
                i++;
            }
            else if (args[i].equals("--no-prefix")) // no class name prefix (make default?)
            {
                CLASS_NAME_PREFIX = "";
            }
            else if (args[i].equals("--force-overwrite"))
            {
                forceOverwrite = true;
            }
            else if (args[i].equals("--not-derived"))
            {
                derived = false;
            }
            else if (args[i].equals("--make-virtual"))
            {
                makeVirtual = true;
            }
            else if (args[i].equals("--file-type"))
            {
                fileTypes.add(args[i + 1]);
                i++;
            }
            else if (args[i].equals("--output-automaton"))
            {
                outputAutomaton = true;
            }
            else if (args[i].equals("--generate-from-project"))
            {
              projectFile = args[i + 1];
              i++;
            }
            else
            {
                System.err.println("Flag not recognized: " + args[i]);
                System.exit(1);
            }
        }
        
        // Output the automaton code too
        if (outputAutomaton)
        {
            String azCPP = getPackageFileAsString("AZ.cpp");
            writeToFile(azCPP, OUTPUT_PATH + "AZ.cpp");

            String azH = getPackageFileAsString("AZ.h");
            writeToFile(azH, OUTPUT_PATH + "AZ.h");

            String azHPP = getPackageFileAsString("AZ.t.hpp");
            writeToFile(azHPP, OUTPUT_PATH + "AZ.t.hpp");

            String azJS = getPackageFileAsString("javascript/az.js");
            writeToFile(azJS, OUTPUT_PATH + "az.js");

            if (derived)
            {
                String automatonCPP = getPackageFileAsString("Automaton.cpp");
                writeToFile(automatonCPP, OUTPUT_PATH + "Automaton.cpp");

                String automatonH = getPackageFileAsString("Automaton.h");
                writeToFile(automatonH, OUTPUT_PATH + "Automaton.h");
            }

            String tcpClientCPP = getPackageFileAsString("TCPClient.cpp");
            writeToFile(tcpClientCPP, OUTPUT_PATH + "TCPClient.cpp");

            String tcpClientH = getPackageFileAsString("TCPClient.h");
            writeToFile(tcpClientH, OUTPUT_PATH + "TCPClient.h");

            String stateDebuggerCPP = getPackageFileAsString("StateDebugger.cpp");
            writeToFile(stateDebuggerCPP, OUTPUT_PATH + "StateDebugger.cpp");

            String stateDebuggerH = getPackageFileAsString("StateDebugger.h");
            writeToFile(stateDebuggerH, OUTPUT_PATH + "StateDebugger.h");

            System.exit(0);
        }

        if(projectFile != null)
        {
            generateFromProject(projectFile);
            System.exit(0);
        }
        
        if (CLASS_NAME == null)
        {
            System.err.println("No class name specified.");
            System.err.println("Specify one with --class-name");
            System.exit(1);
        }

        if (TEMPLATE_NAME != null && fileTypes.size() != 1)
        {
            System.err.println("When specifying a template to use, excatly one file type must also be specified");
            System.exit(1);
        }

        if (TEMPLATE_NAME == null && fileTypes.size() == 0)
        {
            System.err.println("Specify a file type with --file-type to use an in build template. Otherwise supply a custom template with --template");
            System.exit(1);
        }

        if (TEMPLATE_NAME != null && !derived)
        {
            System.err.println("Warning: --not-derived has no effect when using a custom template");
        }
        
        if (STATE_DIAGRAM == null)
        {
            System.err.println("No diagram specified. Sepcify one with --diagram");
            System.exit(1);
        }

        CodeGenerator.CLASS_NAME = CLASS_NAME_PREFIX + CLASS_NAME;
    }
}
