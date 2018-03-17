package me.egg82.hme.reflection.light;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import ninja.egg82.plugin.utils.TaskUtil;
import ninja.egg82.utils.CollectionUtil;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightAPIHelper implements ILightHelper {
	//vars
	private ConcurrentHashMap<String, Location> lightLocations = new ConcurrentHashMap<String, Location>();
	
	//constructor
	public LightAPIHelper() {
		
	}
	
	//public
	public void addLight(Location loc, boolean async) {
		if (!Bukkit.isPrimaryThread()) {
			TaskUtil.runSync(new Runnable() {
				public void run() {
					addLightInternal(loc, async);
				}
			});
		} else {
			addLightInternal(loc, async);
		}
	}
	public void removeLight(Location loc, boolean async) {
		if (!Bukkit.isPrimaryThread()) {
			TaskUtil.runSync(new Runnable() {
				public void run() {
					removeLightInternal(loc, async);
				}
			});
		} else {
			removeLightInternal(loc, async);
		}
	}
	public void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		if (!Bukkit.isPrimaryThread()) {
			TaskUtil.runSync(new Runnable() {
				public void run() {
					recreateLightInternal(oldLoc, newLoc, async);
				}
			});
		} else {
			recreateLightInternal(oldLoc, newLoc, async);
		}
	}
	public void removeAllLights() {
		if (!Bukkit.isPrimaryThread()) {
			TaskUtil.runSync(new Runnable() {
				public void run() {
					removeAllLightsInternal();
				}
			});
		} else {
			removeAllLightsInternal();
		}
	}
	
	//private
	private void addLightInternal(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		loc = CollectionUtil.putIfAbsent(lightLocations, key, loc);
		
		LightAPI.createLight(loc, 15, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	private void removeLightInternal(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		lightLocations.remove(key);
		
		LightAPI.deleteLight(loc, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	private void recreateLightInternal(Location oldLoc, Location newLoc, boolean async) {
		LightAPI.deleteLight(oldLoc, async);
		LightAPI.createLight(newLoc, 15, async);
		
		String key1 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		String key2 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		lightLocations.remove(key1);
		newLoc = CollectionUtil.putIfAbsent(lightLocations, key2, newLoc);
		
		List<ChunkInfo> oldChunks = LightAPI.collectChunks(oldLoc);
		List<ChunkInfo> newChunks = LightAPI.collectChunks(newLoc);
		
		HashSet<ChunkInfo> updatedChunks = new HashSet<ChunkInfo>();
		updatedChunks.addAll(oldChunks);
		updatedChunks.addAll(newChunks);
		
		for (ChunkInfo info : updatedChunks) {
			LightAPI.updateChunk(info);
		}
	}
	private void removeAllLightsInternal() {
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
}
