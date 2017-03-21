package me.egg82.hme.util.interfaces;

import org.bukkit.Location;

public interface ILightHelper {
	void addLight(Location loc, boolean async);
	void removeLight(Location loc, boolean async);
	void recreateLight(Location oldLoc, Location newLoc, boolean async);
}
