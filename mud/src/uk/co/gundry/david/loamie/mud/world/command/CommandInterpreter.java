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

public interface CommandInterpreter {

	/**
	 * Is called with a command received from a player. A Command Interpreter checks for basic and system
	 * commands. It does not attempt to understand the various commands that items could take. It needs to
	 * be passed the SocketThread so it can execute the commands. It returns true if it understands the
	 * command, otherwise it returns false.
	 * 
	 * @param command
	 * @param connection
	 */
	public boolean interpret(String command, SocketThread connection);
}
