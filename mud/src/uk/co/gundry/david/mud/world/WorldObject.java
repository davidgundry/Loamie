package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.co.gundry.david.mud.Game;

/**
 * Represents an abstract "thing" in the world tree.
 * 
 * @author Adam Gundry
 */
public class WorldObject implements Serializable
{
	private static final int TYPE = -1;
	protected String name;
	protected String description;
	protected WorldObject location;
	protected List<String> synonyms = new ArrayList<String>();
	protected List<WorldObject> contents = new ArrayList<WorldObject>();
	
	
	private static final long serialVersionUID = 1L;

	public WorldObject() {
	}
	public WorldObject(String name, String description) {
		this.name = name;
		this.description = description;
	}
	public WorldObject(String name, String description,List<String> synonyms) {
		this.name = name;
		this.description = description;
		this.synonyms = synonyms;
	}
	public WorldObject(String name, String description,List<String> synonyms,WorldObject location) {
		this.name = name;
		this.description = description;
		this.synonyms = synonyms;
		this.location = location;
	}

	/**
	 * Called when something sends a message to the object. 
	 * Implementations should deal with this appropriately.
	 * 
	 * @param text  Message received
	 */
	public void receiveMessage(String text) {
	}	
	
	/**
	 * Called when a player sends a message to the object. 
	 * Implementations should deal with this appropriately.
	 * 
	 * @param text  Message received
	 */
	public void receiveMessageFromPlayer(String text) {
	}
	
	/**
	 * Returns the object's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the object's description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the list of the objects synonyms
	 */
	public List<String> getSynonyms() {
		return synonyms;
	}
	
	/**
	 * Changes the object's name to the one supplied.
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	
	/**
	 * Changes the object's description to the one supplied.
	 */
	public void setDescription(String newDescription) {
		this.description = newDescription;
	}
	
	/**
	 * Returns a string listing the contents of the object.
	 */
	public String describeContents() {
		String contentsText = "";
		if (contents.size() > 0)
		{
			contentsText += "\nContents: ";
			for (WorldObject object: contents)
				contentsText += object.getName() + ", ";
		}
		return contentsText;
	}
	
	/**
	 * Writes the object to the PrintStream in XML format.
	 */
	public void saveStateToXML(PrintStream ps) {
	}
	
	/**
	 * Returns WorldObject type. 0 for GameCharacters, 1 for Items, 2 for Rooms and 3 for Doors.
	 */
	public int getType() {
		return TYPE;
	}
	
	/**
	 * Moves object to specified location. Returns true on success, false if for any reason the
	 * object can't be moved there. (Eg. the location doesn'e exist)
	 * 
	 * @param location - the target to move to
	 */
	public boolean moveTo(WorldObject location) {
		return false;
	}
	
	/**
	 * Called when an object that the WorldObject is containing leaves.
	 * Must remove the object from the list of contents.
	 * 
	 * @param object - the object that has left
	 */
	public void objectExited(WorldObject object) {
		contents.remove(object);
		this.receiveMessage(String.format("%s has left.", object.getName()));
	}
	
	/**
	 * Called when an object enters the contents of the WorldObject.
	 * Must add the object to the list of contents
	 * 
	 * @param object - the object that entered
	 */
	public void objectEntered(WorldObject object) {
		contents.add(object);		
		object.receiveMessage("\nYou have entered " + this.name);
		this.receiveMessage(String.format("%s has entered.", object.getName()));
	}
	
	/**
	 * If it is a GameCharacter, it heals value of hitpoints.
	 * Rooms forward the heal command to all of its contents.
	 * 
	 * @param value
	 */
	public void heal(int value) {
	}
	
	/**
	 * Returns the WorldObject that an object thinks it is contained within.
	 * In the case of Rooms, they return themselves.
	 */
	public WorldObject getLocation() {
		return null;
	}

	/**
	 * Called when a verb is performed upon this object.
	 * It checks if this object knows what the verb is, and if so, it does something.
	 *
	 * @param command
	 * @param actor
	 */
	public int interpretCommand(String text, GameCharacter actor) {
		return 0;
	}
	
	/**
	 * Calls when this object performs an action itself. Eg. A socket sends a command,
	 *  or an Item sends a command (probably after being commanded itself)
	 *
	 * @param command
	 * @param actor
	 */
	public int processCommand(String command, GameCharacter actor) {
		return 0;
	}
	
	/**
	 * Called when this object is a listener for a PlayerCharacter.
	 *  This is used to allow things to break out of the usual interpreter for a while.
	 *
	 * @param command
	 * @param actor
	 */
	public void listenToCommand(String command, PlayerCharacter actor) {
	}

	public void setLocation(WorldObject location) {
		this.location = location;
	}
	
	/**
	 * Moves this WorldObject to the room with the specified ID.
	 * 
	 * @param roomNo
	 */
	public void moveToByID(int roomNo)
	{
		roomNo = Math.abs(roomNo);
		if (roomNo < Game.getWorld().getRooms().size())
			this.moveTo(Game.getWorld().getRooms().get(roomNo));
		else
			this.receiveMessage("That is not a valid room");
	}

	/**
	 * Moves this WorldObject to the room with the specified name.
	 * 
	 * @param name
	 */
	public void moveToByName(String name)
	{
		for (Room place: Game.getWorld().getRooms())
			if (place.getName().equals(name)){
				this.moveTo(place);
				return;
			}
		this.receiveMessage("That is not a valid room");
	}
	public String describeDoors() {
		return null;
	}
	
	/**
	 * Return an item in this object's contents by its name.
	 * @param name - the name of the WorldObject to return
	 * @return - the WorldObject, or null if it can't be found
	 */
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
	
	/**
	 * Return a list of all doors in this object's contents
	 */
	public List<Door> getDoors()
	{
		List<Door> doors = new ArrayList<Door>();
		
		for (int i=0;i<contents.size();i++)
			if (contents.get(i).getType() == 3) doors.add((Door) contents.get(i));
		
		return doors;
	}
	
	/**
	 * Return a list of all GameCharacters in this object's contents
	 */
	public List<GameCharacter> getGameCharacters()
	{
		List<GameCharacter> gcs = new ArrayList<GameCharacter>();
		
		for (int i=0;i<contents.size();i++)
			if (contents.get(i).getType() == GameCharacter.getStaticType()) gcs.add((GameCharacter) contents.get(i));
		
		return gcs;
	}
	
	/**
	 * At the moment, this doesn't remove the things from the current room, but does move them into the target
	 * @param target
	 */
	public void ejectContents(WorldObject target)
	{ 
		for (WorldObject object: contents)
	    	target.objectEntered(object);
	}
}

