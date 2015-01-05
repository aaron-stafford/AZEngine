package az;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AZGenericMachineGenerator
{
	static Hashtable<String, Integer> stateIndex = new Hashtable<String, Integer>();
	static Hashtable<String, Integer> eventIndex = new Hashtable<String, Integer>();
	static Hashtable<String, String> codeBlocks = new Hashtable<String, String>();

	static String inputFile = null;
	static String CLASS_NAME = null;
	static String STATE_METHOD_PREFIX = "AZ_";
	static ArrayList<String> transitions = new ArrayList<String>();
	static String initialState = null;

	/**
	 * @param args
	 */
	public void init()
	{
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
		} catch (Exception e)
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
								} else
								{
									String target = transitionElement
									        .getAttribute("target");

									transitions
									        .add("stateMachine->InsertTransition(0, "
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
		} catch (Exception e)
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

	public StringBuffer genFile(BufferedReader templateReader,
	        boolean makeVirtual)
	{
		StringBuffer output = new StringBuffer();

		try
		{
			while (templateReader.ready())
			{
				String s = templateReader.readLine();

				if(s == null)
				{
					break;
				}

				Pattern p = Pattern.compile(".*(\\$\\{(.*)\\}).*");
				Matcher m = p.matcher(s);
				boolean b = m.matches();
				if (b)
				{
					String key = m.group(2);
					String matchedString = Pattern.quote(m.group(1));

					if (key.equals("CLASS_NAME"))
					{
						String result = s.replaceAll(matchedString, CLASS_NAME);
						output.append(result);
						
					} else if (key.equals("STATE_INDEXES_START"))
					{
						Set<String> stateNames = stateIndex.keySet();
						TreeSet<String> orderedSet = new TreeSet<String>(
						        stateNames);
						output.append(s + "\n");
						for (String stateName : orderedSet)
						{
							output.append("  const int " + stateName + " = "
							        + stateIndex.get(stateName) + ";\n");
						}

						while (templateReader.ready())
						{
							String tempString = templateReader.readLine();
							Matcher matches = p.matcher(tempString);
							boolean didMatch = matches.matches();
							if (didMatch)
							{
								String newKey = matches.group(2);

								if (newKey.equals("STATE_INDEXES_END"))
								{
									output.append(tempString);
									break;
								}
							}
						}
					} else if (key.equals("INPUT_INDEXES_START"))
					{
						Set<String> eventNames = eventIndex.keySet();
						TreeSet<String> orderedEventSet = new TreeSet<String>(
						        eventNames);

						output.append(s + "\n");
						for (String eventName : orderedEventSet)
						{
							output.append("  const int " + eventName + " = "
							        + eventIndex.get(eventName) + ";\n");
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
					} else if (key.equals("STATE_METHODS_DECLARATIONS_START"))
					{
						Set<String> stateNames = stateIndex.keySet();
						TreeSet<String> orderedSet = new TreeSet<String>(
						        stateNames);

						output.append(s + "\n");
						for (String stateName : orderedSet)
						{
							if (makeVirtual)
							{
								output.append("  virtual bool "
								        + STATE_METHOD_PREFIX + stateName
								        + "();\n");

							} else
							{
								output.append("  bool " + STATE_METHOD_PREFIX
								        + stateName + "();\n");
							}
						}

						while (templateReader.ready())
						{
							String tempString = templateReader.readLine();
							Matcher matches = p.matcher(tempString);
							boolean didMatch = matches.matches();
							if (didMatch)
							{
								String newKey = matches.group(2);

								if (newKey
								        .equals("STATE_METHODS_DECLARATIONS_END"))
								{
									output.append(tempString);
									break;
								}
							}
						}
					} else if (key.equals("STATE_METHODS_DEFINITIONS"))
					{
						Set<String> stateNames = stateIndex.keySet();
						TreeSet<String> orderedSet = new TreeSet<String>(
						        stateNames);

						for (String stateName : orderedSet)
						{
							output.append("bool " + CLASS_NAME + "::"
							        + STATE_METHOD_PREFIX + stateName + "()\n");
							output.append("{\n");
							String code = codeBlocks.get(stateName);
							if (code != null)
							{
								output.append(code + "\n");
							} else
							{
								output.append("  return false;\n");
							}
							output.append("}\n\n");
						}
					} else if (key.equals("TABLE_INSERTIONS_START"))
					{
						output.append(s + "\n");
						for (String transition : transitions)
						{
							output.append("  " + transition + "\n");
						}

						while (templateReader.ready())
						{
							String tempString = templateReader.readLine();
							Matcher matches = p.matcher(tempString);
							boolean didMatch = matches.matches();
							if (didMatch)
							{
								String newKey = matches.group(2);

								if (newKey.equals("TABLE_INSERTIONS_END"))
								{
									output.append(tempString);
									break;
								}
							}
						}
					} else if (key.equals("STATE_METHOD_POINTERS_START"))
					{
						Set<String> stateNames = stateIndex.keySet();
						TreeSet<String> orderedSet = new TreeSet<String>(
						        stateNames);

						output.append(s + "\n");
						output.append("  mi = new MethodIndex["
						        + (orderedSet.size() + 1) + "];\n");
						String[] name = orderedSet.toArray(new String[2]);
						for (int i = 0; i < orderedSet.size(); i++)
						{
							output.append("  mi[" + name[i] + "] = (MethodIndex)&"
							        + CLASS_NAME + "::AZ_" + name[i] + ";\n");
						}

						while (templateReader.ready())
						{
							String tempString = templateReader.readLine();
							Matcher matches = p.matcher(tempString);
							boolean didMatch = matches.matches();
							if (didMatch)
							{
								String newKey = matches.group(2);

								if (newKey.equals("STATE_METHOD_POINTERS_END"))
								{
									output.append(tempString);
									break;
								}
							}
						}
					} else if (key.equals("INITIAL_STATE_START"))
					{
						output.append(s + "\n");
						output.append("  m_CurrentState = " + initialState
						        + ";\n");

						while (templateReader.ready())
						{
							String tempString = templateReader.readLine();
							Matcher matches = p.matcher(tempString);
							boolean didMatch = matches.matches();
							if (didMatch)
							{
								String newKey = matches.group(2);

								if (newKey.equals("INITIAL_STATE_END"))
								{
									output.append(tempString);
									break;
								}
							}
						}
					}
				} else
				{
					output.append(s);
				}

				output.append("\n");
			}
			return output;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public StringBuffer genFile(String template, boolean makeVirtual)
	{
		BufferedReader reader = new BufferedReader(new StringReader(template));

		return genFile(reader, makeVirtual);
	}
}
