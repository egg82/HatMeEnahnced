package me.egg82.hme.reflection.light;

import org.bukkit.Location;

public class NullLightHelper implements ILightHelper {
	//vars
	
	//constructor
	private NullLightHelper() {
		
	}
	
	//public
	public void addLight(Location loc, boolean async) {
		
	}
	public void removeLight(Location loc, boolean async) {
		
	}
	public void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		
	}
	public void removeAllLights() {
		
	}

	public boolean isValidLibrary() {
		return false;
	}
	
	//private
	
}
