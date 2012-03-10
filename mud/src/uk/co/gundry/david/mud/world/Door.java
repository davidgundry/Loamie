package uk.co.gundry.david.mud.world;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.gundry.david.mud.Game;

/**
 * Class for a door. A WorldItem that conveys people from one room to another, one way only.
 * 
 * @author David Gundry
 */
public class Door extends WorldObject implements Serializable {
	private static final int TYPE = 2;
	private static final long serialVersionUID = 1L;
	private Room target;
	private String targetName;
	
	public Door(String name, String description, Room target){
		super(name, description);
		this.target = target;
	}
	
	public Door(String name, String description, String target, List<String> synonyms){
		super(name, description,synonyms);
		this.target = null;
		this.targetName = target;
	}
	
	public Door(String name, String description, Room target, List<String> synonyms){
		super(name, description,synonyms);
		this.target = target;
	}
	
	public Door() {}
	
	public Room getTarget(){
		return target;
	}
	
	public int getType() {
		return TYPE;
	}
	
	public static int getStaticType() {
		return TYPE;
	}
	
	public String getTargetName()
	{
		return targetName;
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

	public void saveStateToXML(PrintStream ps) {
		ps.println("		<door>");
		ps.println("			<name>"+this.getName()+"</name>");
		ps.println("			<description>"+this.getDescription()+"</description>");
		if ((this.getTarget() != null) && (!this.getTarget().getName().startsWith("DELETE")))
			ps.println("			<target>"+this.getTarget().getName()+"</target>");
		if (this.getSynonyms() != null)
			for (String syn: this.getSynonyms())
			{
				ps.println("			<synonym>"+syn+"</synonym>");
			}
		ps.println("		</door>");
	}

	public static List<Door> loadStateFromXML(Element firstRoomElement){
		NodeList doorList = firstRoomElement.getElementsByTagName("door");
        int totalDoors = doorList.getLength();
        Game.logMessage("Number of doors read: " + totalDoors);
        
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
	                    Game.logMessage("	Door : Name : " + ((Node)textDoorNameList.item(0)).getNodeValue().trim());
	                    String doorName = ((Node)textDoorNameList.item(0)).getNodeValue().trim();
	
	                    //-------
	                    NodeList doorDescList = firstDoorElement.getElementsByTagName("description");
	                    Element doorDescElement = (Element)doorDescList.item(0);
	
	                    NodeList textDoorDescList =  doorDescElement.getChildNodes();
	                    Game.logMessage("	Door : Description : " + ((Node)textDoorDescList.item(0)).getNodeValue().trim());
	                    String doorDesc = ((Node)textDoorDescList.item(0)).getNodeValue().trim();
	                    
	                    //-------
	                    String doorTarget = "";
	                    NodeList doorTargetList = firstDoorElement.getElementsByTagName("target");
	                    if (doorTargetList.getLength() >0)
	                    {
		                    Element doorTargetElement = (Element)doorTargetList.item(0);
		                    
		                    NodeList textDoorTargetList =  doorTargetElement.getChildNodes();
		                    Game.logMessage("	Door : Target : " + ((Node)textDoorTargetList.item(0)).getNodeValue().trim());
		                    doorTarget = ((Node)textDoorTargetList.item(0)).getNodeValue().trim();
	                    }
	                    
	                  //----
	                    NodeList synList = firstDoorElement.getElementsByTagName("synonym");
			            int totalSyns = synList.getLength();
			            Game.logMessage("Number of synonyms read: " + totalSyns);
	                    
			            List<String> newSyns = new ArrayList<String>();
			            
	                    for(int l=0; l<synList.getLength(); l++){
	                    	if (synList.item(l) != null)
	                		{
		                    	Element synElement = (Element)synList.item(l);
		                    	NodeList childSynList = synElement.getChildNodes();
		                    	Game.logMessage("	Door : Synonym : " + (((Node)childSynList.item(0)).getNodeValue().trim()));
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
