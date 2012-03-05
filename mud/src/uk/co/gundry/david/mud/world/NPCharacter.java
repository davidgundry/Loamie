package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.gundry.david.mud.ScriptInterpreter;
import uk.co.gundry.david.mud.world.npc.Dialogue;

/**
 * A Non-Player Character is an extension of a GameCharacter that gives the character dialogues, and
 * in the future enable it to battle players.
 * 
 * @author David Gundry
 */
public class NPCharacter extends GameCharacter  {
	
	private static final long serialVersionUID = 1L;
	private String greeting = "";
	private String farewell = "";
	private String dismissal = "They have nothing to say to you.";
	
	private List<Dialogue> dialogues = new ArrayList<Dialogue>();
	private Map<GameCharacter,List<Dialogue>> playerDialogues = new HashMap<GameCharacter,List<Dialogue>>();

	/**
	 * Creates an empty Non-Player Character. Variables required to use this NPC have not been set, so don't try
	 * and add it to the world.
	 */
	public NPCharacter()
	{
		super();
	}
	
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
	
	public NPCharacter(String name, String description, List<String> synonyms, List<Dialogue> dialogues)
	{
		super(name, description, synonyms);
		this.dialogues = dialogues;
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
	
	public void saveStateToXML(PrintStream ps) {
		ps.println("<npc>");
		ps.println("	<name>" + name + "</name>");
		ps.println("	<description>" + description + "</description>");
		if (this.getSynonyms() != null)
			for (String syn: this.getSynonyms())
			{
				ps.println("			<synonym>"+syn+"</synonym>");
			}
		ps.println("	<hitpoints>" + hitPoints + "</hitpoints>");
		ps.println("	<xp>" + xp + "</xp>");
		if (lastRoom != null)
			ps.println("	<lastRoom>" + lastRoom.getName() + "</lastRoom>");
		ps.println("	<greeting> " + greeting + "</greeting>");
		ps.println("	<farewell> " + farewell + "</farewell>");
		ps.println("	<dismissal> " + dismissal + "</dismissal>");
		
		if (this.dialogues != null)
		{
		    Iterator<Dialogue> i = dialogues.iterator();
		    while(i.hasNext())
		    	i.next().saveStateToXML(ps);
		}
		ps.println("</npc>");
		
	}

	public static List<NPCharacter> loadStateFromXML(Element firstRoomElement)
	{
		NodeList npcList = firstRoomElement.getElementsByTagName("npc");
        int totalItems = npcList.getLength();
        System.out.println("Number of NPCs read: " + totalItems);
        
        List<NPCharacter> newNPCs = new ArrayList<NPCharacter>();
        
    	Element npcElement = (Element)npcList.item(0);
    	if (npcElement != null)
		{
    	NodeList childItemList = npcElement.getChildNodes();
    	
    	for(int j=0; j<childItemList.getLength(); j++){
    		if (npcList.item(j) != null)
    		{
        		Node firstNPCNode = npcList.item(j);
	                if(firstNPCNode.getNodeType() == Node.ELEMENT_NODE){
        		
            		Element firstNPCElement = (Element)firstNPCNode;
            		
            		 //-------
                    NodeList itemNameList = firstNPCElement.getElementsByTagName("name");
                    Element itemNameElement = (Element)itemNameList.item(0);

                    NodeList textItemNameList =  itemNameElement.getChildNodes();
                    System.out.println("	NPC : Name : " + ((Node)textItemNameList.item(0)).getNodeValue().trim());
                    String npcName = ((Node)textItemNameList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList itemDescList = firstNPCElement.getElementsByTagName("description");
                    Element itemDescElement = (Element)itemDescList.item(0);

                    NodeList textItemDescList =  itemDescElement.getChildNodes();
                    System.out.println("	NPC : Description : " + ((Node)textItemDescList.item(0)).getNodeValue().trim());
                    String npcDesc = ((Node)textItemDescList.item(0)).getNodeValue().trim();
                    
                   
                    //----
                    NodeList synList = firstNPCElement.getElementsByTagName("synonym");
		            int totalSyns = synList.getLength();
		            System.out.println("Number of synonyms read: " + totalSyns);
                    
		            List<String> newSyns = new ArrayList<String>();
		            
                    for(int l=0; l<npcList.getLength(); l++){
                    	if (synList.item(l) != null)
                		{
	                    	Element synElement = (Element)synList.item(l);
	                    	NodeList childSynList = synElement.getChildNodes();
	                   // 	System.out.println("	item : Synonym : " + ((Node)synList.item(0)).getNodeValue().trim());
		                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
                		}
                    }
                        
                    Dialogue tempDialogue = new Dialogue();
	            	List<Dialogue> newDialogues = tempDialogue.loadStateFromXML(firstNPCElement);
                    
                    newNPCs.add(new NPCharacter(npcName,npcDesc,newSyns, newDialogues));
	                }
	        	}
        	}
        }
		return newNPCs;
	}
	
}
