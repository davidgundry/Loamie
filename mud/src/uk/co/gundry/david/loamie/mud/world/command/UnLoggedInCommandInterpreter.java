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

import uk.co.gundry.david.loamie.mud.Game;
import uk.co.gundry.david.loamie.mud.net.SocketThread;
import uk.co.gundry.david.loamie.mud.world.GameCharacter;
import uk.co.gundry.david.loamie.mud.world.PlayerCharacter;
import uk.co.gundry.david.loamie.mud.world.Room;

public class UnLoggedInCommandInterpreter implements CommandInterpreter {

	/**
	 * Interprets commands for users who are connected to the server, but do not have a character associated with them,
	 * thus they are not logged in.
	 * 
	 * @param command
	 * @param connection
	 */
	public boolean interpret(String command, SocketThread connection)
	{
		if (command.startsWith("login "))
		{
			if (Game.getWorld().getRooms().size() > 0)
			{
				for (Room room: Game.getWorld().getRooms())
				{
					if (room.getContentsByName(command.substring(6)) != null){
						if (room.getContentsByName(command.substring(6)).getType() == GameCharacter.getStaticType());
						{
							connection.setCharacter(new PlayerCharacter((GameCharacter)room.getContentsByName(command.substring(6))));
							connection.sendMessage("A character has been found by that name.");
							if (connection.getCharacter().getLocation() == Game.getWorld().getRooms().get(0)){
								connection.getCharacter().moveTo(connection.getCharacter().getLastRoom());
								connection.getCharacter().playerConnected(connection);
								return true;
							} else{
								connection.logMessage(connection.getCharacter().getName() + " arrived in an unexpected location. Did the server not shut down properly?");
								connection.getCharacter().playerConnected(connection);
								return true;
							}
						}
					}	
				}
				
				connection.setCharacter(Game.createPlayerCharacter(command.substring(6), "As yet completely undescribed and unremarkable."));
				connection.sendMessage("A new character has been created.");
				connection.getCharacter().playerConnected(connection);
				return true;
			}
			connection.sendMessage("The server has not got a world loaded.");
			Game.logMessage("Player tried to connect to empty world!");
			return true;
		}
		
		/*if (command.toLowerCase().equals("restore"))
		{
			connection.sendMessage(connection.getServerThread().restoreWorldState());
			return true;
		}*/
		
		if (command.toLowerCase().equals("restore"))
		{
			connection.sendMessage(connection.getServerThread().restoreWorldStateFromXML());
			return true;
		}
		
		if (command.toLowerCase().equals("shutdown"))
		{
			Runtime.getRuntime().exit(0);
			return true;
		}
		
		return false;
	}
	
}
