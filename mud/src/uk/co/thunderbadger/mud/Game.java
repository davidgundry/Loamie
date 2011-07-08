package uk.co.thunderbadger.mud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.thunderbadger.mud.net.ServerThread;
import uk.co.thunderbadger.mud.world.Door;
import uk.co.thunderbadger.mud.world.Item;
import uk.co.thunderbadger.mud.world.NPCharacter;
import uk.co.thunderbadger.mud.world.PlayerCharacter;
import uk.co.thunderbadger.mud.world.Room;
import uk.co.thunderbadger.mud.world.World;
import uk.co.thunderbadger.mud.world.npc.Dialogue;

/**
 * Overall control of the game server.
 * 
 * @author Adam Gundry
 */
public final class Game
{
	private static final int PORT = 1984;

	private ServerThread serverThread;

	private World world;
	
	private String goodbyeMessage = "";
	private String welcomeMessage = "Welcome to the game!\nUseful commands:\nlogin [username]\nhelp";
	/**
	 * Called when a new player character needs to be created. This constructs
	 * the object, inserts it into the game tree, and returns it.
	 * 
	 * @param name
	 * @return
	 */
	public PlayerCharacter createPlayerCharacter(String name, String description)
	{
		PlayerCharacter character = new PlayerCharacter(name, description);
		character.moveTo(getWorld().getRooms().get(1));
		character.setLastRoom(character.getLocation());
		return character;
	}
	
	/**
	 * Main entrypoint to the application. Creates and starts the game.
	 * 
	 * @param args  Command-line arguments to the application
	 */
	public static void main(String[] args)
	{
		new Game().play();		
	}
	
	
	/**
	 * Runs a newly-constructed game. This creates a base room and runs a server thread.
	 */
	private void play()
	{
		// Bert as yet not in any save file
		List<Dialogue> convo2x = new ArrayList<Dialogue>();
		convo2x.add(new Dialogue(1,"\"I will heal you.\"","heal 10 actor"));
		convo2x.add(new Dialogue(2,"\"You said Verb\"","shout dance!"));

		List<Dialogue> convo1x = new ArrayList<Dialogue>();
		convo1x.add(new Dialogue(1,"\"You said Cheese\""));
		convo1x.add(new Dialogue(2,"\"You said Apples\""));
		
		List<Dialogue> convox = new ArrayList<Dialogue>();
		convox.add(new Dialogue(1,"\"I am Bert, an NPC in this world.\"\n1 Cheese\n2 Apples", convo1x));
		convox.add(new Dialogue(2,"\"What do you need?\"\n1 Healing, please.\n2 A verb, please.", convo2x));
		
		getWorld().getRooms().get(1).objectEntered(new NPCharacter("Bert","A small green man.","\"Hi there!\"\n1 Tell me about yourself.\n2 Help me!","Farewell then!",convox));		
		
		
		setServerThread(new ServerThread(this, getPort()));
		getServerThread().start();
	}

	public void setServerThread(ServerThread serverThread) {
		this.serverThread = serverThread;
	}

	public ServerThread getServerThread() {
		return serverThread;
	}

	public static int getPort() {
		return PORT;
	}
	
	public String getGoodbyeMessage(){
		return goodbyeMessage;
	}
	
	public String getWelcomeMessage(){
		return welcomeMessage;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}
}
