package uk.co.thunderbadger.mud.world;

import java.util.ArrayList;
import java.util.List;

public class World {

	private List<Room> rooms = new ArrayList<Room>();

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}

	public List<Room> getRooms() {
		return rooms;
	}
	
	
}
