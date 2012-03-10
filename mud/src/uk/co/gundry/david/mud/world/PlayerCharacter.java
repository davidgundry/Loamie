package uk.co.gundry.david.mud.world;

import uk.co.gundry.david.mud.net.SocketThread;

/**
 * Represents a character that can be controlled by a human (well, a socket connection, at any rate).
 * Note that the character need not be controlled all the time. This class should only add things related
 * to the socket connection, and any mechanical things should be a part of GameCharacter or above.
 * 
 * @author Adam Gundry extended by David Gundry
 */
public final class PlayerCharacter extends GameCharacter
{	
	private static final long serialVersionUID = 1L;
	private transient volatile SocketThread thread;
	
	/**
	 * Create a new player character with the given name.
	 * 
	 * @param name
	 */
	public PlayerCharacter(String name, String description)
	{
		super(name, description);
	}
	
	/**
	 * Convert a GameCharacter into a PlayerCharacter
	 * @param gc
	 */
	public PlayerCharacter(GameCharacter gc) {
		this.setSynonyms(gc.getSynonyms());
		this.setLocation(gc.getLocation());
		this.lastRoom = gc.getLastRoom();
		this.hitPoints = gc.getHitPoints();
		this.xp = gc.getXp();
		this.setName(gc.getName());
		this.setDescription(gc.getDescription());
	}

	/**
	 * If a thread is attached, forwards the message through the socket.
	 */
	@Override
	public void receiveMessage(String text)
	{
		if (thread != null){
			String[] lines = text.split("[\\r\\n]+");
			for(int i=0;i<lines.length;i++){
				thread.sendMessage("          " + lines[i].replace("_"," "));
			}
		}
	}
	
	/**
	 * If a thread is attached, forwards the message through the socket.
	 */
	@Override
	public void receiveMessageFromPlayer(String text)
	{
		if (thread != null){
			String[] lines = text.split("[\\r\\n]+");
			for(int i=0;i<lines.length;i++){
				thread.sendMessage("                    " + lines[i].replace("_"," "));
			}
		}
	}
	
	/**
	 * Called when a player connects to control this character.
	 * 
	 * @param thread  Controlling socket thread
	 */
	public void playerConnected(SocketThread thread)
	{
		this.thread = thread;
		receiveMessage("Welcome to the game, " + this.getName() + "!");
	}
	
	/**
	 * Called when the attached thread disconnects.	 
	 */
	public void playerDisconnected()
	{
		this.thread = null;
	}
}
