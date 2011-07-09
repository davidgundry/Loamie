package uk.co.thunderbadger.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.thunderbadger.mud.ScriptInterpreter;

/**
 * Represents an item. Things which are not rooms, doors or characters. Things which can be picked up and dropped.
 * 
 * @author David Gundry
 */
public class Item implements Serializable, WorldObject {

	private String name;
	private String description;
	private List<WorldObject> contents = new ArrayList<WorldObject>();
	private WorldObject location;
	
	private List<String> synonyms;
	private Map<String, String> commands = new HashMap<String,String>();
	

	public Item(String name, String description, WorldObject location)
	{
		this.name = name;
		this.description = description;
		this.location = location;
	}
	
	public Item(String name, String description, List<String> synonyms)
	{
		this.name = name;
		this.description = description;
		this.synonyms = synonyms;
	}
	
	public Item(String name, String description, WorldObject location, List<String> synonyms)
	{
		this.name = name;
		this.description = description;
		this.location = location;
		this.synonyms = synonyms;
	}
	
	public Item(String name, String description, WorldObject location, List<String> synonyms, Map<String,String> commands)
	{
		this.name = name;
		this.description = description;
		this.location = location;
		this.synonyms = synonyms;
		this.commands = commands;
	}

	public Item(String itemName, String itemDesc, List<String> newSyns, Map<String, String> newComms) {
		this.name = itemName;
		this.description = itemDesc;
		this.synonyms = newSyns;
		this.commands = newComms;
	}

	public Item() {
	}

	public String describeContents() {
		String contentsText = "";
		if (contents.size() > 0)
		{
			contentsText += "\nContents: ";
			for (WorldObject object: contents)
				contentsText += object.getName() + ", ";
		}
		return contentsText;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
	
	public WorldObject getLocation() {
		return location;
	}
	
	public void setCommands(HashMap<String,String> commands){
		this.commands = commands;
	}

	public int getType() {
		return 1;
	}

	public boolean moveTo(WorldObject location) {
		if (location != null)
		{
			if (this.location != null)
				this.location.objectExited(this);
			this.location = location;
			location.objectEntered(this);
			return true;
		}
		return false;
	}
	
	public void moveTo(Room location) {
		if (this.location != null)
			this.location.objectExited(this);
		this.location = location;
		location.objectEntered(this);
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

	public void receiveMessage(String text) {

	}

	public void setDescription(String newDescription) {
		this.description = newDescription;

	}

	public void setName(String newName) {
		this.name = newName;

	}
	
	/**
	 * Called when an object enters this item. Adds it to the contents list.
	 * 
	 * @param object
	 */
	public void objectEntered(WorldObject object)
	{
		contents.add(object);
	}
	
	/**
	 * Calls when an object leaves this item. Removes it from the contents list.
	 * 
	 * @param object
	 */
	public void objectExited(WorldObject object)
	{
		contents.remove(object);
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public int processCommand(String command, GameCharacter actor) {
		ScriptInterpreter si = new ScriptInterpreter();
		return si.interpret(command, this, actor);
	}
	
	public void heal(int value) {
		// TODO Auto-generated method stub
		
	}

	public void listenToCommand(String command, PlayerCharacter actor) {
		// TODO Auto-generated method stub
		
	}

	public void receiveMessageFromPlayer(String text) {
		// TODO Auto-generated method stub
		
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
		if (this.contents != null)
			for (WorldObject cont: this.contents)
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
	public List<Item> loadStateFromXML(Element firstRoomElement)
	{
		NodeList itemList = firstRoomElement.getElementsByTagName("item");
        int totalItems = itemList.getLength();
        System.out.println("Number of items read: " + totalItems);
        
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
                    System.out.println("	Item : Name : " + ((Node)textItemNameList.item(0)).getNodeValue().trim());
                    String itemName = ((Node)textItemNameList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList itemDescList = firstItemElement.getElementsByTagName("description");
                    Element itemDescElement = (Element)itemDescList.item(0);

                    NodeList textItemDescList =  itemDescElement.getChildNodes();
                    System.out.println("	Item : Description : " + ((Node)textItemDescList.item(0)).getNodeValue().trim());
                    String itemDesc = ((Node)textItemDescList.item(0)).getNodeValue().trim();
                    
                   
                    //----
                    NodeList synList = firstItemElement.getElementsByTagName("synonym");
		            int totalSyns = synList.getLength();
		            System.out.println("Number of synonyms read: " + totalSyns);
                    
		            List<String> newSyns = new ArrayList<String>();
		            
                    for(int l=0; l<itemList.getLength(); l++){
                    	if (synList.item(l) != null)
                		{
	                    	Element synElement = (Element)synList.item(l);
	                    	NodeList childSynList = synElement.getChildNodes();
	                   // 	System.out.println("	item : Synonym : " + ((Node)synList.item(0)).getNodeValue().trim());
		                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
                		}
                    }
                    
                    //----
                    NodeList commList = firstItemElement.getElementsByTagName("command");
		            int totalComms = commList.getLength();
		            System.out.println("Number of commands read: " + totalComms);
                    
		            Map<String,String> newComms = new HashMap<String,String>();
		            
                    for(int l=0; l<itemList.getLength(); l++){
                    	if (commList.item(l) != null)
                		{
                    		Node firstCommNode = commList.item(j);
     		                if(firstCommNode.getNodeType() == Node.ELEMENT_NODE){
     		                	
     		                	Element firstCommElement = (Element)firstCommNode;
     		                	
			                    //-------
			                    NodeList commLabelList = (firstCommElement).getElementsByTagName("label");
			                    Element commLabelElement = (Element)commLabelList.item(0);

			                    NodeList textLabelCommList =  commLabelElement.getChildNodes();
			                    System.out.println("	Item : Command : Label :" + ((Node)textLabelCommList.item(0)).getNodeValue().trim());
			                    String commLabel = ((Node)textLabelCommList.item(0)).getNodeValue().trim();
			                    
			                    //-------
			                    NodeList commActionList = (firstCommElement).getElementsByTagName("action");
			                    Element commActionElement = (Element)commActionList.item(0);

			                    NodeList textActionCommList =  commActionElement.getChildNodes();
			                    System.out.println("	Item : Command : Action : " + ((Node)textActionCommList.item(0)).getNodeValue().trim());
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
