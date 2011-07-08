package uk.co.thunderbadger.mud.world;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.thunderbadger.mud.net.SocketThread;

/**
 * Represents a character that can be controlled by a human (well, a socket connection, at any rate).
 * Note that the character need not be controlled all the time.
 * 
 * @author Adam Gundry
 */
public final class PlayerCharacter extends GameCharacter
{	
	
	private transient volatile SocketThread thread;
	
	/**
	 * Create a new player character with the given name.
	 * 
	 * @param name
	 */
	public PlayerCharacter(String name, String description)
	{
		super(name, description);
	}
	
	/**
	 * If a thread is attached, forwards the message through the socket.
	 */
	@Override
	public void receiveMessage(String text)
	{
		if (thread != null){
			String[] lines = text.split("[\\r\\n]+");
			for(int i=0;i<lines.length;i++){
				thread.sendMessage("          " + lines[i].replace("_"," "));
			}
		}
	}
	
	/**
	 * If a thread is attached, forwards the message through the socket.
	 */
	@Override
	public void receiveMessageFromPlayer(String text)
	{
		if (thread != null){
			String[] lines = text.split("[\\r\\n]+");
			for(int i=0;i<lines.length;i++){
				thread.sendMessage("                    " + lines[i].replace("_"," "));
			}
		}
	}
	
	/**
	 * Called when a player connects to control this character.
	 * 
	 * @param thread  Controlling socket thread
	 */
	public void playerConnected(SocketThread thread)
	{
		this.thread = thread;
		
		receiveMessage("Welcome to the game, " + name + "!");
	}
	
	/**
	 * Called when the attached thread disconnects.	 
	 */
	public void playerDisconnected()
	{
	//	location.objectExited(this);
		this.thread = null;
	}
	
	
	public void create(String blueprint)
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
			if (creationTargetRoomNo < this.thread.getServer().getGame().getWorld().getRooms().size()){
				creationTarget = this.thread.getServer().getGame().getWorld().getRooms().get(creationTargetRoomNo);
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
		if (creationType == 2) this.thread.getServer().getGame().getWorld().getRooms().add(new Room(creationName, blueprint));
		if (creationType == 3) this.getLocation().objectEntered(new Door(creationName, blueprint, creationTarget));
	}
	
	public void edit(String blueprint)
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
			this.getLocation().setName(editName);
			this.getLocation().setDescription(blueprint);
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
			
			if (this.getLocation().getContentsByName(targetName) != null){
				this.getLocation().getContentsByName(targetName).setDescription(blueprint);
				this.getLocation().getContentsByName(targetName).setName(editName);
			} else
				thread.sendMessage("Cannot find " +targetName);
			return;
			}		
	}
	
	public void delete(String blueprint)
	{
		if (blueprint.equals("room")){
			if (this.getLocation() != this.thread.getServer().getGame().getWorld().getRooms().get(0)){
				Room roomToDelete = this.getLocation();
				this.getLocation().ejectContents(this.thread.getServer().getGame().getWorld().getRooms().get(0));
				// This could potentially cause a problem if someone manages to enter the room just after everything has
				// been ejected. They might then be trapped?
				this.thread.getServer().getGame().getWorld().getRooms().remove(roomToDelete);
				return;
			}
		}		
		else {
			this.getLocation().objectExited(this.getLocation().getContentsByName(blueprint));
			return;
			}		
	}
	
	public void eject(String text)
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
		if (target >= this.thread.getServer().getGame().getWorld().getRooms().size()){
			thread.sendMessage("That is not a valid room");
			return;
		}
		
		if (victimName.equals("all"))
			this.getLocation().ejectContents(this.thread.getServer().getGame().getWorld().getRooms().get(target));
		else 
			if (this.getLocation().getContentsByName(victimName) != null)
				this.getLocation().getContentsByName(victimName).moveTo(this.thread.getServer().getGame().getWorld().getRooms().get(target));
			else thread.sendMessage("Cannot find " + victimName);
	}
	
	public void userLookUp()
	{
		thread.sendMessage("IP Address       Username");
		for (SocketThread user: this.thread.getServer().getThreads()){
			thread.sendMessage(user.getIP() + "  " + user.getCharacter().getName());
		}
		
	}
	
	public void moveToByID(int roomNo)
	{
		roomNo = Math.abs(roomNo);
		if (roomNo < this.thread.getServer().getGame().getWorld().getRooms().size())
			this.moveTo(this.thread.getServer().getGame().getWorld().getRooms().get(roomNo));
		else
			this.receiveMessage("That is not a valid room");
	}

	public void moveToByName(String name)
	{
		for (Room place: this.thread.getServer().getGame().getWorld().getRooms())
			if (place.getName().equals(name)){
				this.moveTo(place);
				return;
			}
		this.receiveMessage("That is not a valid room");
	}

}
