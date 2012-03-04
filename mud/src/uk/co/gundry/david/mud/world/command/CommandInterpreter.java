package uk.co.gundry.david.mud.world.command;

import uk.co.gundry.david.mud.net.SocketThread;

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
