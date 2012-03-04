package uk.co.thunderbadger.mud.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
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

import uk.co.thunderbadger.mud.Game;
import uk.co.thunderbadger.mud.world.World;


/**
 * Main network server class for the MUD.
 * 
 * @author Adam Gundry, extended by David Gundry
 */
public final class ServerThread extends Thread implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	Game game;
	
	private int port;
	private boolean verbose = true;	
	
	private transient List<SocketThread> threads = Collections.synchronizedList(new ArrayList<SocketThread>());		
	
	private String filePrefix = System.getProperty("user.dir") + "//save//";
	private String XMLFilePrefix = System.getProperty("user.dir") + "//xml//";
	
	/**
	 * Creates a server on the given port.
	 * 
	 * @param port  Port number to run server on
	 */
	public ServerThread(Game game, int port)
	{
		this.game = game;
		this.port = port;
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public List<SocketThread> getThreads()
	{
		return threads;
	}
	
	/**
	 * Log an error (to standard error, at the moment).
	 * 
	 * @param error  Error to log
	 */
	public void logError(String message, Throwable error)
	{	
		System.err.println(message);
		error.printStackTrace();
	}
	
	/**
	 * Log a message (to standard output, at the moment).
	 * @param message  Text of message
	 */
	public void logMessage(String message)
	{	
		if (verbose)
			System.out.println(message);
	}
	
	/**
	 * Listens on the server's port, waits for clients to connect
	 * and generates a SocketThread for each.
	 */
	@Override
	public void run()
	{
		logMessage("Starting server on port " +port + "...");
		
		ServerSocket serverSocket = null;
		boolean listening = true;
		
		try
		{
			serverSocket = new ServerSocket(port);
		} catch (BindException e) {
			logMessage("Unable to bind to port " + port + ". Make sure no other programs (or instances of this program) are using that port.");
			System.exit(-1);
		} catch (IOException e) {
			logMessage("Failed to listen on port " + port);
			e.printStackTrace();
			System.exit(-1);
		}
		
		try
		{
			while (listening)
			{
				SocketThread thread = new SocketThread(serverSocket.accept(), this);
				threads.add(thread);
				thread.start();		
			}
		} catch (IOException e) {
			logMessage("Exception occurred while accepting connections, terminating.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		try
		{
			serverSocket.close();
		} catch (IOException e) {
			logMessage("Exception occurred while closing socket.");
			e.printStackTrace();
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
	public void saveWorldState()
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		for (SocketThread thread: threads)
			thread.sendMessage("Game state is being saved.");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			File f=new File(filePrefix + dateFormat.format(new Date()) + ".save");
			logMessage("Saving to:" + f.getAbsolutePath());
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
	}
	
	/**
	 * Loads the game state from the latest save file.
	 * 
	 * @return message
	 */
	public String restoreWorldState()
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
			     return "Restore Failed! (ClassNotFoundException)";
			   }			
			   logMessage("Restored to state at: " + df.format(latestDate) + ".");
			   return "Sucessfully restored to state at: " + df.format(latestDate) + ".";
			}
		} else {
			return "There are players logged in! Restore doesn't work when players are logged in, as things get messed up. All players should be disconnected.";
		}
	}

	
	public void saveWorldStateToXML()
	{
		FileOutputStream fos = null;
		PrintStream ps;
		
		for (SocketThread thread: threads)
			thread.sendMessage("Game world is being saved to xml.");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			File f=new File(XMLFilePrefix + dateFormat.format(new Date()) + ".xml");
			logMessage("Saving to:" + f.getAbsolutePath());
			new File(XMLFilePrefix).mkdirs();
		    f.createNewFile();
			fos = new FileOutputStream(XMLFilePrefix + dateFormat.format(new Date()) + ".xml");
			ps = new PrintStream(fos);
			
			getGame().getWorld().saveStateToXML(ps);
		
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
		if (threads.size() == 1){
			
			File dir = new File(XMLFilePrefix);
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
					logMessage(XMLFilePrefix + df.format(latestDate) + ".xml");
			        Document doc = docBuilder.parse (new File(XMLFilePrefix + df.format(latestDate) + ".xml"));
			        doc.getDocumentElement().normalize();
			        logMessage("Root element of the doc is " + doc.getDocumentElement().getNodeName());
					
			        logMessage("Attempting to restore to state at " + df.format(latestDate));
			        
			        World newWorld = getGame().getWorld().restoreStateFromXML(doc);
			        if (newWorld != null)
			        {
						getGame().setWorld(newWorld);
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
			return "There are players logged in! Restore doesn't work when players are logged in, as things get messed up. All players should be disconnected.";
		}
	}
	}
