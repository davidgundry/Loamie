/*
Loamie - A MUD Engine
Copyright (C) 2012  David Gundry, Adam Gundry

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package uk.co.gundry.david.loamie.mud;

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

import uk.co.gundry.david.loamie.mud.net.ServerThread;
import uk.co.gundry.david.loamie.mud.world.PlayerCharacter;
import uk.co.gundry.david.loamie.mud.world.World;

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
	private static int port = 1357;
	/**
	 * This is the message players see when they first connect to the server.
	 * The default should be overwritten by a value loaded from the server config file.
	 */
	private static String welcomeMessage = "Welcome to the server!\n Type help for a list of commands.";
	/**
	 * This is a string about commands for users not logged in.
	 * A value should be loaded from the server config file.
	 */
	private static String helpConsoleCommands;
	/**
	 * This is a string about commands for admins and those with access to admin commands
	 * A value should be loaded from the server config file.
	 */
	private static String helpAdminCommands;
	/**
	 * This is a string about commands for logged in players.
	 * A value should be loaded from the server config file.
	 */
	private static String helpGameCommands;
	/**
	 * This is the location of the world save files
	 * It must be loaded correctly from the server config file for the server to run.
	 */
	private static String worldSaveLocation;
	/**
	 * This is the total number of socketThreads this server will allow at any one time.
	 * The default value should be overwritten by a value loaded from the server config file.
	 */
	private static int maxConnections = 100;
	/**
	 * When the server is running in verbose mode, it echos every command it receives.
	 */
	private static boolean verbose = true;	
	/**
	 * When the server is running in debug mode, it prints more (mostly unnecesary) information
	 * for debugging purposes.
	 */
	private static boolean debug = false;	
	/**
	 * This is the world currently running on the server.
	 */
	private static World world;

	
	/**
	 * Called when a new player character needs to be created. This constructs
	 * the object, inserts it into the game tree, and returns it.
	 * 
	 * @param name
	 */
	public static PlayerCharacter createPlayerCharacter(String name, String description)
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
		
		ServerThread serverThread = new ServerThread(getPort());
		serverThread.start();
		logMessage(serverThread.restoreWorldStateFromXML());
		if (!hasWorldLoaded())
		{
			logError("World not found. Quitting.", null);
			System.exit(1);
		}
		
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
					logMessage("Loading config from:" + configFile.getAbsolutePath());
			        Document doc = docBuilder.parse(configFile);
			        doc.getDocumentElement().normalize();
			        
			        NodeList portList = doc.getDocumentElement().getElementsByTagName("port");
			        if (portList.getLength() > 0)
			        {
				        Element portElement = (Element)portList.item(0);
		                NodeList portChildList =  portElement.getChildNodes();
		                port = Integer.parseInt(((Node)portChildList.item(0)).getNodeValue().trim());
			        } else
			        {
			        	logError("Error: <port> not found\nfalling back to port " + port,null);
			        }
			        
			        NodeList maxConList = doc.getDocumentElement().getElementsByTagName("max-connections");
			        if (maxConList.getLength() > 0)
			        {
				        Element maxConElement = (Element)maxConList.item(0);
		                NodeList maxConChildList =  maxConElement.getChildNodes();
		                maxConnections = Integer.parseInt(((Node)maxConChildList.item(0)).getNodeValue().trim());
			        } else
			        {
			        	logError("Error: <max-connections> not found\nfalling back to default: " + maxConnections,null);
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
			        	logError("Error: <welcome-message> not found\nfalling back to default.",null);
			        }
			        
			        NodeList worldName = doc.getDocumentElement().getElementsByTagName("world");
			        if (worldName.getLength() > 0)
			        {
		                Element worldNameElement = (Element)worldName.item(0);
		                NodeList worldNameChildList =  worldNameElement.getChildNodes();
		                worldSaveLocation = ((Node)worldNameChildList.item(0)).getNodeValue().trim() + "/";
			        }
			        else
			        {
			        	logError("Error: <world> not found\n. Falling back to \"world\"",null);
			        	worldSaveLocation = "world" + "/";
			        }
			        
			        NodeList help = doc.getDocumentElement().getElementsByTagName("help-console");
			        if (help.getLength() > 0)
			        {
		                Element helpElement = (Element)help.item(0);
		                NodeList helpChildList =  helpElement.getChildNodes();
		                helpConsoleCommands = ((Node)helpChildList.item(0)).getNodeValue().trim();
			        }
			        else
			        {
			        	logError("Error: <help-console> not found\nIt is advised to specify help information.",null);
			        }
			        
			        help = doc.getDocumentElement().getElementsByTagName("help-admin");
			        if (help.getLength() > 0)
			        {
		                Element helpElement = (Element)help.item(0);
		                NodeList helpChildList =  helpElement.getChildNodes();
		                helpAdminCommands = ((Node)helpChildList.item(0)).getNodeValue().trim();
			        }
			        else
			        {
			        	logError("Error: <help-admin> not found\nIt is advised to specify help information.",null);
			        }
			        
			        help = doc.getDocumentElement().getElementsByTagName("help-game");
			        if (help.getLength() > 0)
			        {
		                Element helpElement = (Element)help.item(0);
		                NodeList helpChildList =  helpElement.getChildNodes();
		                helpGameCommands = ((Node)helpChildList.item(0)).getNodeValue().trim();
			        }
			        else
			        {
			        	logError("Error: <help-game> not found\nIt is advised to specify help information.",null);
			        }
			        
			        
					logMessage("Config load sucessful.");
					return;
					
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				logError("Config load failed. Server will terminate.", null);
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
		if (isVerbose())
			System.out.println(message);
	}
	
	/**
	 * Returns the port that the server is currently listening for connections on.
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * Sets the world that the server is currently running. Ensure that no PlayerCharacters are connected,
	 * as that'll break things.
	 * 
	 * @param world - world to run on the server
	 */
	public static void setWorld(World w) {
		world = w;
	}

	/**
	 * Returns the world that is currently running on the server.
	 */
	public static World getWorld() {
		return world;
	}

	/**
	 * Sets the message that users see when they connect to the server.
	 * 
	 * @param welcomeMessage - message to display
	 */
	public void setWelcomeMessage(String wm) {
		welcomeMessage = wm;
	}

	/**
	 * Returns the current message that is displayed to users when they connect to the server.
	 */
	public static String getWelcomeMessage() {
		return welcomeMessage;
	}

	/**
	 * Returns the console help string that is loaded from the server-config.xml file
	 * @return
	 */
	public static String getHelpConsoleCommands() {
		return helpConsoleCommands;
	}
	
	/**
	 * Returns the admin help string that is loaded from the server-config.xml file
	 * @return
	 */
	public static String getHelpAdminCommands() {
		return helpAdminCommands;
	}
	
	/**
	 * Returns the in-game help string that is loaded from the server-config.xml file
	 * @return
	 */
	public static String getHelpGameCommands() {
		return helpGameCommands;
	}

	/**
	 * Returns the path to the location to save the world files
	 * @return
	 */
	public static String getWorldSaveLocation() {
		return worldSaveLocation;
	}
	
	/**
	 * Returns true if the server is running a world, false otherwise.
	 */
	public boolean hasWorldLoaded()
	{
		return (world != null);
	}

	/**
	 * Returns the max concurrent connections allowed by the server.
	 */
	public static int getMaxConnections() {
		return maxConnections;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static boolean isVerbose() {
		return verbose;
	}
}
