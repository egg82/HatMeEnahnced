package me.egg82.hme.util;

import org.bukkit.Location;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightHelper {
	//vars
	
	//constructor
	public LightHelper() {
		
	}
	
	//public
	public static void addLight(Location loc, boolean async) {
		LightAPI.createLight(loc, 15, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunks(info);
		}
	}
	public static void removeLight(Location loc, boolean async) {
		LightAPI.deleteLight(loc, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunks(info);
		}
	}
	public static void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		LightAPI.deleteLight(oldLoc, async);
		LightAPI.createLight(newLoc, 15, async);
		for (ChunkInfo info : LightAPI.collectChunks(oldLoc)) {
			LightAPI.updateChunks(info);
		}
		for (ChunkInfo info : LightAPI.collectChunks(newLoc)) {
			LightAPI.updateChunks(info);
		}
	}
	
	//private
	
}
