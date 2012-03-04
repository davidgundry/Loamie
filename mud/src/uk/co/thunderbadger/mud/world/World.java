package uk.co.thunderbadger.mud.world;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.thunderbadger.mud.world.item.MapItem;

/**
 * A world object is a container for all data specific to the game the server is running.
 * It contains all of the rooms, objects, etc. Player Characters, however, are stored
 * separately, so that loading a new world won't delete players' characters.
 * 
 * @author David Gundry
 */
public class World implements WorldObject{

	private static final long serialVersionUID = 1L;
	private List<Room> rooms = new ArrayList<Room>();

	private String goodbyeMessage = "";
	private String welcomeMessage = "";
	
	/**
	 * Outputs the current state of the world, and everything it contains to a PrintStream as XML.
	 */
	public void saveStateToXML(PrintStream ps)
	{
		ps.println("<world>");
		ps.println("	<welcome-message>" + welcomeMessage + "</welcome-message>");
		ps.println("	<goodbye-message>" + goodbyeMessage + "</goodbye-message>");
		for (Room room: getRooms())
		{
			room.saveStateToXML(ps);
		}
		ps.println("</world>");
	}
	
	/**
	 * Takes a loaded XML document and creates a new World out of it.
	 * 
	 * @param XMLFilePrefix
	 * @param latestDate
	 * @param df
	 * @return
	 */
	public World restoreStateFromXML(Document doc)
	{
			World newWorld = new World();
			
	        NodeList wmessageList = doc.getDocumentElement().getElementsByTagName("welcome-message");
            Element wmessageElement = (Element)wmessageList.item(0);
            NodeList wmessageChildList =  wmessageElement.getChildNodes();
            if (((Node)wmessageChildList.item(0)) != null)
            	welcomeMessage = ((Node)wmessageChildList.item(0)).getNodeValue().trim();
           
			
	        NodeList gmessageList = doc.getDocumentElement().getElementsByTagName("goodbye-message");
            Element gmessageElement = (Element)gmessageList.item(0);
            NodeList gmessageChildList =  gmessageElement.getChildNodes();
            if (((Node)gmessageChildList.item(0)) != null)
            	goodbyeMessage = ((Node)gmessageChildList.item(0)).getNodeValue().trim();
			
		
	        //------ LOAD ROOMS ------- //
	        
	        NodeList listOfRooms = doc.getElementsByTagName("room");
	        int totalRooms = listOfRooms.getLength();
	        System.out.println("Number of rooms read: " + totalRooms);
	
	        for(int s=0; s<listOfRooms.getLength() ; s++){
	
	            Node firstRoomNode = listOfRooms.item(s);
	            if(firstRoomNode.getNodeType() == Node.ELEMENT_NODE){
	
	
	                Element firstRoomElement = (Element)firstRoomNode;
	
	                //-------
	                NodeList nameList = firstRoomElement.getElementsByTagName("name");
	                Element nameElement = (Element)nameList.item(0);
	
	                NodeList textNameList =  nameElement.getChildNodes();
	                System.out.println("Name : " + ((Node)textNameList.item(0)).getNodeValue().trim());
	                String thisName = ((Node)textNameList.item(0)).getNodeValue().trim();
	
	                //-------
	                NodeList descriptionList = firstRoomElement.getElementsByTagName("description");
	                Element descriptionElement = (Element)descriptionList.item(0);
	
	                NodeList textDescList = descriptionElement.getChildNodes();
	                System.out.println("Description : " + ((Node)textDescList.item(0)).getNodeValue().trim());
	                String thisDesc = ((Node)textDescList.item(0)).getNodeValue().trim();
	
	                //------ LOAD DOORS ------- //
	                
	            	Door tempDoor = new Door();
	            	List<Door> newDoors = tempDoor.loadStateFromXML(firstRoomElement);
	            	
	            	//------ LOAD STANDARD ITEMS ------- //
	                
	            	Item tempItem = new Item();
	            	List<Item> newItems = tempItem.loadStateFromXML(firstRoomElement);
	            	
	            	// ----- LOAD NPCS -------------//
	            	NPCharacter tempNPC = new NPCharacter();
	            	List<NPCharacter> newNPCs = tempNPC.loadStateFromXML(firstRoomElement);
	            	
	            	//------ LOAD MAP ITEMS ------- //
	            	
	            	MapItem tempMapItem = new MapItem();
	            	List<Item> newMapItems = tempMapItem.loadStateFromXML(firstRoomElement);
	            	
	            	
	                Room newRoom = new Room(thisName,thisDesc);
	                for (int k=0; k<newDoors.size();k++)
	                	newRoom.objectEntered(newDoors.get(k));
	                for (int k=0; k<newItems.size();k++)
	                	newItems.get(k).moveTo(newRoom);
	                for (int k=0; k<newMapItems.size();k++)
	                	newMapItems.get(k).moveTo(newRoom);
	                for (int k=0; k<newNPCs.size();k++)
	                	newNPCs.get(k).moveTo(newRoom);
	                
	                newWorld.getRooms().add(newRoom);
	                
	            }
	
	        }
	        
	        for (Room room: newWorld.getRooms())
	        {
	        	for (Door door: room.getDoors())
	        	{
	        		if (!door.findTarget(newWorld.getRooms()))
	        		{
	        			System.out.println("Failed at door " + door.getName() + " in room " + room.getName() + " due to bad target name " + door.getTargetName());
	        			return null;
	        		}
	        	}
	        }
	        
	        
	    /*    
			// Bert as yet not in any save file
			List<Dialogue> convo2x = new ArrayList<Dialogue>();
			convo2x.add(new Dialogue(1,"\"I will heal you.\"","heal 10 actor"));
			convo2x.add(new Dialogue(2,"\"You said Verb\"","shout dance!"));

			List<Dialogue> convo1x = new ArrayList<Dialogue>();
			convo1x.add(new Dialogue(1,"\"You said Cheese\""));
			convo1x.add(new Dialogue(2,"\"You said Apples\""));
			
			List<Dialogue> convox = new ArrayList<Dialogue>();
			convox.add(new Dialogue(1,"\"I am Bert, an NPC in this world.\"\n1 Cheese\n2 Apples", convo1x));
			convox.add(new Dialogue(2,"\"What do you need?\"\n1 Healing, please.\n2 A verb, please.", convo2x));
			
			newWorld.getRooms().get(1).objectEntered(new NPCharacter("Bert","A small green man.","\"Hi there!\"\n1 Tell me about yourself.\n2 Help me!","Farewell then!",convox));		
			
			*/
		return newWorld;
	}
	
	/**
	 * Sets the list of rooms contained in the world. Be careful about doing this while the server has players
	 * connected.
	 * 
	 * @param rooms - the new list of rooms
	 */
	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}

	/**
	 * Get a complete list of all the rooms in the world. 
	 */
	public List<Room> getRooms() {
		return rooms;
	}

	/**
	 * Set the message that is shown when someone logs out of the game.
	 * 
	 * @param goodbyeMessage - the message to show
	 */
	public void setGoodbyeMessage(String goodbyeMessage) {
		this.goodbyeMessage = goodbyeMessage;
	}

	/**
	 * Get the message that is currently displayed to users logging out of the game world.
	 */
	public String getGoodbyeMessage() {
		return goodbyeMessage;
	}

	/**
	 * Set the message that is shown when someone logs on to the server.
	 * It is shown after the server welcome message.
	 * 
	 * @param welcomeMessage - the message to show
	 */
	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	/**
	 * Get the message that is currently displayed to users logging on to the server.
	 */
	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public String describeContents() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public WorldObject getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getSynonyms() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void heal(int value) {
		// TODO Auto-generated method stub
		
	}

	public int interpretCommand(String text, GameCharacter actor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void listenToCommand(String command, PlayerCharacter actor) {
		// TODO Auto-generated method stub
		
	}

	public boolean moveTo(WorldObject location) {
		return false;
		// TODO Auto-generated method stub
		
	}

	public void objectEntered(WorldObject object) {
		// TODO Auto-generated method stub
		
	}

	public void objectExited(WorldObject object) {
		// TODO Auto-generated method stub
		
	}

	public int processCommand(String command, GameCharacter actor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void receiveMessage(String text) {
		// TODO Auto-generated method stub
		
	}

	public void receiveMessageFromPlayer(String text) {
		// TODO Auto-generated method stub
		
	}

	public void setDescription(String newDescription) {
		// TODO Auto-generated method stub
		
	}

	public void setName(String newName) {
		// TODO Auto-generated method stub
		
	}
	
	
}
