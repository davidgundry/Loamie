package uk.co.gundry.david.loamie.mud.world.command;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.gundry.david.loamie.mud.Game;
import uk.co.gundry.david.loamie.mud.net.SocketThread;
import uk.co.gundry.david.loamie.mud.world.Door;
import uk.co.gundry.david.loamie.mud.world.Room;
import uk.co.gundry.david.loamie.mud.world.WorldObject;

public class AdminCommandInterpreter implements CommandInterpreter {

	/**
	 * A Command Interpreter specifically for administrator commands, being those that modify the base world
	 * such as creating and editing rooms, and those that save the world state, and those that allow control
	 * over players connected to the server.
	 * 
	 * @param command
	 * @param connection
	 */
	public boolean interpret(String command, SocketThread connection)
	{
		if (command.toLowerCase().startsWith("goto "))
		{
			int roomNo = 0;
			try {
				int value = Integer.parseInt(command.substring(5));
				roomNo = value;
			} catch (NumberFormatException value) {
				connection.getCharacter().receiveMessage("That is not a valid room");
				return true;
			}
			connection.getCharacter().moveToByID(roomNo);
			return true;
		}
		
		if (command.toLowerCase().startsWith("create "))
		{
			create(command.substring(7),connection);
			return true;
		}
		
		if (command.toLowerCase().startsWith("edit "))
		{
			edit(command.substring(5),connection);
			return true;
		}
		
		if (command.toLowerCase().startsWith("delete "))
		{
			delete(command.substring(7),connection);
			return true;
		}
		
		if (command.toLowerCase().startsWith("eject "))
		{
			eject(command.substring(6),connection);
			return true;
		}
		
		if (command.toLowerCase().equals("users"))
		{
			userLookUp(connection);
			return true;
		}
		
		/*if (command.toLowerCase().equals("save"))
		{
			connection.getServerThread().saveWorldState();
			return true;
		}*/
		
		if (command.toLowerCase().equals("save"))
		{
			connection.getServerThread().saveWorldStateToXML();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Players (Socket Connections) can modify the game world live. If the player issues a 'create' command,
	 * this code is run. It interprets the arguments given to the 'create' command.
	 * 
	 * @param blueprint
	 */
	public void create(String blueprint, SocketThread thread)
	{
		int creationType = 0;
		int creationTargetRoomNo = 0;
		Room creationTarget = new Room();
		String creationName;
		
		if (blueprint.startsWith("room ")){
			blueprint = blueprint.substring(5);
			creationType = 2;
		}
		else if (blueprint.startsWith("door to ")){
			blueprint = blueprint.substring(8);
			creationType = 3;
			String roomNoVal;
			StringTokenizer st = new StringTokenizer(blueprint);
			try {
				roomNoVal = st.nextToken();
			} catch (NoSuchElementException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			try {
				creationTargetRoomNo = Integer.parseInt(roomNoVal);
			} catch (NumberFormatException ex) {
				thread.sendMessage("That is not a valid room");
				return;
			}
			if (creationTargetRoomNo < Game.getWorld().getRooms().size()){
				creationTarget = Game.getWorld().getRooms().get(creationTargetRoomNo);
			} else{
				thread.sendMessage("That room does not exist!");
				return;
			}
			blueprint = blueprint.substring(roomNoVal.length()+1);
		} else{
			thread.sendMessage("What do you want to create?");
		}
		
		// Get the name from the blueprint
		StringTokenizer st = new StringTokenizer(blueprint);
		try {
			creationName = st.nextToken();
		} catch (NoSuchElementException ex) {
			thread.sendMessage("Wrong syntax!");
			return;
		}
		try{
			blueprint = blueprint.substring(creationName.length()+1);
		} catch (StringIndexOutOfBoundsException ex)
		{
			thread.sendMessage("Wrong syntax!");
			return;
		}
		if (creationType == 2) Game.getWorld().getRooms().add(new Room(creationName, blueprint));
		if (creationType == 3) thread.getCharacter().getLocation().objectEntered(new Door(creationName, blueprint, creationTarget));
	}
	
	/**
	 * Players (Socket Connections) can modify the game world live. If the player issues an 'edit' command,
	 * this code is run. It interprets the arguments given to the 'edit' command and changes the
	 * world accordingly.
	 * 
	 * @param blueprint
	 */
	public void edit(String blueprint, SocketThread thread)
	{
		if (blueprint.startsWith("room ")){
			blueprint = blueprint.substring(5);
			
			// Get the name from the blueprint
			StringTokenizer st = new StringTokenizer(blueprint);
			String editName;
			try {
				editName = st.nextToken();
			} catch (NoSuchElementException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			try{
				blueprint = blueprint.substring(editName.length()+1);
			} catch (StringIndexOutOfBoundsException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			thread.getCharacter().getLocation().setName(editName);
			thread.getCharacter().getLocation().setDescription(blueprint);
			return;
		}		
		else {
			// Get targetName from the blueprint
			StringTokenizer st = new StringTokenizer(blueprint);
			String targetName;
			try {
				targetName = st.nextToken();
			} catch (NoSuchElementException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			try{
				blueprint = blueprint.substring(targetName.length()+1);
			} catch (StringIndexOutOfBoundsException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			
			// Get the name from the blueprint
			String editName;
			try{
				editName = st.nextToken();
			} catch (NoSuchElementException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			try{
				blueprint = blueprint.substring(editName.length()+1);
			} catch (StringIndexOutOfBoundsException ex) {
				thread.sendMessage("Wrong syntax!");
				return;
			}
			
			if (thread.getCharacter().getLocation().getContentsByName(targetName) != null){
				thread.getCharacter().getLocation().getContentsByName(targetName).setDescription(blueprint);
				thread.getCharacter().getLocation().getContentsByName(targetName).setName(editName);
			}
			else if (thread.getCharacter().getLocation().getName().equals(targetName)){
				thread.getCharacter().getLocation().setDescription(blueprint);
				thread.getCharacter().getLocation().setName(editName);
			} else
				thread.sendMessage("Cannot find " +targetName);
			return;
			}		
	}
	
	/**
	 * Players (Socket Connections) can modify the game world live. If the player issues an 'delete' command,
	 * this code is run. It interprets the arguments given to the 'delete' command and changes the
	 * world accordingly.
	 * 
	 * This removes all objects in the room, gets rid of all doors, and marks the room for deletion.
	 * In theory no one can reenter the room without admin powers, and it will be skipped on the next save.
	 * 
	 * @param blueprint
	 */
	public void delete(String blueprint, SocketThread thread)
	{
		if (blueprint.equals("room")){
			if (thread.getCharacter().getLocation() != Game.getWorld().getRooms().get(0))
			{ // You really don't want to delete Limbo
				Room roomToDelete = (Room) thread.getCharacter().getLocation();
				for (Door door: roomToDelete.getDoors())
				{
					roomToDelete.objectExited(door);
				}
				roomToDelete.ejectContents(Game.getWorld().getRooms().get(0));

				roomToDelete.setName("DELETE" + roomToDelete.getName());
				//this.thread.getServerThread().getGame().getWorld().getRooms().remove(roomToDelete);
				return;
			}
		}		
		else {
			WorldObject object = thread.getCharacter().getLocation().getContentsByName(blueprint);
			if (object != null)
				thread.getCharacter().getLocation().objectExited(object);
			return;
			}		
	}
	
	/**
	 * Interprets an 'eject' command, which forcibly moves an object, or the entire contents of a room
	 * to a room specified within the command.
	 * 
	 * @param text
	 */
	public void eject(String text, SocketThread thread)
	{
		StringTokenizer st = new StringTokenizer(text);
		String victimName;
		try {
		victimName = st.nextToken();
		} catch (NoSuchElementException ex) {
			thread.sendMessage("Wrong syntax!");
			return;	
		}
		try{
			text = text.substring(victimName.length()+1);
		} catch (StringIndexOutOfBoundsException ex) {
			thread.sendMessage("Wrong syntax!");
			return;
		}
		int target;
		try {
			target = Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			thread.sendMessage("That is not a valid room");
			return;
		}
		target = Math.abs(target);
		if (target >= Game.getWorld().getRooms().size()){
			thread.sendMessage("That is not a valid room");
			return;
		}
		
		if (victimName.equals("all"))
			thread.getCharacter().getLocation().ejectContents(Game.getWorld().getRooms().get(target));
		else 
			if (thread.getCharacter().getLocation().getContentsByName(victimName) != null)
				thread.getCharacter().getLocation().getContentsByName(victimName).moveTo(Game.getWorld().getRooms().get(target));
			else thread.sendMessage("Cannot find " + victimName);
	}
	
	public void userLookUp(SocketThread thread)
	{
		thread.sendMessage(thread.getServerThread().countConnections() + " / " + Game.getMaxConnections() + " users connected.");
		thread.sendMessage(thread.getServerThread().countLogins() + " users currently playing.");
		thread.sendMessage("IP Address       Username");
		for (SocketThread user: thread.getServerThread().getThreads()){
			thread.sendMessage(user.getIP() + "		" + user.getCharacter().getName());
		}
	}
	
}
