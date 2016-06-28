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
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class Main
{
  private static final Logger log = Logger.getLogger(Main.class.getName());
    static String CLASS_NAME_PREFIX = "AZ";
    static String TEMPLATE_NAME = null;
    static String OUTPUT_PATH = "";
    static String STATE_DIAGRAM = null;
    static ArrayList<String> fileTypes = new ArrayList<String>();
    static String projectFile;
    static String language = null;
    static AbstractGenerator generator = null;

    // Overwrite existing implementation files?
    static boolean forceOverwrite = false;

    static boolean outputAutomaton = false;
    
    private static String getPackageFileAsString(String filename)
    {
        String content = null;
        try
        {
            InputStream is = Main.class.getResourceAsStream("/az/"
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

    public static void main(String[] args)
    {
        processArgs(args);

/*
        for (String fileType : fileTypes)
        {
            try
            {
                String outputFile = OUTPUT_PATH + AbstractGenerator.CLASS_NAME + fileType;
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
                String outputFile = OUTPUT_PATH + AbstractGenerator.CLASS_NAME + ".h";

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
                StringBuffer output = generator.genFile(template, makeVirtual);

                if (output != null)
                {
                    writeToFile(output.toString(), outputFile);
                }
            }
            else if (fileType.equals(".cpp"))
            {
                String outputFile = OUTPUT_PATH + AbstractGenerator.CLASS_NAME + ".cpp";

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

                StringBuffer output = generator.genFile(template, makeVirtual);

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
*/
    }

    private static void processArgs(String[] args)
    {
        boolean generateFromProject = false;
        // check if we are processing a project.
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("--generate-from-project"))
            {
               generateFromProject = true;

/*
            else if (args[i].equals("--generate-from-project"))
            {
              projectFile = args[i + 1];
              i++;
            }

        if(projectFile != null)
        {
            generator.generateFromProject(projectFile);
            System.exit(0);
        }
        
*/

               break;
            }
        }
        if(generateFromProject)
        {
            log.log(Level.INFO, "Starting generation from project");
            ProjectGenerator projectGenerator = new ProjectGenerator();
            projectGenerator.processArgs(args);
            projectGenerator.generateCode();
            log.log(Level.INFO, "Finished generating project");
            System.exit(1);
        }
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
                AbstractGenerator.CLASS_NAME = args[i + 1];
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
                AbstractGenerator.derived = false;
            }
            else if (args[i].equals("--make-virtual"))
            {
                AbstractGenerator.makeVirtual = true;
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
            else if (args[i].equals("--language"))
            {
                language = args[i + 1];
                i++;
                if(!language.equals("javascript") && !language.equalsIgnoreCase("c++"))
                {
                    System.err.println("Language: " + language + " is not supported");
                    System.err.println("Only javascript and c++ are supported");
                    System.exit(1);
                }
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
            if(language == null || language.equalsIgnoreCase("c++"))
            {
                String azCPP = getPackageFileAsString("AZ.cpp");
                AbstractGenerator.writeToFile(azCPP, OUTPUT_PATH + "AZ.cpp");
 
                String azH = getPackageFileAsString("AZ.h");
                AbstractGenerator.writeToFile(azH, OUTPUT_PATH + "AZ.h");
 
                String azHPP = getPackageFileAsString("AZ.t.hpp");
                AbstractGenerator.writeToFile(azHPP, OUTPUT_PATH + "AZ.t.hpp");
 
                if (AbstractGenerator.derived)
                {
                    String automatonCPP = getPackageFileAsString("Automaton.cpp");
                    AbstractGenerator.writeToFile(automatonCPP, OUTPUT_PATH + "Automaton.cpp");
 
                    String automatonH = getPackageFileAsString("Automaton.h");
                    AbstractGenerator.writeToFile(automatonH, OUTPUT_PATH + "Automaton.h");
                }
 
                String tcpClientCPP = getPackageFileAsString("TCPClient.cpp");
                AbstractGenerator.writeToFile(tcpClientCPP, OUTPUT_PATH + "TCPClient.cpp");
 
                String tcpClientH = getPackageFileAsString("TCPClient.h");
                AbstractGenerator.writeToFile(tcpClientH, OUTPUT_PATH + "TCPClient.h");
 
                String stateDebuggerCPP = getPackageFileAsString("StateDebugger.cpp");
                AbstractGenerator.writeToFile(stateDebuggerCPP, OUTPUT_PATH + "StateDebugger.cpp");
 
                String stateDebuggerH = getPackageFileAsString("StateDebugger.h");
                AbstractGenerator.writeToFile(stateDebuggerH, OUTPUT_PATH + "StateDebugger.h");
            }
            else if(language.equalsIgnoreCase("javascript"))
            {
                String azJS = getPackageFileAsString("javascript/az.js");
                AbstractGenerator.writeToFile(azJS, OUTPUT_PATH + "az.js");
            }
            System.exit(0);
        }

        if(language == null || language.equalsIgnoreCase("c++"))
        {
          generator = new CPPGenerator();
        }
        else if(language.equalsIgnoreCase("javascript"))
        {
          generator = new JavascriptGenerator();
        }
        else
        {
          System.err.println("No generator defined for language: " + language);
        }

        //AbstractGenerator.inputFile = STATE_DIAGRAM;
        //generator.init();

        if (AbstractGenerator.CLASS_NAME == null)
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

        if (TEMPLATE_NAME != null && !AbstractGenerator.derived)
        {
            System.err.println("Warning: --not-derived has no effect when using a custom template");
        }
        
        if (STATE_DIAGRAM == null)
        {
            System.err.println("No diagram specified. Sepcify one with --diagram");
            System.exit(1);
        }

        AbstractGenerator.CLASS_NAME = CLASS_NAME_PREFIX + AbstractGenerator.CLASS_NAME;
    }
}
