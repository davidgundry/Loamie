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

package uk.co.gundry.david.loamie.mud.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.gundry.david.loamie.mud.Game;
import uk.co.gundry.david.loamie.mud.world.PlayerCharacter;
import uk.co.gundry.david.loamie.mud.world.WorldObject;
import uk.co.gundry.david.loamie.mud.world.command.AdminCommandInterpreter;
import uk.co.gundry.david.loamie.mud.world.command.PlayerCommandInterpreter;
import uk.co.gundry.david.loamie.mud.world.command.UnLoggedInCommandInterpreter;


/**
 * Handles communications with a single network socket.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class SocketThread extends Thread
{            		
	private boolean running = false;    	
	private String remoteIP = "unknown";
	
	private transient BufferedReader in;
	private transient PrintStream out;
    private transient ServerThread serverThread;
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
		this.serverThread = server;
    }
    
    public PlayerCharacter getCharacter()
    {
    	return character;
    }

    public String getIP()
    {
    	return remoteIP;
    }
    public ServerThread getServerThread()
    {
    	return serverThread;
    }
    
    /**
     * Shut down the socket connection.     
     */
    public void disconnect()
    {    	    
		if (loggedIn()){
			sendMessage("You are now being placed in Limbo. When you log back on you will be returned to your previous location.");
			character.moveToByID(0);
			sendMessage(Game.getWorld().getGoodbyeMessage());
			character.playerDisconnected();
		}
    	logMessage("Connection closed.");	
    	running = false;
        out.close(); 
        try { in.close(); } catch (IOException e) { e.printStackTrace(); }
        try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        
        serverThread.threadDisconnected(this);
    }
    
	/**
	 * Log an error which occurred in this thread.
	 * 
	 * @param error  Error to log
	 */
	private void logError(Throwable error)
	{		
		Game.logError(String.format("<%s %d> Error occurred:", remoteIP, getId()), error);
	}
	
	
	/**
	 * Log a message about this thread.
	 * 
	 * @param message  Message to log
	 */
	public void logMessage(String message)
	{
		Game.logMessage(String.format("<ID %d, address %s> %s", getId(), remoteIP, message));
	}
	
	
	/**
	 * Handle the command received from the client.
	 * 
	 * @param command  Text received
	 */
	private void processCommand(String command)
	{
		logMessage("Command: " + command);				
		
		// Commands for logged-in players
		if (character != null)
		{
			//Admin Commands
			if (new AdminCommandInterpreter().interpret(command, this))
				return;
			//Player Commands
			if (new PlayerCommandInterpreter().interpret(command, this))
				return;

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
			if (new UnLoggedInCommandInterpreter().interpret(command, this))
				return;

		}
		
		// Commands for everybody
		
		if (command.equals("quit"))
		{
			disconnect();
			return; 
		}
		if (command.equals("help"))
		{
			if (character != null)
			{
				sendMessage(Game.getHelpGameCommands());
				sendMessage(Game.getHelpAdminCommands());
			}
			
			sendMessage(Game.getHelpConsoleCommands());
				
			return; 
		}
		
		// If we got here, the command wasn't understood
		sendMessage("Huh?");
	}
      
	public void init()
	{
		try
    	{
    		// Set up the connection
    		out = new PrintStream(socket.getOutputStream());
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));                           
            remoteIP = socket.getInetAddress().getHostAddress();                      
            
            logMessage("Accepted connection.");
            sendMessage(Game.getWelcomeMessage());
            sendMessage(Game.getWorld().getWelcomeMessage());
            
    		running = true;
    	} catch (IOException ex) {
    		logError(ex);
    		disconnect();
    	}
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
   		}
	    catch(IOException e)
	    {
	    	logError(e);
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

    /**
     * Set the character object that is associated with this socket.
     * 
     * @param character
     */
	public void setCharacter(PlayerCharacter character) {
		this.character = character;
		
	}
	
	/**
	 * Returns true if the player has a character, false otherwise
	 * @return
	 */
	public boolean loggedIn()
	{
		return (character != null);
	}
}






