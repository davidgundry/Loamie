/*
Loamie - A MUD Engine
Copyright (C) 2012  David Gundry, Adam Gundry

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package uk.co.gundry.david.loamie.mud.world;

import uk.co.gundry.david.loamie.mud.net.SocketThread;

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
