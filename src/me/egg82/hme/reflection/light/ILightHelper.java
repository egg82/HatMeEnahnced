package me.egg82.hme.reflection.light;

import org.bukkit.Location;

public interface ILightHelper {
	//functions
	void addLight(Location loc, boolean async);
	void removeLight(Location loc, boolean async);
	void recreateLight(Location oldLoc, Location newLoc, boolean async);
	void removeAllLights();
}
