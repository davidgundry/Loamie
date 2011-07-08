package uk.co.thunderbadger.mud.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import uk.co.thunderbadger.mud.ScriptInterpreter;
import uk.co.thunderbadger.mud.world.npc.Dialogue;

public class NPCharacter extends GameCharacter  {
	
	private String greeting = "";
	private String farewell = "";
	private String dismissal = "They have nothing to say to you.";
	private List<Dialogue> dialogues = new ArrayList<Dialogue>();
	private Map<GameCharacter,List<Dialogue>> playerDialogues = new HashMap<GameCharacter,List<Dialogue>>();

	public NPCharacter(String name, String description) {
		super(name, description);
	}
	
	public NPCharacter(String name, String description, int hitPoints) {
		super(name, description, hitPoints);
	}
	
	public NPCharacter(String name, String description, String dismissal) {
		super(name, description);
		this.dismissal = dismissal;
	}
	
	public NPCharacter(String name, String description, int hitPoints,  String dismissal) {
		super(name, description, hitPoints);
		this.dismissal = dismissal;
	}
	
	public NPCharacter(String name, String description, String greeting, String farewell, List<Dialogue> dialogues) {
		super(name, description);
		this.dialogues = dialogues;
		this.greeting = greeting;
		this.farewell = farewell;
	}
	
	public NPCharacter(String name, String description, int hitPoints, String greeting, String farewell, List<Dialogue> dialogues) {
		super(name, description, hitPoints);
		this.dialogues = dialogues;
		this.greeting = greeting;
		this.farewell = farewell;
	}

	public int interpretCommand(String command, GameCharacter actor)
	{
		if (command.toLowerCase().equals("talk to") || command.toLowerCase().equals("chat to") || command.toLowerCase().equals("speak to") || command.toLowerCase().equals("talk with") || command.toLowerCase().equals("speak with") || command.toLowerCase().equals("chat with"))
		{
			if (dialogues != null){
				actor.getLocation().receiveMessage(actor.getName() + " is talking to " + this.getName());
				actor.setListener(this);
				actor.receiveMessage(greeting);
				playerDialogues.put(actor, dialogues);
			} else
				actor.receiveMessage(dismissal);
			return 1;
		} else if (command.toLowerCase().equals("attack"))
		{
			
			return 1;
		} else
		return 0;
	}
	
	public void listenToCommand(String command, PlayerCharacter actor) {
		for (Dialogue conversation: getCurrentDialogues(actor)) 
			if (command.toLowerCase().equals(Integer.toString(conversation.getTrigger())))
			{
				actor.receiveMessage(conversation.getline());
				playerDialogues.remove(actor);
				if (conversation.getAction() != null)
					processCommand(conversation.getAction(), actor);
				if (conversation.getDialogues() != null){
					playerDialogues.put(actor, conversation.getDialogues());
					return;
				} else {
					stopConversation(actor);
					return;
				}
			}
		if (command.toLowerCase().equals("stop")){
			stopConversation(actor);
			return;
		}
		
		// If we got to here the command wasn't understood.
		actor.receiveMessage("Pardon?");		
	}
	
	private void stopConversation(GameCharacter actor)
	{			
		actor.receiveMessage(farewell);
		playerDialogues.remove(actor);
		actor.setListener(null);
	}
	
	private List<Dialogue> getCurrentDialogues(GameCharacter actor)
	{
		return playerDialogues.get(actor);
	}
	
	public int processCommand(String command, GameCharacter actor) {
		ScriptInterpreter si = new ScriptInterpreter();
		return si.interpret(command, this, actor);
	}
	
	void commandHeal(String command, GameCharacter actor){
		StringTokenizer st = new StringTokenizer(command);
		String valueString;
		try {
			valueString = st.nextToken();
		} catch (NoSuchElementException ex) {
			this.receiveMessage("Wrong syntax!");
			return;	
		}
		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException ex) {
			this.receiveMessage("That is not a valid amount");
			return;
		}
		String targetName = "";
		try {
			targetName = st.nextToken();
		} catch (NoSuchElementException ex) {
			this.location.heal(value);
		}
		
		if (targetName.equals("room"))
			this.getLocation().heal(value);
		else if (targetName.equals("actor"))
			actor.heal(value);
		else
			this.receiveMessage("What do you want me to heal?");

	}

	
}
