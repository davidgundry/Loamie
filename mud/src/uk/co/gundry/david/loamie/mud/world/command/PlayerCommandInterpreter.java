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

package uk.co.gundry.david.loamie.mud.world.command;

import uk.co.gundry.david.loamie.mud.net.SocketThread;

public class PlayerCommandInterpreter implements CommandInterpreter {

	
	public boolean interpret(String command, SocketThread connection)
	{
		if (command.toLowerCase().startsWith("say "))
		{
			connection.getCharacter().say(command.substring(4));
			return true;
		}
		
		if (command.toLowerCase().startsWith("shout "))
		{
			connection.getCharacter().shout(command.substring(6));
			return true;
		}

		if (command.startsWith("*"))
		{
			connection.getCharacter().rpAction(command.substring(1));
			return true;
		}

		if (command.startsWith("/"))
		{
			connection.getCharacter().ownerlessRpAction(command.substring(1));
			return true;
		}
		
		if (command.toLowerCase().equals("look") || command.toLowerCase().equals("look around"))
		{
			connection.getCharacter().look();
			return true;
		}
		
		if (command.toLowerCase().equals("door") || command.toLowerCase().equals("doors") || command.toLowerCase().equals("look doors") || command.toLowerCase().equals("look door") || command.toLowerCase().equals("look at door") || command.toLowerCase().equals("look at doors") || command.toLowerCase().equals("look at the door") || command.toLowerCase().equals("look at the doors"))
		{
			connection.getCharacter().receiveMessage(connection.getCharacter().getLocation().describeDoors());
			return true;
		}
		
		if (command.toLowerCase().equals("use door") || command.toLowerCase().equals("use the door"))
		{
			if (connection.getCharacter().getLocation().getDoors().size() == 1){
				connection.getCharacter().getLocation().getDoors().get(0).interpretCommand("use", connection.getCharacter());
			}
			else
				connection.getCharacter().receiveMessage("Which door you mean?");
			return true;
		}
		
		if (command.toLowerCase().startsWith("look at "))
		{
			connection.getCharacter().objectLook(command.substring(8).toLowerCase());
			return true;
		}
		
		if (command.toLowerCase().startsWith("look "))
		{
			connection.getCharacter().objectLook(command.substring(5).toLowerCase());
			return true;
		}

		if (command.toLowerCase().equals("me"))
		{
			connection.getCharacter().receiveMessage(connection.getCharacter().getName() + "\n" + connection.getCharacter().getDescription());
			return true;
		}
		
		if (command.toLowerCase().equals("stat") || command.toLowerCase().equals("stats"))
		{
			connection.getCharacter().receiveMessage(connection.getCharacter().characterStats());
			return true;
		}
		
		if (command.toLowerCase().equals("inven"))
		{
			connection.getCharacter().receiveMessage(connection.getCharacter().describeContents());

			return true;
		}
		
		if (command.toLowerCase().equals("sheet") || command.toLowerCase().equals("character sheet") || command.toLowerCase().equals("character"))
		{
			connection.getCharacter().receiveMessage(connection.getCharacter().characterSheet());
			return true;
		}
		
		return false;
	}
	
}
