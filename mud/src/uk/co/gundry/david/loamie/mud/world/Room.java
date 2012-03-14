/*
Loamie - A MUD Engine
Copyright (C) 2012  David Gundry, Adam Gundry

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package uk.co.gundry.david.loamie.mud.world;

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
