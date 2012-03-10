package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.gundry.david.mud.Game;

/**
 * Represents a character in the game. Subclasses may represent player characters or
 * those with some kind of AI control.
 * 
 * @author Adam Gundry
 */
public class GameCharacter implements Serializable, WorldObject
{

	private static final long serialVersionUID = 1L;
	// name cannot be the same as a room (Item drop code)
	protected String name;	
	protected String description;	
	
	protected List<String> synonyms = new ArrayList<String>();
	
	protected Room location;
	protected Room lastRoom;
	int lastRoomID;
	protected int hitPoints;
	protected int xp;
	
	WorldObject listener;

	private List<WorldObject> contents = new ArrayList<WorldObject>();
	
	public static final int TYPE = 0;
	
	/**
	 * Create a new character with the given name and description
	 * 
	 * @param name
	 * @param description
	 */
	public GameCharacter(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.hitPoints = 10;
		this.xp = 0;
		this.listener = null;
	}
	
	public GameCharacter()
	{
		
	}
	
	public GameCharacter(String name, String description, int hitPoints)
	{
		this.name = name;
		this.description = description;
		this.hitPoints = hitPoints;
		this.xp = 0;
		this.listener = null;
	}
	
	public GameCharacter(String name, String description, List<String> synonyms)
	{
		this.name = name;
		this.description = description;
		this.synonyms = synonyms;
		this.hitPoints = 10;
		this.xp = 0;
		this.listener = null;
	}
	
	public GameCharacter(String gcName, String gcDesc, List<String> newSyns,int hp, int xp,int lastRm, Room location) {
		this.name = gcName;
		this.description = gcDesc;
		this.synonyms = newSyns;
		this.hitPoints = hp;
		this.xp = xp;
		this.listener = null;
		this.location = location;
		this.lastRoomID = lastRm;
	}

	/**
	 * We do nothing with messages received. Subclasses should override this.
	 */
	public void receiveMessage(String text)
	{
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setDescription(String newDescription){
		description = newDescription;
	}
	
	public Room getLocation()
	{
		return location;
	}
	
	public Room getLastRoom()
	{
		return lastRoom;
	}
	
	public void setLastRoom(Room lastRoom)
	{
		this.lastRoom = lastRoom;
	}
	
	public int getHitPoints()
	{
		return hitPoints;
	}
	
	public int getXp()
	{
		return xp;
	}
	
	public int getType(){
		return TYPE;
	}
	
	/**
	 * Moves this character to the given room, notifying the current location and destination
	 * as appropriate.
	 * 
	 * @param location
	 */
	public boolean moveTo(WorldObject location)
	{
		if (location != null)
		{
			if (this.location != null)
				this.location.objectExited(this);
			this.lastRoom = this.location;
			this.location = (Room) location;
			location.objectEntered(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Says the given text out loud. This emits an appropriate message to the surrounding room. 
	 * 
	 * @param text  Words to speak
	 */
	public void say(String text)
	{
		location.receiveMessage(String.format("%s says, \"%s\"", name, text));
	}
	public void shout(String text)
	{
		location.receiveMessage(String.format("%s shouts, \"%s\"", name, text));
	}
	public void rpAction(String text)
	{
		location.receiveMessage(String.format("%s %s", name, text.trim()));
	}
	public void ownerlessRpAction(String text)
	{
		location.receiveMessage(text);
	}
	public void look()
	{
		receiveMessage(location.getName() + "\n" + location.getDescription() + "\n" + location.describeContents().replace(this.name+",", ""));
	}
	public void objectLook(String text)
	{
		if (text.equals("me") || text.equals("self") || text.equals("myself"))
		{
			receiveMessage(this.getDescription() + "\n" + this.describeContents());
		} else {
			WorldObject object = location.getContentsByName(text);
			if (object != null)
				receiveMessage(object.getDescription() + "\n" + object.describeContents());
			else {
				WorldObject object2 = this.getContentsByName(text);
				if (object2 != null)
					receiveMessage("Inventory:" + object2.getDescription() + "\n" + object2.describeContents());
				else
					receiveMessage("look at what?");
			}
				
			 
		}
	}
		
	public String getDescription()
	{
		return this.description;
	}
	
	public String describeContents()
	{
		String contentsText = "";
		if (contents.size() > 0)
		{
			contentsText += "\nInventory: ";
			for (WorldObject object: contents)
				contentsText += object.getName() + ", ";
		}
		else
			contentsText = "\nInventory: (empty)";
		
		return contentsText;
	}
	
	public String characterSheet()
	{
		return this.getName() + " (" + this.getLocation().getName() + ")\n" + this.getDescription() + "\n" + this.characterStats() + this.describeContents();
	}
	
	public String characterStats()
	{
		return "Hitpoints: " + this.hitPoints + "\nXp: " + this.xp;
	}

	public WorldObject getContentsByName(String name)
	{
	    for (WorldObject object: contents){
	    	if (object.getName().toLowerCase().equals(name.toLowerCase()))
	    		return object;
	    }
	    for (WorldObject object: contents){
	    	if (object.getSynonyms() != null)
		    	for (String text: object.getSynonyms()){
		    		if (text.toLowerCase().equals(name.toLowerCase()))
		    			return object;
	    	}
	    }
	    return null;
	}
	
	public int interpretCommand(String command, GameCharacter actor)
	{
		if (command.toLowerCase().startsWith("attack "))
		{
			this.hitPoints -= 1;
			actor.getLocation().receiveMessage(actor.getName() + " attacks " + this.getName() + ".");
			return 1;
		} else
			return 0;
	}
	
	/**
	 * Called when an object enters this character's inventory. Adds it to the contents list.
	 * 
	 * @param object
	 */
	public void objectEntered(WorldObject object)
	{
		contents.add(object);		
		this.receiveMessage(String.format("You have gained a %s.", object.getName()));
	}
	
	/**
	 * Calls when an object leaves this character's inventory. Removes it from the contents list.
	 * @param object
	 */
	public void objectExited(WorldObject object)
	{
		contents.remove(object);
		this.receiveMessage(String.format("You have lost a %s.", object.getName()));
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void heal(int value) {
		this.hitPoints = this.hitPoints + value;
		if (value > 0)
			this.receiveMessage("You have been healed " + value + " points.");
		else 
			this.receiveMessage("You have been harmed " + -value + " points.");
		
	}

	public int processCommand(String command, GameCharacter actor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void listenToCommand(String command, PlayerCharacter actor) {
		
	}
	
	public WorldObject getListener()
	{
		return listener;
	}
	
	public void setListener(WorldObject listener)
	{
		this.listener = listener;
	}

	/**
	 * This is overrided by PlayerCharacters (with socket connections), which
	 * forward the message over the socket.
	 */
	public void receiveMessageFromPlayer(String text) {
		// TODO Auto-generated method stub
	}

	/**
	 * Moves this GameCharacter to the room with the specified ID.
	 * 
	 * @param roomNo
	 */
	public void moveToByID(int roomNo)
	{
		roomNo = Math.abs(roomNo);
		if (roomNo < Game.getWorld().getRooms().size())
			this.moveTo(Game.getWorld().getRooms().get(roomNo));
		else
			this.receiveMessage("That is not a valid room");
	}

	/**
	 * Moves this GameCharacter to the room with the specified name.
	 * 
	 * @param name
	 */
	public void moveToByName(String name)
	{
		for (Room place: Game.getWorld().getRooms())
			if (place.getName().equals(name)){
				this.moveTo(place);
				return;
			}
		this.receiveMessage("That is not a valid room");
	}
	
	/** 
	 * Writes all of the information to save in XML format to the supplied PrintStream
	 */
	public void saveStateToXML(PrintStream ps) {
		
		ps.println("		<game-character>");
		ps.println("			<name>"+this.name+"</name>");
		ps.println("			<description>"+this.description+"</description>");
		ps.println("			<hp>" + hitPoints + "</hp>");
		ps.println("			<xp>" + xp + "</xp>");
		boolean foundIt = false;
		for (int i=0;i<Game.getWorld().getRooms().size();i++)
		{
			if (Game.getWorld().getRooms().get(i).getName().equals(location.getName()))
			{
				foundIt = true;
				ps.println("			<location>" + i + "</location>");
				if (foundIt)
					break;
			}
		}
		foundIt = false;
		for (int i=0;i<Game.getWorld().getRooms().size();i++)
		{
			if (Game.getWorld().getRooms().get(i).getName().equals(lastRoom.getName()))
			{
				foundIt = true;
				ps.println("			<last-room>" + i + "</last-room>");
			}
			if (foundIt)
				break;
		}
		
		for (String syn: synonyms)
		{
			ps.println("			<synonym>"+ syn + "</synonym>");
		}
		
		for (WorldObject cont: this.contents)
		{
			cont.saveStateToXML(ps);
		}
		ps.println("		</game-character>");
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

	public List<WorldObject> getContents() {
		return contents;
	}

	public void setContents(List<WorldObject> contents) {
		this.contents = contents;
	}
	    
	public static List<GameCharacter> loadStateFromXML(Element firstRoomElement)
	{
		NodeList gcList = firstRoomElement.getElementsByTagName("game-character");
        int totalItems = gcList.getLength();
        Game.logMessage("Number of gcs read: " + totalItems);
        
        List<GameCharacter> newGCs = new ArrayList<GameCharacter>();
        
    	Element gcElement = (Element)gcList.item(0);
    	if (gcElement != null)
		{
    	NodeList childItemList = gcElement.getChildNodes();
    	
    	for(int j=0; j<childItemList.getLength(); j++){
    		if (gcList.item(j) != null)
    		{
        		Node firstgcNode = gcList.item(j);
	                if(firstgcNode.getNodeType() == Node.ELEMENT_NODE){
        		
            		Element firstgcElement = (Element)firstgcNode;
            		
            		 //-------
                    NodeList itemNameList = firstgcElement.getElementsByTagName("name");
                    Element itemNameElement = (Element)itemNameList.item(0);

                    NodeList textItemNameList =  itemNameElement.getChildNodes();
                    Game.logMessage("	GC : Name : " + ((Node)textItemNameList.item(0)).getNodeValue().trim());
                    String gcName = ((Node)textItemNameList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList itemDescList = firstgcElement.getElementsByTagName("description");
                    Element itemDescElement = (Element)itemDescList.item(0);

                    NodeList textItemDescList =  itemDescElement.getChildNodes();
                    Game.logMessage("	GC : Description : " + ((Node)textItemDescList.item(0)).getNodeValue().trim());
                    String gcDesc = ((Node)textItemDescList.item(0)).getNodeValue().trim();
                    
                    //-------
                    NodeList nList = firstgcElement.getElementsByTagName("xp");
                    Element nElement = (Element)nList.item(0);

                    NodeList tnList =  nElement.getChildNodes();
                    Game.logMessage("	GC : XP : " + ((Node)tnList.item(0)).getNodeValue().trim());
                    int xp = Integer.parseInt(((Node)tnList.item(0)).getNodeValue().trim());
                   
                    //-------
                    nList = firstgcElement.getElementsByTagName("hp");
                    nElement = (Element)nList.item(0);

                    tnList =  nElement.getChildNodes();
                    Game.logMessage("	GC : HP : " + ((Node)tnList.item(0)).getNodeValue().trim());
                    int hp = Integer.parseInt(((Node)tnList.item(0)).getNodeValue().trim());
                   
                    
                    //-------
                    Room gcLoc = new Room();
                    nList = firstgcElement.getElementsByTagName("location");
                    if (nList.getLength()>0)
                    {
	                    nElement = (Element)nList.item(0);
	                    tnList =  nElement.getChildNodes();
	                    Game.logMessage("	GC : Location : " + ((Node)tnList.item(0)).getNodeValue().trim());
	                    int loc = Integer.parseInt(((Node)tnList.item(0)).getNodeValue().trim());
	                    gcLoc = Game.getWorld().getRooms().get(loc);
                    }
                    else
                    {
                    	Game.logError("<location> missing for GC " + gcName + "Setting to Limbo.", null);
                    	gcLoc = Game.getWorld().getRooms().get(0);
                    }
                    
                    //-------
                    int gcLastRm =0;
                    nList = firstgcElement.getElementsByTagName("last-room");
                    if (nList.getLength()>0)
                    {
	                    nElement = (Element)nList.item(0);
	                    tnList =  nElement.getChildNodes();
	                    Game.logMessage("	GC : Last Room : " + ((Node)tnList.item(0)).getNodeValue().trim());
	                    gcLastRm = Integer.parseInt(((Node)tnList.item(0)).getNodeValue().trim());
                    }
                    else
                    {
                    	Game.logError("<last-room> missing for GC " + gcName + "Setting to Limbo.", null);
                    	gcLastRm = 0;
                    }
                    
                    //----
                    NodeList synList = firstgcElement.getElementsByTagName("synonym");
		            int totalSyns = synList.getLength();
		            Game.logMessage("Number of synonyms read: " + totalSyns);
                    
		            List<String> newSyns = new ArrayList<String>();
		            
                    for(int l=0; l<gcList.getLength(); l++){
                    	if (synList.item(l) != null)
                		{
	                    	Element synElement = (Element)synList.item(l);
	                    	NodeList childSynList = synElement.getChildNodes();
	                   // 	Game.logMessage("	item : Synonym : " + ((Node)synList.item(0)).getNodeValue().trim());
		                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
                		}
                    }
                        
                    newGCs.add(new GameCharacter(gcName,gcDesc,newSyns,hp,xp,gcLastRm,gcLoc));
	                }
	        	}
        	}
        }
		return newGCs;
	}

	public void lastRoomFromID() {
		this.lastRoom = Game.getWorld().getRooms().get(lastRoomID);
	}
	
}
