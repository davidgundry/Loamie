package uk.co.thunderbadger.mud.world.npc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dialogue implements Serializable {

	private String line;
	private int trigger;
	private List<Dialogue> dialogues = new ArrayList<Dialogue>();
	private String action;
	
	public Dialogue(int trigger, String line)
	{
		this.line = line;
		this.trigger = trigger;
		this.dialogues = null;
	}
	
	public Dialogue(int trigger, String line, List<Dialogue> dialogues)
	{
		this.line = line;
		this.trigger = trigger;
		this.dialogues = dialogues;
	}
	
	public Dialogue(int trigger, String line, String action)
	{
		this.line = line;
		this.trigger = trigger;
		this.dialogues = null;
		this.action = action;
	}
	
	public Dialogue(int trigger, String line, List<Dialogue> dialogues, String action)
	{
		this.line = line;
		this.trigger = trigger;
		this.dialogues = dialogues;
		this.action = action;
	}
	
	public int getTrigger()
	{
		return trigger;
	}
	
	public String getline()
	{
		return line;
	}
	
	public List<Dialogue> getDialogues()
	{
		return dialogues;
	}
	
	public String getAction()
	{
		return action;
	}
	
}
