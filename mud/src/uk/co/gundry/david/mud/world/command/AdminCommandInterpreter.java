package uk.co.gundry.david.mud.world.command;

import uk.co.gundry.david.mud.net.SocketThread;

public class AdminCommandInterpreter implements CommandInterpreter {

	/**
	 * A Command Interpreter specifically for administrator commands, being those that modify the base world
	 * such as creating and editing rooms, and those that save the world state, and those that allow control
	 * over players connected to the server.
	 * 
	 * @param command
	 * @param connection
	 */
	public boolean interpret(String command, SocketThread connection)
	{
		if (command.toLowerCase().startsWith("goto "))
		{
			int roomNo = 0;
			try {
				int value = Integer.parseInt(command.substring(5));
				roomNo = value;
			} catch (NumberFormatException value) {
				connection.getCharacter().receiveMessage("That is not a valid room");
				return true;
			}
			connection.getCharacter().moveToByID(roomNo);
			return true;
		}
		
		if (command.toLowerCase().startsWith("create "))
		{
			connection.getCharacter().create(command.substring(7));
			return true;
		}
		
		if (command.toLowerCase().startsWith("edit "))
		{
			connection.getCharacter().edit(command.substring(5));
			return true;
		}
		
		if (command.toLowerCase().startsWith("delete "))
		{
			connection.getCharacter().delete(command.substring(7));
			return true;
		}
		
		if (command.toLowerCase().startsWith("eject "))
		{
			connection.getCharacter().eject(command.substring(6));
			return true;
		}
		
		if (command.toLowerCase().equals("users"))
		{
			connection.getCharacter().userLookUp();
			return true;
		}
		
		/*if (command.toLowerCase().equals("save"))
		{
			connection.getServerThread().saveWorldState();
			return true;
		}*/
		
		if (command.toLowerCase().equals("save"))
		{
			connection.getServerThread().saveWorldStateToXML();
			return true;
		}
		
		return false;
	}
	
}
