package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for a door. A WorldItem that conveys people from one room to another, one way only.
 * 
 * @author David Gundry
 */
public class Door implements WorldObject, Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private Room target;
	private String targetName;
	
	private List<String> synonyms;
	
	public Door(String name, String description, Room target){
		this.name = name;
		this.description = description;
		this.target = target;
	}
	
	public Door(String name, String description, String target, List<String> synonyms){
		this.name = name;
		this.description = description;
		this.target = null;
		this.targetName = target;
		this.synonyms = synonyms;
	}
	
	public Door(String name, String description, Room target, List<String> synonyms){
		this.name = name;
		this.description = description;
		this.target = target;
		this.synonyms = synonyms;
	}
	
	public Door() {
		// TODO Auto-generated constructor stub
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
	
	public Room getTarget(){
		return target;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setDescription(String newDescription){
		description = newDescription;
	}
	
	public int getType(){
		return 3;
	}
	
	public String getTargetName()
	{
		return targetName;
	}
	
	public String describeContents(){
		return "";
	}
	
	public boolean findTarget(List<Room> roomList){
		boolean foundIt = false;
		for (Room room: roomList)
		{
			if (room.getName().equals(targetName))
			{
				foundIt = true;
				this.target = room;
			}
		}
		if (!foundIt)
			return false;
		else
			return true;
	}

	public int interpretCommand(String command, GameCharacter actor) {
		if (command.toLowerCase().equals("use") || command.toLowerCase().startsWith("go"))
		{			
			if (actor.moveTo(target))
				actor.getLastRoom().receiveMessage(actor.getName() + " goes through the " + this.getName() );
			else
				actor.receiveMessage("This door doesn't go anywhere.");
			return 1;
		} else
			return 0;
	}
	
	public boolean moveTo(WorldObject target)
	{
		return false;

	}
	
	public void objectEntered(WorldObject object)
	{

	}

	public void objectExited(WorldObject object)
	{

	}

	public void receiveMessage(String text) {
		// TODO Auto-generated method stub

	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public WorldObject getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public void heal(int value) {
		// TODO Auto-generated method stub
		
	}

	public int processCommand(String command, GameCharacter actor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void listenToCommand(String command, PlayerCharacter actor) {
		// TODO Auto-generated method stub
		
	}

	public void receiveMessageFromPlayer(String text) {
		// TODO Auto-generated method stub
		
	}

	public void saveStateToXML(PrintStream ps) {
		ps.println("		<door>");
		ps.println("			<name>"+this.getName()+"</name>");
		ps.println("			<description>"+this.getDescription()+"</description>");
		ps.println("			<target>"+this.getTarget().getName()+"</target>");
		if (this.getSynonyms() != null)
			for (String syn: this.getSynonyms())
			{
				ps.println("			<synonym>"+syn+"</synonym>");
			}
		ps.println("		</door>");
	}

	public List<Door> loadStateFromXML(Element firstRoomElement){
		NodeList doorList = firstRoomElement.getElementsByTagName("door");
        int totalDoors = doorList.getLength();
        System.out.println("Number of doors read: " + totalDoors);
        
        List<Door> newDoors = new ArrayList<Door>();
        
        if (totalDoors > 0)
        {
	    	Element doorElement = (Element)doorList.item(0);
	    	NodeList childDoorList = doorElement.getChildNodes();
	    	
	    	for(int j=0; j<childDoorList.getLength(); j++){
	    		if (doorList.item(j) != null)
	    		{
	        		Node firstDoorNode = doorList.item(j);
		                if(firstDoorNode.getNodeType() == Node.ELEMENT_NODE){
	        		
	            		Element firstDoorElement = (Element)firstDoorNode;
	            		
	            		 //-------
	                    NodeList doorNameList = firstDoorElement.getElementsByTagName("name");
	                    Element doorNameElement = (Element)doorNameList.item(0);
	
	                    NodeList textDoorNameList =  doorNameElement.getChildNodes();
	                    System.out.println("	Door : Name : " + ((Node)textDoorNameList.item(0)).getNodeValue().trim());
	                    String doorName = ((Node)textDoorNameList.item(0)).getNodeValue().trim();
	
	                    //-------
	                    NodeList doorDescList = firstDoorElement.getElementsByTagName("description");
	                    Element doorDescElement = (Element)doorDescList.item(0);
	
	                    NodeList textDoorDescList =  doorDescElement.getChildNodes();
	                    System.out.println("	Door : Description : " + ((Node)textDoorDescList.item(0)).getNodeValue().trim());
	                    String doorDesc = ((Node)textDoorDescList.item(0)).getNodeValue().trim();
	                    
	                    //-------
	                    NodeList doorTargetList = firstDoorElement.getElementsByTagName("target");
	                    Element doorTargetElement = (Element)doorTargetList.item(0);
	
	                    NodeList textDoorTargetList =  doorTargetElement.getChildNodes();
	                    System.out.println("	Door : Target : " + ((Node)textDoorTargetList.item(0)).getNodeValue().trim());
	                    String doorTarget = ((Node)textDoorTargetList.item(0)).getNodeValue().trim();
	                    
	                  //----
	                    NodeList synList = firstDoorElement.getElementsByTagName("synonym");
			            int totalSyns = synList.getLength();
			            System.out.println("Number of synonyms read: " + totalSyns);
	                    
			            List<String> newSyns = new ArrayList<String>();
			            
	                    for(int l=0; l<synList.getLength(); l++){
	                    	if (synList.item(l) != null)
	                		{
		                    	Element synElement = (Element)synList.item(l);
		                    	NodeList childSynList = synElement.getChildNodes();
		                //    	System.out.println("	Door : Synonym : " + ((Node)synList.item(0)).getNodeValue().trim());
			                    newSyns.add(((Node)childSynList.item(0)).getNodeValue().trim());
	                		}
	                    }
	                    	
	                    
	                    newDoors.add(new Door(doorName,doorDesc,doorTarget,newSyns));
		                }
	        	}
	        }
        }
		return newDoors;
	}

}
