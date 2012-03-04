package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Represents a character in the game. Subclasses may represent player characters or
 * those with some kind of AI control.
 * 
 * @author Adam Gundry
 */
public class GameCharacter implements Serializable, WorldObject
{

	private static final long serialVersionUID = 1L;
	// name cannot be the same as a room (Item drop code)
	protected String name;	
	protected String description;	
	
	private List<String> synonyms;
	
	protected Room location;
	protected Room lastRoom;
	protected int hitPoints;
	protected int xp;
	
	WorldObject listener;

	private List<WorldObject> contents = new ArrayList<WorldObject>();
	
	/**
	 * Create a new character with the given name and description
	 * 
	 * @param name
	 * @param description
	 */
	public GameCharacter(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.hitPoints = 10;
		this.xp = 0;
		this.listener = null;
	}
	
	public GameCharacter()
	{
		
	}
	
	public GameCharacter(String name, String description, int hitPoints)
	{
		this.name = name;
		this.description = description;
		this.hitPoints = hitPoints;
		this.xp = 0;
		this.listener = null;
	}
	
	public GameCharacter(String name, String description, List<String> synonyms)
	{
		this.name = name;
		this.description = description;
		this.synonyms = synonyms;
		this.hitPoints = 10;
		this.xp = 0;
		this.listener = null;
	}
	
	/**
	 * We do nothing with messages received. Subclasses should override this.
	 */
	public void receiveMessage(String text)
	{
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setDescription(String newDescription){
		description = newDescription;
	}
	
	public Room getLocation()
	{
		return location;
	}
	
	public Room getLastRoom()
	{
		return lastRoom;
	}
	
	public void setLastRoom(Room lastRoom)
	{
		this.lastRoom = lastRoom;
	}
	
	public int getHitPoints()
	{
		return hitPoints;
	}
	
	public int getXp()
	{
		return xp;
	}
	
	public int getType(){
		return 0;
	}
	
	/**
	 * Moves this character to the given room, notifying the current location and destination
	 * as appropriate.
	 * 
	 * @param location
	 */
	public boolean moveTo(WorldObject location)
	{
		if (location != null)
		{
			if (this.location != null)
				this.location.objectExited(this);
			this.lastRoom = this.location;
			this.location = (Room) location;
			location.objectEntered(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Says the given text out loud. This emits an appropriate message to the surrounding room. 
	 * 
	 * @param text  Words to speak
	 */
	public void say(String text)
	{
		location.receiveMessage(String.format("%s says, \"%s\"", name, text));
	}
	public void shout(String text)
	{
		location.receiveMessage(String.format("%s shouts, \"%s\"", name, text));
	}
	public void rpAction(String text)
	{
		location.receiveMessage(String.format("%s %s", name, text.trim()));
	}
	public void ownerlessRpAction(String text)
	{
		location.receiveMessage(text);
	}
	public void look()
	{
		receiveMessage(location.getName() + "\n" + location.getDescription() + "\n" + location.describeContents().replace(this.name+",", ""));
	}
	public void objectLook(String text)
	{
		if (text.equals("me") || text.equals("self") || text.equals("myself"))
		{
			receiveMessage(this.getDescription() + "\n" + this.describeContents());
		} else {
			WorldObject object = location.getContentsByName(text);
			if (object != null)
				receiveMessage(object.getDescription() + "\n" + object.describeContents());
			else {
				WorldObject object2 = this.getContentsByName(text);
				if (object2 != null)
					receiveMessage("Inventory:" + object2.getDescription() + "\n" + object2.describeContents());
				else
					receiveMessage("look at what?");
			}
				
			 
		}
	}
		
	public String getDescription()
	{
		return this.description;
	}
	
	public String describeContents()
	{
		String contentsText = "";
		if (contents.size() > 0)
		{
			contentsText += "\nInventory: ";
			for (WorldObject object: contents)
				contentsText += object.getName() + ", ";
		}
		else
			contentsText = "\nInventory: (empty)";
		
		return contentsText;
	}
	
	public String characterSheet()
	{
		return this.getName() + " (" + this.getLocation().getName() + ")\n" + this.getDescription() + "\n" + this.characterStats() + this.describeContents();
	}
	
	public String characterStats()
	{
		return "Hitpoints: " + this.hitPoints + "\nXp: " + this.xp;
	}

	public WorldObject getContentsByName(String name)
	{
	    for (WorldObject object: contents){
	    	if (object.getName().toLowerCase().equals(name.toLowerCase()))
	    		return object;
	    }
	    for (WorldObject object: contents){
	    	if (object.getSynonyms() != null)
		    	for (String text: object.getSynonyms()){
		    		if (text.toLowerCase().equals(name.toLowerCase()))
		    			return object;
	    	}
	    }
	    return null;
	}
	
	public int interpretCommand(String command, GameCharacter actor)
	{
		if (command.toLowerCase().startsWith("attack "))
		{
			this.hitPoints -= 1;
			actor.getLocation().receiveMessage(actor.getName() + " attacks " + this.getName() + ".");
			return 1;
		} else
			return 0;
	}
	
	/**
	 * Called when an object enters this character's inventory. Adds it to the contents list.
	 * 
	 * @param object
	 */
	public void objectEntered(WorldObject object)
	{
		contents.add(object);		
		this.receiveMessage(String.format("You have gained a %s.", object.getName()));
	}
	
	/**
	 * Calls when an object leaves this character's inventory. Removes it from the contents list.
	 * @param object
	 */
	public void objectExited(WorldObject object)
	{
		contents.remove(object);
		this.receiveMessage(String.format("You have lost a %s.", object.getName()));
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void heal(int value) {
		this.hitPoints = this.hitPoints + value;
		if (value > 0)
			this.receiveMessage("You have been healed " + value + " points.");
		else 
			this.receiveMessage("You have been harmed " + -value + " points.");
		
	}

	public int processCommand(String command, GameCharacter actor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void listenToCommand(String command, PlayerCharacter actor) {
		
	}
	
	public WorldObject getListener()
	{
		return listener;
	}
	
	public void setListener(WorldObject listener)
	{
		this.listener = listener;
	}

	public void receiveMessageFromPlayer(String text) {
		// TODO Auto-generated method stub
		
	}

	public void saveStateToXML(PrintStream ps) {
		// TODO Auto-generated method stub
		
	}
	
	void commandHeal(String command, GameCharacter actor){
		StringTokenizer st = new StringTokenizer(command);
		String valueString;
		try {
			valueString = st.nextToken();
		} catch (NoSuchElementException ex) {
			this.receiveMessage("Wrong syntax!");
			return;	
		}
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException ex) {
			this.receiveMessage("That is not a valid amount");
			return;
		}
		String targetName = "";
		try {
			targetName = st.nextToken();
		} catch (NoSuchElementException ex) {
			this.location.heal(value);
		}
		
		if (targetName.equals("room"))
			this.getLocation().heal(value);
		else if (targetName.equals("actor"))
			actor.heal(value);
		else
			this.receiveMessage("What do you want me to heal?");

	}
	    
}
