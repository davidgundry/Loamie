package uk.co.thunderbadger.mud;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.co.thunderbadger.mud.net.ServerThread;
import uk.co.thunderbadger.mud.world.NPCharacter;
import uk.co.thunderbadger.mud.world.PlayerCharacter;
import uk.co.thunderbadger.mud.world.World;
import uk.co.thunderbadger.mud.world.npc.Dialogue;

/**
 * Overall control of the game server.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class Game
{
	private static int PORT = 0;
	private String welcomeMessage = "";

	private ServerThread serverThread;

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
		character.moveTo(getWorld().getRooms().get(1));
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
		
		setServerThread(new ServerThread(this, getPort()));
		getServerThread().start();
	}

	/**
	 * Loads the server configuration from the server-config.xml file.
	 * At the moment is reading things as null!
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
	                Element portElement = (Element)portList.item(0);
	                NodeList portChildList =  portElement.getChildNodes();
	                PORT = Integer.parseInt(((Node)portChildList.item(0)).getNodeValue().trim());
	                
			        NodeList wmessageList = doc.getDocumentElement().getElementsByTagName("welcome-message");
	                Element wmessageElement = (Element)wmessageList.item(0);
	                NodeList wmessageChildList =  wmessageElement.getChildNodes();
	                welcomeMessage = ((Node)wmessageChildList.item(0)).getNodeValue().trim();
			        
					System.out.println("Config load sucessful.");
					return;
					
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Config load failed. Server will terminate.");
				System.exit(1);
	}
	
	public void setServerThread(ServerThread serverThread) {
		this.serverThread = serverThread;
	}

	/**
	 * Returns the thread that is currently listening for connections.
	 */
	public ServerThread getServerThread() {
		return serverThread;
	}

	/**
	 * Returns the port that the server is curently listening for connections on.
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
