package uk.co.thunderbadger.mud.world.npc;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Dialogue implements Serializable {

	private static final long serialVersionUID = 1L;
	private String line;
	private int trigger;
	private List<Dialogue> dialogues = new ArrayList<Dialogue>();
	private String action;
	
	public Dialogue()
	{
		
	}
	
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
	
	public void saveStateToXML(PrintStream ps) {
	
		ps.println("		<dialogue>");
		ps.println("			<trigger>" + getTrigger() + "</trigger>");
		ps.println("			<line>" + getline() + "</line>");
		ps.println("			<action>" + getAction() + "</action>");
		
		if (this.dialogues != null)
		{
		    Iterator<Dialogue> i = dialogues.iterator();
		    while(i.hasNext())
		    	i.next().saveStateToXML(ps);
		}
		
		ps.println("		</dialogue>");
	
	}
	
	public List<Dialogue> loadStateFromXML(Element firstNPCElement)
	{// TODO: this isn't working yet.
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		try {
			NodeList listOfDialogues = (NodeList) xpath.evaluate("./dialogue", firstNPCElement,XPathConstants.NODESET);
			//NodeList listOfDialogues = (NodeList) xpath.evaluate("dialogue/dialogue", firstNPCElement,XPathConstants.NODESET);			
			//NodeList listOfDialogues = firstNPCElement.getElementsByTagName("dialogue");
	        int totalDialogues = listOfDialogues.getLength();
	        System.out.println("Number of dialogues read: " + totalDialogues);
	
	        ArrayList<Dialogue> newDialogues = new ArrayList<Dialogue>();
	        
	        for(int s=0; s<listOfDialogues.getLength(); s++){
	
	            Node firstDialogueNode = listOfDialogues.item(s);
	            
	            if(firstDialogueNode.getNodeType() == Node.ELEMENT_NODE){
	
	                Element firstDialogueElement = (Element)firstDialogueNode;
	
			        NodeList commTriggerList = (firstNPCElement).getElementsByTagName("trigger");
			        Element commTriggerElement = (Element)commTriggerList.item(0);
			        NodeList textTriggerCommList =  commTriggerElement.getChildNodes();
			        System.out.println("	NPC : Dialogue : Trigger :" + ((Node)textTriggerCommList.item(0)).getNodeValue().trim());
			        String commTrigger = ((Node)textTriggerCommList.item(0)).getNodeValue().trim();
			        
			        NodeList commLineList = (firstNPCElement).getElementsByTagName("line");
			        Element commLineElement = (Element)commLineList.item(0);
			        NodeList textLineCommList =  commLineElement.getChildNodes();
			        System.out.println("	NPC : Dialogue : Line :" + ((Node)textLineCommList.item(0)).getNodeValue().trim());
			        String commLine = ((Node)textLineCommList.item(0)).getNodeValue().trim();
			        
			        NodeList commActionList = (firstNPCElement).getElementsByTagName("action");
			        Element commActionElement = (Element)commActionList.item(0);
			        NodeList textActionCommList =  commActionElement.getChildNodes();
			        System.out.println("	NPC : Dialogue : Action :" + ((Node)textActionCommList.item(0)).getNodeValue().trim());
			        String commAction = ((Node)textActionCommList.item(0)).getNodeValue().trim();
			        
	                Dialogue tempDialogue = new Dialogue();
	            	List<Dialogue> moreDialogues = tempDialogue.loadStateFromXML(firstDialogueElement);
	            	
	            	newDialogues.add(new Dialogue(Integer.parseInt(commTrigger),commLine, moreDialogues, commAction));
	            }
	        }
	        return newDialogues;
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
}
