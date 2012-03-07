package uk.co.gundry.david.mud.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import uk.co.gundry.david.mud.Game;
import uk.co.gundry.david.mud.world.World;


/**
 * Main network server class for the MUD.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class ServerThread extends Thread
{	

	private int port;
	
	/**
	 * ArrayList of socket connections to the server
	 */
	private transient List<SocketThread> threads = Collections.synchronizedList(new ArrayList<SocketThread>());		
	
	/**
	 * Creates a server on the given port.
	 * 
	 * @param port  Port number to run server on
	 */
	public ServerThread(int port)
	{
		this.port = port;
	}
	
	public List<SocketThread> getThreads()
	{
		return threads;
	}
		
	/**
	 * Listens on the server's port, waits for clients to connect
	 * and generates a SocketThread for each.
	 */
	@Override
	public void run()
	{
		Game.logMessage("Starting server on port " +port + "...");
		
		ServerSocket serverSocket = null;
		boolean listening = true;
		
		try
		{
			serverSocket = new ServerSocket(port);
		} catch (BindException e) {
			Game.logError("Unable to bind to port " + port + ". Make sure no other programs (or instances of this program) are using that port.",e);
			System.exit(-1);
		} catch (IOException e) {
			Game.logError("Failed to listen on port " + port,e);
			System.exit(-1);
		}
		
		try
		{
			while (listening)
			{
				SocketThread thread = new SocketThread(serverSocket.accept(), this);
				threads.add(thread);
				thread.init();		
				thread.start();
				
				if (threads.size() > Game.getMaxConnections())
				{
			    	thread.sendMessage("Sorry, the server is full.\nYou will now be disconnected.");
			    	thread.logMessage("Server full.");
			    	thread.disconnect();
				}
			}
		} catch (IOException e) {
			Game.logError("Exception occurred while accepting connections, terminating.",e);
			System.exit(-1);
		}
		
		try
		{
			serverSocket.close();
		} catch (IOException e) {
			Game.logError("Exception occurred while closing socket.",e);
			System.exit(-1);
		}
	}
	
	/**
	 * Sends a message to all the connected clients.
	 * 
	 * @param message  Message object to broadcast
	 */
	public void sendMessage(String message, SocketThread sender)
	{
		synchronized(threads)
		{
			for (SocketThread thread: threads)
			{
				thread.sendMessage(message);
			}
		}
	}
	
	/**
	 * Called when a thread has been disconnected, whether voluntarily or not.
	 * Removes the thread from the set.
	 * 
	 * @param thread  Thread that just disconnected
	 */
	void threadDisconnected(SocketThread thread)
	{
		threads.remove(thread);
	}
	
	/**
	 * Saves the current state of the game to a file.
	 * 
	 */
/*	public void saveWorldState()
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		for (SocketThread thread: threads)
			thread.sendMessage("Game state is being saved.");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			File f=new File(filePrefix + dateFormat.format(new Date()) + ".save");
			Game.logMessage("Saving to:" + f.getAbsolutePath());
			new File(filePrefix).mkdirs();
		    f.createNewFile();
			fos = new FileOutputStream(filePrefix + dateFormat.format(new Date()) + ".save");
			out = new ObjectOutputStream(fos);
			out.writeObject(this.getGame().getWorld());
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}*/
	
	/**
	 * Loads the game state from the latest save file.
	 * 
	 * @return message
	 */
	/*public String restoreWorldState()
	{		
		if (threads.size() == 1){
			FileInputStream fis = null;
			ObjectInputStream in = null;
			
			File dir = new File(filePrefix);
			String[] children = dir.list(); 
			if (children == null) {
				return "No save files present!";
			} else {
				List<Date> saveDate = new ArrayList<Date>();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				for (String child: children)
				{
			        try {
						saveDate.add(df.parse(child));   
			        }
			        catch (ParseException e) {
			            e.printStackTrace();
			        }
				}
				
				if (saveDate.size() == 0)
					return "No save files present!";
				
				Date latestDate = saveDate.get(0);
				
				if (saveDate.size() == 1) {
				} else{
					for (int i=1;i<saveDate.size();i++){
						if (saveDate.get(i).after(latestDate)){
							latestDate = saveDate.get(i);
						}
					}
				}
				try
				{
			    	fis = new FileInputStream(filePrefix + df.format(latestDate) + ".save");
			    	in = new ObjectInputStream(fis);
			    	this.getGame().setWorld((World) in.readObject());
			    	in.close();
				}
			   catch(IOException ex)
			   {
			     ex.printStackTrace();
			     return "Restore Failed! (Error reading save file.)";
			   }
			   catch(ClassNotFoundException ex)
			   {
			     ex.printStackTrace();
			     return "Restore Failed! (Was the file saved in a different version of this program?)";
			   }			
				 Game.logMessage("Restored to state at: " + df.format(latestDate) + ".");
			   return "Sucessfully restored to state at: " + df.format(latestDate) + ".";
			}
		} else {
			return "There are players logged in! Restore doesn't work when players are logged in, as things get messed up. All players should be disconnected.";
		}
	}*/

	/**
	 * Attempts to write the current World state to XML, so it can be restored later.
	 */
	public void saveWorldStateToXML()
	{
		FileOutputStream fos = null;
		PrintStream ps;
		
		for (SocketThread thread: threads)
			thread.sendMessage("Game world is being saved to xml.");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			File f=new File(Game.getWorldSaveLocation() + dateFormat.format(new Date()) + ".xml");
			Game.logMessage("Saving to:" + f.getAbsolutePath());
			new File(Game.getWorldSaveLocation()).mkdirs();
		    f.createNewFile();
			fos = new FileOutputStream(Game.getWorldSaveLocation() + dateFormat.format(new Date()) + ".xml");
			ps = new PrintStream(fos);
			
			Game.getWorld().saveStateToXML(ps);
		
			ps.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	/**
	 * Tries to load the XML file, then if successful, will create a new World object, finally replacing the current
	 * world object with the new one.
	 * 
	 * This does not work when the players are logged in, as it can really screw things up.
	 * 
	 */
	public String restoreWorldStateFromXML()
	{
		if (threads.size() <= 1){
			
			File dir = new File(Game.getWorldSaveLocation());
			String[] children = dir.list(); 
			if (children == null) {
				return "XML folder is empty or does not exist!\nTried: " + dir.getAbsolutePath();
			} else {
				List<Date> saveDate = new ArrayList<Date>();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				for (String child: children)
				{
			        try {
						saveDate.add(df.parse(child));   
			        }
			        catch (ParseException e) {
			            e.printStackTrace();
			        }
				}
				
				if (saveDate.size() == 0)
					return "No save files present!";
				
				Date latestDate = saveDate.get(0);
				
				if (saveDate.size() == 1) {
				} else{
					for (int i=1;i<saveDate.size();i++){
						if (saveDate.get(i).after(latestDate)){
							latestDate = saveDate.get(i);
						}
					}
				}
			
				// This is the bit that loads it!
		    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder docBuilder;
				try {
					docBuilder = docBuilderFactory.newDocumentBuilder();
					Game.logMessage(Game.getWorldSaveLocation() + df.format(latestDate) + ".xml");
			        Document doc = docBuilder.parse (new File(Game.getWorldSaveLocation() + df.format(latestDate) + ".xml"));
			        doc.getDocumentElement().normalize();
					
			        Game.logMessage("Attempting to restore to state at " + df.format(latestDate));
			        
			        World newWorld = Game.getWorld().restoreStateFromXML(doc);
			        if (newWorld != null)
			        {
						Game.setWorld(newWorld);
						return "Restore sucessful.";
			        }
			        else
			        	return "Restore failed. Check server log.";
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "Restore failed.";
			}
		} else {
			return "There are players logged in! Restore doesn't work when players are logged in, as things get messed up. All players should be disconnected first.";
		}
	}
	
	/**
	 * Returns the number of players currently logged in.
	 * @return
	 */
	public int countLogins()
	{
		int n=0;
		for (SocketThread thread: getThreads())
			if (thread.loggedIn()) n++;
		
		return n;
	}
	
	/**
	 * Returns the number of socket threads running on the server.
	 * @return
	 */
	public int countConnections()
	{
		return getThreads().size();
	}
	
	}
