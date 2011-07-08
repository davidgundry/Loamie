package uk.co.thunderbadger.mud;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.thunderbadger.mud.world.GameCharacter;
import uk.co.thunderbadger.mud.world.WorldObject;

public class ScriptInterpreter {

	/* Commands that are recognised:
	 * 
	 * heal [value] [room/holder/actor]
	 * message [string] (untagged)
	 * announce [string] (untagged)
	 * say [message] (same as command say)
	 * shout [message] (same as command shout)
	 * pick up (etc.) (moves caller to actor)
	 * drop (etc.) (moves caller to actor's location)
	 * delete self
	 * 
	 */
	
	public int interpret(String command, WorldObject caller, GameCharacter actor)
	{
		if (command.toLowerCase().startsWith("heal ")){
			command = command.substring(5);
			commandHeal(command, caller, actor);
			return 1;
		} else if (command.toLowerCase().startsWith("message ")){
			command = command.substring(8);
			actor.receiveMessage(command);
			return 1;
		} else if (command.toLowerCase().startsWith("announce ")){
			command = command.substring(9);
			actor.getLocation().receiveMessage(command);
			return 1;
		} else if (command.toLowerCase().startsWith("say ")){
			command = command.substring(4);
			actor.getLocation().receiveMessage(caller.getName() + " says, \"" + command + "\"");
			return 1;
		}else if (command.toLowerCase().startsWith("shout ")){
			command = command.substring(6);
			actor.getLocation().receiveMessage(caller.getName() + " shouts, \"" + command + "\"");
			return 1;
		} else if (command.toLowerCase().equals("pick up") || command.toLowerCase().equals("get") || command.toLowerCase().equals("grab") || command.toLowerCase().equals("take")){
			if (caller.getLocation() != actor)
				caller.moveTo(actor);
			return 1;
		} else if (command.toLowerCase().equals("drop") || command.toLowerCase().equals("put down") || command.toLowerCase().equals("lose")){
			if (caller.getLocation() == actor)
				caller.moveTo(actor.getLocation());
			return 1;
		} else if (command.toLowerCase().equals("delete self")){
			caller.getLocation().objectExited(caller);
			return 1;
		} else
			return 0;
	}
	
	void commandHeal(String command, WorldObject caller, GameCharacter actor){
		StringTokenizer st = new StringTokenizer(command);
		String valueString;
		try {
			valueString = st.nextToken();
		} catch (NoSuchElementException ex) {
			caller.receiveMessage("Wrong syntax!");
			return;	
		}
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException ex) {
			caller.receiveMessage("That is not a valid amount");
			return;
		}
		String targetName = "";
		try {
			targetName = st.nextToken();
		} catch (NoSuchElementException ex) {
			caller.getLocation().heal(value);
		}
		
		if (targetName.equals("room"))
			caller.getLocation().getLocation().heal(value);
		else if (targetName.equals("actor"))
			actor.heal(value);
		else if (targetName.equals("holder"))
			if (caller.getLocation().getType() == 0)
				caller.getLocation().heal(value);	

		else
			caller.receiveMessage("What do you want me to heal?");

	}
	
}
