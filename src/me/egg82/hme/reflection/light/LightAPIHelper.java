package me.egg82.hme.reflection.light;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightAPIHelper implements ILightHelper {
	//vars
	private HashMap<String, Location> lightLocations = new HashMap<String, Location>();
	
	//constructor
	public LightAPIHelper() {
		
	}
	
	//public
	public void addLight(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		lightLocations.put(key, loc);
		
		LightAPI.createLight(loc, 15, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	public void removeLight(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		lightLocations.remove(key);
		
		LightAPI.deleteLight(loc, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	public void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		LightAPI.deleteLight(oldLoc, async);
		LightAPI.createLight(newLoc, 15, async);
		
		String key1 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		String key2 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		lightLocations.remove(key1);
		lightLocations.put(key2, newLoc);
		
		List<ChunkInfo> oldChunks = LightAPI.collectChunks(oldLoc);
		List<ChunkInfo> newChunks = LightAPI.collectChunks(newLoc);
		
		HashSet<ChunkInfo> updatedChunks = new HashSet<ChunkInfo>();
		updatedChunks.addAll(oldChunks);
		updatedChunks.addAll(newChunks);
		
		for (ChunkInfo info : updatedChunks) {
			LightAPI.updateChunk(info);
		}
	}
	public void removeAllLights() {
		HashSet<ChunkInfo> updatedChunks = new HashSet<ChunkInfo>();
		
		lightLocations.forEach((k, v) -> {
			LightAPI.deleteLight(v, true);
			updatedChunks.addAll(LightAPI.collectChunks(v));
		});
		lightLocations.clear();
		
		for (ChunkInfo info : updatedChunks) {
			LightAPI.updateChunk(info);
		}
	}
	
	//private
	
}
