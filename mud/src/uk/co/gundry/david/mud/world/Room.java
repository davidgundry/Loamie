package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a room in the game world (that is, a location). Rooms can contain any world objects,
 * including other rooms (?).
 * 
 * @author Adam Gundry extended by David Gundry
 */
public class Room extends WorldObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int TYPE = 2;
	
	/**
	 * Creates a room without setting any of the variables. The program will probably break if you
	 * try and add this room to the game world without defining its variables first.
	 */
	public Room(){}
	
	/**
	 * Creates a new room with the given name and description.
	 * 
	 * @param name
	 * @param description
	 */
	public Room(String name, String description)
	{
		super(name, description);
	}
	
	/**
	 * When a message is received, it is passed to the room's contents.
	 */
	public void receiveMessage(String text)
	{
		for (WorldObject object: getContents())
			object.receiveMessage(text);
	}
	
	/**
	 * When a message is received from a player, it is passed to the room's contents.
	 */
	public void receiveMessageFromPlayer(String text) {
		for (WorldObject object: getContents())
			object.receiveMessageFromPlayer(text);
	}

	public int getType(){
		return TYPE;
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

	public void heal(int value) {
		for (WorldObject object: getContents())
			object.heal(value);
	}

	public void saveStateToXML(PrintStream ps)
	{
		ps.println("	<room>");
		ps.println("		<name>"+this.getName()+"</name>");
		ps.println("		<description>"+this.getDescription()+"</description>");
		for (Door door: this.getDoors())
		{
			door.saveStateToXML(ps);
		}
		for (WorldObject cont: this.getContents())
		{
			if ((cont.getType() == 0) || (cont.getType() == 1 )){
				cont.saveStateToXML(ps);
			}
		}
		ps.println("	</room>");
	}
	
}
