package uk.co.gundry.david.loamie.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.gundry.david.loamie.mud.Game;
import uk.co.gundry.david.loamie.mud.ScriptInterpreter;

/**
 * Represents an item. Things which are not rooms, doors or characters. Things which can be picked up and dropped.
 * 
 * @author David Gundry
 */
public class Item extends WorldObject implements Serializable {

	private static final int TYPE = 1;
	private static final long serialVersionUID = 1L;
	private Map<String, String> commands = new HashMap<String,String>();
	

	public Item(String name, String description, WorldObject location)
	{
		super(name,description);
		setLocation(location);
	}
	
	public Item(String name, String description, List<String> synonyms)
	{
		super(name,description,synonyms);
	}
	
	public Item(String name, String description, WorldObject location, List<String> synonyms)
	{
		super(name,description,synonyms,location);
	}
	
	public Item(String name, String description, WorldObject location, List<String> synonyms, Map<String,String> commands)
	{
		super(name,description,synonyms,location);
		this.commands = commands;
	}

	public Item(String itemName, String itemDesc, List<String> newSyns, Map<String, String> newComms) {
		super(itemName,itemDesc,newSyns);
		this.commands = newComms;
	}

	public Item() {
		super();
	}
	
	public void setCommands(HashMap<String,String> commands){
		this.commands = commands;
	}

	public int getType(){
		return TYPE;
	}
	
	/**
	 * Called when the server does not immediately know what a verb does.
	 * Items which are named after the verb are queried to see if they have
	 * either a command script for that verb, or inbuilt code, in that order.
	 * Thus 'pick up' can be overridden by using the same verb for a script.
	 * 
	 * @param text
	 * @param actor
	 */
	public int interpretCommand(String text, GameCharacter actor) {
		if (commands.containsKey(text)){
			String[] todos = commands.get(text).replace("$_actor", actor.getName()).split("; ");
			for (int i=0;i<todos.length;i++)
				this.processCommand(todos[i], actor);
			return 1;
		}
			
		// These commands are global to all Items, unless they are overridden by using the same command in the Items commands Map, and getting it to do something different.
		if (text.toLowerCase().equals("pick up") || text.toLowerCase().equals("get") || text.toLowerCase().equals("grab") || text.toLowerCase().equals("take")){
			this.processCommand(text, actor);
			return 1;
		} else 
		if (text.toLowerCase().equals("drop") || text.toLowerCase().equals("put down") || text.toLowerCase().equals("lose")){
			this.processCommand(text, actor);
			return 1;
		} else
			return 0;
	}

	public int processCommand(String command, GameCharacter actor) {
		ScriptInterpreter si = new ScriptInterpreter();
		return si.interpret(command, this, actor);
	}

	/**
	 * Called when the world is being saved to XML. The item has to make an XML version
	 * of itself and print it to the PrintStream
	 * 
	 * @param printStream
	 */
	public void saveStateToXML(PrintStream ps)
	{
		ps.println("		<item>");
		ps.println("			<name>"+this.getName()+"</name>");
		ps.println("			<description>"+this.getDescription()+"</description>");
		if (this.getSynonyms() != null)
			for (String syn: this.getSynonyms())
			{
				ps.println("			<synonym>"+syn+"</synonym>");
			}
		if (this.getContents() != null)
			for (WorldObject cont: this.getContents())
			{
				ps.println("			<contains>"+cont.getName()+"</contains>");
			}
		
		if (this.commands != null)
		{
			Set<Entry<String,String>> set = commands.entrySet();
		    Iterator<Entry<String,String>> i = set.iterator();
		    while(i.hasNext()){
				Map.Entry<String,String> me = (Map.Entry<String,String>)i.next();
				ps.println("			<command>");
				ps.println("				<label>"+me.getKey()+"</label>");
				ps.println("				<action>"+me.getValue()+"</action>");
				ps.println("			</command>");
			}
		}
		ps.println("		</item>");
	}

	/**
	 * This function is called with a room element, and needs to pick out all of the items
	 * within the room element with all their variables.
	 * 
	 * @param firstRoomElement
	 */
	public static List<Item> loadStateFromXML(Element firstRoomElement)
	{
		NodeList itemList = firstRoomElement.getElementsByTagName("item");
        int totalItems = itemList.getLength();
        if (Game.isDebug())
        	Game.logMessage("Number of items read: " + totalItems);
        
        List<Item> newItems = new ArrayList<Item>();
        
    	Element itemElement = (Element)itemList.item(0);
    	if (itemElement != null)
		{
    	NodeList childItemList = itemElement.getChildNodes();
    	
    	for(int j=0; j<childItemList.getLength(); j++){
    		if (itemList.item(j) != null)
    		{
        		Node firstItemNode = itemList.item(j);
	                if(firstItemNode.getNodeType() == Node.ELEMENT_NODE){
        		
            		Element firstItemElement = (Element)firstItemNode;
            		
            		 //-------
                    NodeList itemNameList = firstItemElement.getElementsByTagName("name");
                    Element itemNameElement = (Element)itemNameList.item(0);

                    NodeList textItemNameList =  itemNameElement.getChildNodes();
                    if (Game.isDebug())
        	        	Game.logMessage("	Item : Name : " + ((Node)textItemNameList.item(0)).getNodeValue().trim());
                    String itemName = ((Node)textItemNameList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList itemDescList = firstItemElement.getElementsByTagName("description");
                    Element itemDescElement = (Element)itemDescList.item(0);

                    NodeList textItemDescList =  itemDescElement.getChildNodes();
                    if (Game.isDebug())
        	        	Game.logMessage("	Item : Description : " + ((Node)textItemDescList.item(0)).getNodeValue().trim());
                    String itemDesc = ((Node)textItemDescList.item(0)).getNodeValue().trim();
                    
                   
                    //----
                    NodeList synList = firstItemElement.getElementsByTagName("synonym");
		            int totalSyns = synList.getLength();
		            if (Game.isDebug())
			        	Game.logMessage("Number of synonyms read: " + totalSyns);
                    
		            List<String> newSyns = new ArrayList<String>();
		            
                    for(int l=0; l<itemList.getLength(); l++){
                    	if (synList.item(l) != null)
                		{
	                    	Element synElement = (Element)synList.item(l);
	                    	NodeList childSynList = synElement.getChildNodes();
	                   // 	Game.logMessage("	item : Synonym : " + ((Node)synList.item(0)).getNodeValue().trim());
		                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
                		}
                    }
                    
                    //----
                    NodeList commList = firstItemElement.getElementsByTagName("command");
		            int totalComms = commList.getLength();
		            if (Game.isDebug())
			        	Game.logMessage("Number of commands read: " + totalComms);
                    
		            Map<String,String> newComms = new HashMap<String,String>();
		            
                    for(int l=0; l<itemList.getLength(); l++){
                    	if (commList.item(l) != null)
                		{
                    		Node firstCommNode = commList.item(l);
     		                if(firstCommNode.getNodeType() == Node.ELEMENT_NODE){
     		                	
     		                	Element firstCommElement = (Element)firstCommNode;
     		                	
			                    //-------
			                    NodeList commLabelList = (firstCommElement).getElementsByTagName("label");
			                    Element commLabelElement = (Element)commLabelList.item(0);

			                    NodeList textLabelCommList =  commLabelElement.getChildNodes();
			                    if (Game.isDebug())
			        	        	Game.logMessage("	Item : Command : Label :" + ((Node)textLabelCommList.item(0)).getNodeValue().trim());
			                    String commLabel = ((Node)textLabelCommList.item(0)).getNodeValue().trim();
			                    
			                    //-------
			                    NodeList commActionList = (firstCommElement).getElementsByTagName("action");
			                    Element commActionElement = (Element)commActionList.item(0);

			                    NodeList textActionCommList =  commActionElement.getChildNodes();
			                    if (Game.isDebug())
			        	        	Game.logMessage("	Item : Command : Action : " + ((Node)textActionCommList.item(0)).getNodeValue().trim());
			                    String commAction = ((Node)textActionCommList.item(0)).getNodeValue().trim();
			                    
			                    newComms.put(commLabel, commAction);
     		                }
                		}
     		        
                    }	
                    
                    newItems.add(new Item(itemName,itemDesc,newSyns,newComms));
	                }
	        	}
        	}
        }
		return newItems;
	}
	
}
