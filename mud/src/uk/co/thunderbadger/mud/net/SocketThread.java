package uk.co.thunderbadger.mud.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.thunderbadger.mud.world.PlayerCharacter;
import uk.co.thunderbadger.mud.world.Room;
import uk.co.thunderbadger.mud.world.WorldObject;


/**
 * Handles communications with a single network socket.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class SocketThread extends Thread implements Serializable 
{            		
	private boolean running = false;    	
	private String remoteIP = "unknown";
	
	private transient BufferedReader in;
	private transient PrintStream out;
    private transient ServerThread server;
    private transient Socket socket;
    
    PlayerCharacter character;
    
    /**
     * Create a thread for communication with the given socket,
     * running under the given server.
     * 
     * @param socket  Socket to communicate with
     * @param server  Server controlling threads
     */
    SocketThread(Socket socket, ServerThread server)
    {
    	super("NetThread");
    	
    	this.socket = socket;
		this.server = server;
    }
    
    public PlayerCharacter getCharacter()
    {
    	return character;
    }

    public String getIP()
    {
    	return remoteIP;
    }
    public ServerThread getServer()
    {
    	return server;
    }
    
    /**
     * Shut down the socket connection.     
     */
    public void disconnect()
    {    	    
		if (character != null){
			sendMessage("You are now being placed in Limbo. When you log back on you will be returned to your previous location.");
			character.moveToByID(0);
			sendMessage(server.game.getGoodbyeMessage());
			character.playerDisconnected();
		}
    	logMessage("Connection closed.");	
    	running = false;
        out.close(); 
        try { in.close(); } catch (IOException e) { e.printStackTrace(); }
        try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        
        server.threadDisconnected(this);
    }
    
    
	/**
	 * Log an error which occurred in this thread.
	 * 
	 * @param error  Error to log
	 */
	private void logError(Throwable error)
	{		
		server.logError(String.format("<%s %d> Error occurred:", remoteIP, getId()), error);
	}
	
	
	/**
	 * Log a message about this thread.
	 * 
	 * @param message  Message to log
	 */
	private void logMessage(String message)
	{
		server.logMessage(String.format("<ID %d, address %s> %s", getId(), remoteIP, message));
	}
	
	
	/**
	 * Handle the command received from the client.
	 * 
	 * @param command  Text received
	 */
	private void processCommand(String command)
	{
		logMessage("Command: " + command);				
		
		if (character != null)
		{
			// Commands for logged-in players
			
			// Admin commands
			
			if (command.toLowerCase().startsWith("goto "))
			{
				int roomNo = 0;
				try {
					int value = Integer.parseInt(command.substring(5));
					roomNo = value;
				} catch (NumberFormatException value) {
					sendMessage("That is not a valid room");
					return;
				}
				character.moveToByID(roomNo);
				return;
			}
			
			if (command.toLowerCase().startsWith("create "))
			{
				character.create(command.substring(7));
				return;
			}
			
			if (command.toLowerCase().startsWith("edit "))
			{
				character.edit(command.substring(5));
				return;
			}
			
			if (command.toLowerCase().startsWith("delete "))
			{
				character.delete(command.substring(7));
				return;
			}
			
			if (command.toLowerCase().startsWith("eject "))
			{
				character.eject(command.substring(6));
				return;
			}
			
			if (command.toLowerCase().equals("users"))
			{
				character.userLookUp();
				return;
			}
			
			if (command.toLowerCase().equals("save"))
			{
				getServer().saveWorldState();
				return;
			}
			
			if (command.toLowerCase().equals("savexml"))
			{
				getServer().saveWorldStateToXML();
				return;
			}
			
			
			
			// Player commands
			
			if (command.toLowerCase().startsWith("say "))
			{
				character.say(command.substring(4));
				return;
			}
			
			if (command.toLowerCase().startsWith("shout "))
			{
				character.shout(command.substring(6));
				return;
			}

			if (command.startsWith("*"))
			{
				character.rpAction(command.substring(1));
				return;
			}

			if (command.startsWith("/"))
			{
				character.ownerlessRpAction(command.substring(1));
				return;
			}
			
			if (command.toLowerCase().equals("look") || command.toLowerCase().equals("look around"))
			{
				character.look();
				return;
			}
			
			if (command.toLowerCase().equals("door") || command.toLowerCase().equals("doors") || command.toLowerCase().equals("look doors") || command.toLowerCase().equals("look door") || command.toLowerCase().equals("look at door") || command.toLowerCase().equals("look at doors") || command.toLowerCase().equals("look at the door") || command.toLowerCase().equals("look at the doors"))
			{
				character.receiveMessage(character.getLocation().describeDoors());
				return;
			}
			
			if (command.toLowerCase().equals("use door") || command.toLowerCase().equals("use the door"))
			{
				if (character.getLocation().getDoors().size() == 1){
					character.getLocation().getDoors().get(0).interpretCommand("use", character);
				}
				else
					character.receiveMessage("Which door you mean?");
				return;
			}
			
			if (command.toLowerCase().startsWith("look at "))
			{
				character.objectLook(command.substring(8).toLowerCase());
				return;
			}
			
			if (command.toLowerCase().startsWith("look "))
			{
				character.objectLook(command.substring(5).toLowerCase());
				return;
			}

			if (command.toLowerCase().equals("me"))
			{
				character.receiveMessage(character.getName() + "\n" + character.getDescription());
				return;
			}
			
			if (command.toLowerCase().equals("stat") || command.toLowerCase().equals("stats"))
			{
				character.receiveMessage(character.characterStats());
				return;
			}
			
			if (command.toLowerCase().equals("inven"))
			{
				character.receiveMessage(character.describeContents());

				return;
			}
			
			if (command.toLowerCase().equals("sheet") || command.toLowerCase().equals("character sheet") || command.toLowerCase().equals("character"))
			{
				character.receiveMessage(character.characterSheet());
				return;
			}

			// If no luck, split it up and look for an object. When it finds one, see if that knows what the verb is.
			int retval = 0;
			StringTokenizer st = new StringTokenizer(command.toLowerCase());
			try {
			st.nextToken();
			} catch(NoSuchElementException ex)
			{
				sendMessage("Huh?");
				return;
			}
			    while (st.hasMoreTokens()) {
			    	String nextToTry = st.nextToken();
				    WorldObject object = character.getContentsByName(nextToTry);
				    if (object != null){
					    retval = object.interpretCommand(command.substring(0, command.length()-(nextToTry.length()+1)), character);
					    if (retval == 0)
					    	sendMessage("Inven: You cannot do that to " + object.getName());
				    } else {
				    	WorldObject object2 = character.getLocation().getContentsByName(nextToTry);
					    if (object2 != null){
						    retval = object2.interpretCommand(command.substring(0, command.length()-(nextToTry.length()+1)), character);
						    if (retval == 0)
						    	sendMessage("You cannot do that to " + object2.getName());
					    }
				    }
				}
			if (retval == 1)
				return;

		} else {
			
			
			// Commands for connections which have not logged in
			
			if (command.startsWith("login "))
			{
				for (Room room: server.game.getWorld().getRooms())
				{
					if (room.getContentsByName(command.substring(6)) != null){
						character = (PlayerCharacter) room.getContentsByName(command.substring(6));
						sendMessage("A character has been found by that name.");
						if (character.getLocation() == server.game.getWorld().getRooms().get(0)){
							character.moveTo(character.getLastRoom());
							character.playerConnected(this);
							return;
						} else{
							logMessage(character.getName() + " arrived in an unexpected location. Did the server not shut down properly?");
							character.playerConnected(this);
							return;
						}
					}	
				}
				
				character = server.game.createPlayerCharacter(command.substring(6), "As yet completely undescribed and unremarkable.");
				sendMessage("A new character has been created.");
				character.playerConnected(this);
				return;
			}
			
			if (command.toLowerCase().equals("restore"))
			{
				sendMessage(getServer().restoreWorldState());
				return;
			}
			
			if (command.toLowerCase().equals("restorexml"))
			{
				sendMessage(getServer().restoreWorldStateFromXML());
				return;
			}

		}
		
		// Commands for everybody
		
		if (command.equals("quit"))
		{
			disconnect();
			return; 
		}
		
		
		if (command.equals("help"))
		{
			sendMessage("Console Commands:\nlogin [username]\nhelp\nquit\n");
			sendMessage("\nGame Commands:\nsay\nshout\n* (puts username infront. Eg. * dances = Fred dances)\n/ (Sends message with no atribution)\nlook\nlook at [object]\nstat (show your stats)\ndoors (Print info about doors)\n \nMany objects in the world accept many more commands, you'll just have to find them.");
			sendMessage("\nAdmin Commands:\ngoto [roomNo]\ncreate [room/door to [roomNo]] [name] [description]\nedit [room/name] [newName] [newDescription]\ndelete [room/name]\neject [name/all] to [roomNo]");
			return; 
		}
		
		// If we got here, the command wasn't understood
		
		sendMessage("Huh?");
	}
      
	
    /**
     * Activate this network connection and loop while reading messages.
     * Notify all the network listeners when we receive data.
     * To stop the loop, set running to false.
     */
    @Override
    public void run()
    {
    	try
    	{
    		// Set up the connection
    		out = new PrintStream(socket.getOutputStream());
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));                           
            remoteIP = socket.getInetAddress().getHostAddress();                      
            
            logMessage("Accepted connection.");
            sendMessage(server.game.getWelcomeMessage());
            
    		running = true;
    		
	        while (running)
	        {
	        	String command = in.readLine();	        	
	        	if (command == null)
	        		throw new IOException("Stream closed unexpectedly. The client probably disconnected without quitting first.");	        	
		        
	        	if (character != null)
		        	if (character.getListener() != null)
		        		character.getListener().listenToCommand(command, character);
		        	else
		        		processCommand(command);    
	        	else
	        		processCommand(command);
	        }
    	} catch (IOException ex) {
    		logError(ex);
    		disconnect();
    	}
    }

    /**
     * Send a message object to the remote socket.
     * 
     * @param message  Message object to send
     */
    public void sendMessage(String message)
    {
    	String[] lines = message.split("[\\r\\n]+");
		for(int i=0;i<lines.length;i++)
			out.println("          " + lines[i]);
    }
}






