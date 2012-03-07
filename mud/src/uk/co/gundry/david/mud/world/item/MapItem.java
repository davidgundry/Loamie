package uk.co.gundry.david.mud.world.item;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.gundry.david.mud.Game;
import uk.co.gundry.david.mud.world.GameCharacter;
import uk.co.gundry.david.mud.world.Item;
import uk.co.gundry.david.mud.world.PlayerCharacter;
import uk.co.gundry.david.mud.world.WorldObject;

public class MapItem extends Item implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String art;
	String[] targets;
	
	public MapItem(String name, String description, WorldObject location, String art, String[] targets, List<String> synonyms) {
		super(name, description, location, synonyms);
		this.art = art;
		this.targets = targets;
	}
	
	public MapItem(String name, String description, String art, String[] targets, List<String> synonyms) {
		super(name, description, synonyms);
		this.art = art;
		this.targets = targets;
	}
	
	public MapItem() {
		// TODO Auto-generated constructor stub
	}

	public int interpretCommand(String text, GameCharacter actor)
	{
		if (text.toLowerCase().equals("use")){
			if (this.getLocation() == actor)
				this.processCommand(text, actor);
			return 1;
		}
		
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
	
	public void showArt(GameCharacter actor)
	{
		actor.receiveMessage(art);
	}
	
	public void showHelp(GameCharacter actor)
	{
		for (int i=0;i<targets.length;i++)
			actor.receiveMessage((i+1) + " " + targets[i]);
	}
	
	public int processCommand(String command, GameCharacter actor)
	{
		if (command.toLowerCase().equals("use")){
			actor.receiveMessage("You are using the map.");
			showArt(actor);
			showHelp(actor);
			actor.setListener(this);
			return 1;
		} else if (command.toLowerCase().equals("pick up") || command.toLowerCase().equals("get") || command.toLowerCase().equals("grab") || command.toLowerCase().equals("take")){
			if (this.getLocation() != actor)
				this.moveTo(actor);
			return 1;
		} else if (command.toLowerCase().equals("drop") || command.toLowerCase().equals("put down") || command.toLowerCase().equals("lose")){
			if (this.getLocation() == actor){
				this.moveTo(actor.getLocation());
			} else
				actor.receiveMessage("You can\'t drop something you aren\'t carrying!");
				return 1;
		}
		return 0;
	}
	
	public void listenToCommand(String command, PlayerCharacter actor) {
		if (command.toLowerCase().equals("stop") || command.toLowerCase().equals("drop") || command.toLowerCase().equals("put down") || command.toLowerCase().equals("lose") || command.toLowerCase().equals("end") || command.toLowerCase().equals("exit") || command.toLowerCase().equals("quit")){
			actor.receiveMessage("You stop using the map.");
			actor.setListener(null);
			return;
		} else if (command.toLowerCase().equals("help") || command.toLowerCase().equals("look") || command.toLowerCase().equals("view") || command.toLowerCase().equals("places") || command.toLowerCase().equals("map") || command.toLowerCase().equals("look at") || command.toLowerCase().equals("show")){
			showArt(actor);
			showHelp(actor);
			return;
		}
		
		int number;
		try {
			number = Integer.parseInt(command);
		} catch (NumberFormatException ex) {
			actor.receiveMessage("The map does not understand that command.");
			return;
		}
		number = Math.abs(number)-1;
		
		if (number < targets.length){
			actor.receiveMessage("You travel to the " + targets[number]);
			actor.moveToByName(targets[number]);
			actor.setListener(null);
		} else
			actor.receiveMessage("The map does not understand that command.");
	}
	
	public static List<Item> loadStateFromXML(Element firstRoomElement)
	{
		NodeList itemList = firstRoomElement.getElementsByTagName("mapitem");
        int totalItems = itemList.getLength();
        Game.logMessage("Number of mapitems read: " + totalItems);
        
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
                    Game.logMessage("	Item : Name : " + ((Node)textItemNameList.item(0)).getNodeValue().trim());
                    String itemName = ((Node)textItemNameList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList itemDescList = firstItemElement.getElementsByTagName("description");
                    Element itemDescElement = (Element)itemDescList.item(0);

                    NodeList textItemDescList =  itemDescElement.getChildNodes();
                    Game.logMessage("	Item : Description : " + ((Node)textItemDescList.item(0)).getNodeValue().trim());
                    String itemDesc = ((Node)textItemDescList.item(0)).getNodeValue().trim();
                    
           		 //-------
                    NodeList itemArtList = firstItemElement.getElementsByTagName("art");
                    Element itemartElement = (Element)itemArtList.item(0);

                    NodeList textItemArtList =  itemartElement.getChildNodes();
                    Game.logMessage("	Item : Art : " + ((Node)textItemArtList.item(0)).getNodeValue().trim());
                    String itemArt = ((Node)textItemArtList.item(0)).getNodeValue().trim();

                    
                    //----
                    NodeList synList = firstItemElement.getElementsByTagName("synonym");
		            int totalSyns = synList.getLength();
		            Game.logMessage("Number of synonyms read: " + totalSyns);
                    
		            List<String> newSyns = new ArrayList<String>();
		            
                    for(int l=0; l<synList.getLength(); l++){
                    	if (synList.item(l) != null)
                		{
	                    	Element synElement = (Element)synList.item(l);
	                    	NodeList childSynList = synElement.getChildNodes();
	                    	Game.logMessage("	item : Synonym : " + ((Node)childSynList.item(0)).getNodeValue().trim());
		                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
                		}
                    }
                    
                    //----
                    NodeList targetList = firstItemElement.getElementsByTagName("target");
		            int totalTargets = targetList.getLength();
		            Game.logMessage("Number of targets read: " + totalTargets);
                    
		            String[] newTargets = new String[targetList.getLength()];
		            
                    for(int l=0; l<targetList.getLength(); l++){
                    	if (targetList.item(l) != null)
                		{
	                    	Element TargetElement = (Element)targetList.item(l);
	                    	NodeList childTargetList = TargetElement.getChildNodes();
	                    	Game.logMessage("	item : Target : " + ((Node)childTargetList.item(0)).getNodeValue().trim());
		                    newTargets[l] = ((Node)childTargetList.item(0)).getNodeValue().trim();
                		}
                    }
                    
                    
                    newItems.add(new MapItem(itemName,itemDesc,itemArt,newTargets,newSyns));
	                }
	        	}
        	}
        }
		return newItems;
	}
	
	public void saveStateToXML(PrintStream ps)
	{
		ps.println("		<mapitem>");
		ps.println("			<name>"+this.getName()+"</name>");
		ps.println("			<description>"+this.getDescription()+"</description>");
		if (art != null)
			ps.println("			<art>"+this.art+"</art>");
		else
			ps.println("			<art> </art>");
		if (this.getSynonyms() != null)
			for (String syn: this.getSynonyms())
			{
				ps.println("			<synonym>"+syn+"</synonym>");
			}
		if (this.targets != null)
			for (String str: this.targets)
			{
				ps.println("			<target>"+str+"</target>");
			}
		
		ps.println("		</mapitem>");
	}

}
