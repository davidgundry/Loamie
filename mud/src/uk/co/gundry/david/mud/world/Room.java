package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room in the game world (that is, a location). Rooms can contain any world objects,
 * including other rooms (?).
 * 
 * @author Adam Gundry extended by David Gundry
 */
public class Room implements Serializable, WorldObject
{
	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	
	private List<WorldObject> contents = new ArrayList<WorldObject>();
	
	/**
	 * Creates a room without setting any of the variables. The program will probably break if you
	 * try and add this room to the game world.
	 */
	public Room()
	{
		
	}
	
	/**
	 * Creates a new room with the given name and description.
	 * 
	 * @param name
	 * @param description
	 */
	public Room(String name, String description)
	{
		this.name = name;
		this.description = description;
	}
	
	/**
	 * When a message is received, it is passed to the room's contents.
	 */
	public void receiveMessage(String text)
	{
		for (WorldObject object: contents)
			object.receiveMessageFromPlayer(text);
	}
	
	/**
	 * Called when an object enters this room. Adds it to the contents list.
	 * 
	 * @param object
	 */
	public void objectEntered(WorldObject object)
	{
		contents.add(object);		
		object.receiveMessage("\nYou have entered " + this.name);
		this.receiveMessage(String.format("%s has entered.", object.getName()));
	}
	
	/**
	 * Calls when an object leaves this room. Removes it from the contents list.
	 * @param object
	 */
	public void objectExited(WorldObject object)
	{
		contents.remove(object);
		this.receiveMessage(String.format("%s has left.", object.getName()));
	}
	
	/**
	 * Returns the name of this room.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the description of this room.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Sets the name of this room to a string. There should be no spaces,
	 * but underscores (_) can be used in place of spaces.
	 */
	public void setName(String newName){
		name = newName;
	}
	
	/**
	 * Sets the description of the room to a string.
	 */
	public void setDescription(String newDescription){
		description = newDescription;
	}
	
	public int getType(){
		return 2;
	}
	
	/**
	 * Gets the room to describe its contents.
	 */
	public String describeContents()
	{
		String contentsText = "";
		if (contents.size() > 0)
		{
			contentsText += "\nContents: ";
			for (WorldObject object: contents)
				contentsText += object.getName() + ", ";
		}
		return contentsText;
	}
	
	public List<Door> getDoors()
	{
		List<Door> doors = new ArrayList<Door>();
		
		for (int i=0;i<contents.size();i++)
			if (contents.get(i).getType() == 3) doors.add((Door) contents.get(i));
		
		return doors;
	}
	
	/**
	 * Gets the room to describe its doors.
	 */
	public String describeDoors()
	{
		String text = "";
		List<Door> doorList = getDoors();
		for (int i=0;i<doorList.size();i++){
			text += doorList.get(i).getName();
			text += "\n" + doorList.get(i).getDescription();
			text += "\n" + doorList.get(i).describeContents() + "\n \n";
		}
		text = text.substring(0,text.length()-2);
		return text;
	}
	
	public int processCommand(String command, GameCharacter actor)
	{
		if (command.toLowerCase().startsWith("enter "))
		{
			actor.moveTo(this);
			return 1;
		} else
			return 0;
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
	
	public boolean moveTo(WorldObject target)
	{
		return false;
	}
	
	/**
	 * At the moment, this doesn't remove the things from the current room, but does move them into Limbo
	 * @param target
	 */
	public void ejectContents(Room target)
	{ 
		for (WorldObject object: contents)
	    	target.objectEntered(object);
	}

	public List<String> getSynonyms() {
		// TODO Auto-generated method stub
		return null;
	}

	public Room getLocation() {
		return this;
	}

	public void heal(int value) {
		for (WorldObject object: contents)
			object.heal(value);
	}

	/**
	 * This is intentionally not implemented by Room.
	 */
	public int interpretCommand(String text, GameCharacter actor) {return 0;}

	/**
	 * This is intentionally not implemented by Room.
	 */
	public int processCommand(String command) {return 0;}

	/**
	 * This is intentionally not implemented by Room.
	 */
	public void listenToCommand(String command, PlayerCharacter actor) {}

	public void receiveMessageFromPlayer(String text) {
		for (WorldObject object: contents)
			object.receiveMessageFromPlayer(text);
	}
	
	public void saveStateToXML(PrintStream ps)
	{
		ps.println("	<room>");
		ps.println("		<name>"+this.name+"</name>");
		ps.println("		<description>"+this.description+"</description>");
		for (Door door: this.getDoors())
		{
			door.saveStateToXML(ps);
		}
		for (WorldObject cont: this.contents)
		{
			if ((cont.getType() == 0) || (cont.getType() == 1 )){
				cont.saveStateToXML(ps);
			}
		}
		ps.println("	</room>");
	}
	
}
