package me.egg82.hme.util;

import java.util.List;

import org.bukkit.Location;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightAPIHelper implements ILightHelper {
	//vars
	
	//constructor
	public LightAPIHelper() {
		
	}
	
	//public
	public void addLight(Location loc, boolean async) {
		LightAPI.createLight(loc, 15, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	public void removeLight(Location loc, boolean async) {
		LightAPI.deleteLight(loc, async);
		for (ChunkInfo info : LightAPI.collectChunks(loc)) {
			LightAPI.updateChunk(info);
		}
	}
	public void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		LightAPI.deleteLight(oldLoc, async);
		LightAPI.createLight(newLoc, 15, async);
		
		List<ChunkInfo> oldChunks = LightAPI.collectChunks(oldLoc);
		List<ChunkInfo> newChunks = LightAPI.collectChunks(newLoc);
		
		UnifiedSet<ChunkInfo> updatedChunks = new UnifiedSet<ChunkInfo>();
		updatedChunks.addAll(oldChunks);
		updatedChunks.addAll(newChunks);
		
		for (ChunkInfo info : updatedChunks) {
			LightAPI.updateChunk(info);
		}
	}
	
	//private
	
}
