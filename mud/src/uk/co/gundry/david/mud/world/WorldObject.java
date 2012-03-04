package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

/**
 * Represents an abstract "thing" in the world tree.
 * 
 * @author Adam Gundry
 */
public interface WorldObject extends Serializable
{
	/**
	 * Called when something sends a message to the object. 
	 * Implementations should deal with this appropriately.
	 * 
	 * @param text  Message received
	 */
	public void receiveMessage(String text);	
	public void receiveMessageFromPlayer(String text);
	public String getName();
	public String getDescription();
	public List<String> getSynonyms();
	public void setName(String newName);
	public void setDescription(String newDescription);
	public String describeContents();
	public void saveStateToXML(PrintStream ps);
	
	/**
	 * Returns WorldObject type. 0 for GameCharacters, 1 for Items, 2 for Rooms and 3 for Doors.
	 */
	public int getType();
	
	/**
	 * Moves object to specified location. Returns true on success, false if for any reason the
	 * object can't be moved there. (Eg. the location doesn'e exist)
	 * @param location
	 */
	public boolean moveTo(WorldObject location);
	public void objectExited(WorldObject object);
	public void objectEntered(WorldObject object);

	
	/**
	 * If it is a GameCharacter, it heals value of hitpoints.
	 * Rooms forward the heal command to all of its contents.
	 * 
	 * @param value
	 */
	public void heal(int value);
	
	/**
	 * Returns the WorldObject that an object thinks it is contained within.
	 * In the case of Rooms, they return themselves.
	 */
	public WorldObject getLocation();

	/**
	 * Called when a verb is performed upon this object.
	 * It checks if this object knows what the verb is, and if so, it does something.
	 *
	 * @param command
	 * @param actor
	 */
	public int interpretCommand(String text, GameCharacter actor);
	
	/**
	 * Calls when this object performs an action itself. Eg. A socket sends a command,
	 *  or an Item sends a command (probably after being commanded itself)
	 *
	 * @param command
	 * @param actor
	 */
	public int processCommand(String command, GameCharacter actor);
	
	/**
	 * Called when this object is a listener for a PlayerCharacter.
	 *  This is used to allow things to break out of the usual interpreter for a while.
	 *
	 * @param command
	 * @param actor
	 */
	public void listenToCommand(String command, PlayerCharacter actor);
}

