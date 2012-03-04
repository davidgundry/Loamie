package uk.co.gundry.david.mud;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.gundry.david.mud.world.GameCharacter;
import uk.co.gundry.david.mud.world.WorldObject;

/**
 * The script interpreter is the class that is called whenever a script is run,
 * for example when an item with its own script is used. To run a script, create
 * a script interpreter and then call interpret() on it, passing it the required
 * parameters, including the script to run.
 * 
 * <h2>Commands that are recognised:</h2>
 * <ul><li>heal [value] [room/holder/actor]</li>
 * <li>message [string] (untagged)</li>
 * <li>announce [string] (untagged)</li>
 * <li>say [message] (same as command say)</li>
 * <li>shout [message] (same as command shout)</li>
 * <li>pick up (etc.) (moves caller to actor)</li>
 * <li>drop (etc.) (moves caller to actor's location)</li>
 * <li>delete self</li></ul>
 *
 * @author David Gundry
 *
 */
public class ScriptInterpreter {

	/**
	 * Called with the command string, this figures out what the command means and does it.
	 * 
	 * @parm command
	 * @param caller
	 * @param actor
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
	
	/**
	 * This interprets all commands starting with 'heal', and does them.
	 * 
	 * @param command
	 * @param caller
	 * @param actor
	 */
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
