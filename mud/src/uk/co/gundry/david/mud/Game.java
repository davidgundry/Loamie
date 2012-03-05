package uk.co.gundry.david.mud;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.co.gundry.david.mud.net.ServerThread;
import uk.co.gundry.david.mud.world.PlayerCharacter;
import uk.co.gundry.david.mud.world.World;

/**
 * Overall control of the game server.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class Game
{
	/**
	 * This is the port the server listens on.
	 * The default should be overwritten by a value loaded from the server config file.
	 */
	private static int PORT = 1357;
	/**
	 * This is the message players see when they first connect to the server.
	 * The default should be overwritten by a value loaded from the server config file.
	 */
	private String welcomeMessage = "Welcome to the server!\n Type help for a list of commands.";
	/**
	 * When the server is running in verbose mode, it echos every command it receives.
	 */
	private static boolean verbose = true;	
	private ServerThread serverThread;
	/**
	 * This is the world currently running on the server.
	 */
	private World world;

	
	/**
	 * Called when a new player character needs to be created. This constructs
	 * the object, inserts it into the game tree, and returns it.
	 * 
	 * @param name
	 */
	public PlayerCharacter createPlayerCharacter(String name, String description)
	{
		PlayerCharacter character = new PlayerCharacter(name, description);
		if (getWorld().getRooms().size() > 1)
			character.moveTo(getWorld().getRooms().get(1));
		else
			character.moveTo(getWorld().getRooms().get(0));
		character.setLastRoom(character.getLocation());
		return character;
	}
	
	/**
	 * Main entry point to the application. Creates and starts the game.
	 * 
	 * @param args  Command-line arguments to the application
	 */
	public static void main(String[] args)
	{
		new Game().play();		
	}

	
	/**
	 * Runs a newly-constructed game. Loads configuration. Then should just start the server thread, and possibly load a world.
	 */
	private void play()
	{
		loadServerConfig();
		world = new World();
		
		this.serverThread = new ServerThread(this, getPort());
		getServerThread().start();
	}

	/**
	 * Loads the server configuration from the server-config.xml file.
	 */
	private void loadServerConfig()
	{
			File configFile = new File("server-config.xml");
			
				// This is the bit that loads it!
		    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
				try {
					docBuilder = docBuilderFactory.newDocumentBuilder();
					System.out.println("Loading config from:" + configFile.getAbsolutePath());
			        Document doc = docBuilder.parse(configFile);
			        doc.getDocumentElement().normalize();
			        
			        NodeList portList = doc.getDocumentElement().getElementsByTagName("port");
			        if (portList.getLength() > 0)
			        {
				        Element portElement = (Element)portList.item(0);
		                NodeList portChildList =  portElement.getChildNodes();
		                PORT = Integer.parseInt(((Node)portChildList.item(0)).getNodeValue().trim());
			        } else
			        {
			        	System.err.println("Error: <port> not found\n falling back to port " + PORT);
			        }
			        
			        NodeList wmessageList = doc.getDocumentElement().getElementsByTagName("welcome-message");
			        if (wmessageList.getLength() > 0)
			        {
		                Element wmessageElement = (Element)wmessageList.item(0);
		                NodeList wmessageChildList =  wmessageElement.getChildNodes();
		                welcomeMessage = ((Node)wmessageChildList.item(0)).getNodeValue().trim();
			        }
			        else
			        {
			        	System.err.println("Error: <welcome-message> not found\n falling back to default.");
			        }
			        
					System.out.println("Config load sucessful.");
					return;
					
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.err.println("Config load failed. Server will terminate.");
				System.exit(1);
	}
	
	/**
	 * Log an error (to standard error, at the moment).
	 * 
	 * @param error  Error to log
	 */
	public static void logError(String message, Throwable error)
	{	
		System.err.println(message);
		if (error != null)
			error.printStackTrace();
	}
	
	/**
	 * Log a message (to standard output, at the moment).
	 * @param message  Text of message
	 */
	public static void logMessage(String message)
	{	
		if (verbose)
			System.out.println(message);
	}
	
	/**
	 * Returns the thread that is currently listening for connections.
	 */
	public ServerThread getServerThread() {
		return serverThread;
	}

	/**
	 * Returns the port that the server is currently listening for connections on.
	 */
	public static int getPort() {
		return PORT;
	}

	/**
	 * Sets the world that the server is currently running. Ensure that no PlayerCharacters are connected,
	 * as that'll break things.
	 * 
	 * @param world - world to run on the server
	 */
	public void setWorld(World world) {
		this.world = world;
	}

	/**
	 * Returns the world that is currently running on the server.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Sets the message that users see when they connect to the server.
	 * 
	 * @param welcomeMessage - message to display
	 */
	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	/**
	 * Returns the current message that is displayed to users when they connect to the server.
	 */
	public String getWelcomeMessage() {
		return welcomeMessage;
	}
}
