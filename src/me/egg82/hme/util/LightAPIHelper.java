package me.egg82.hme.util;

import java.util.List;

import org.bukkit.Location;

import me.egg82.hme.util.interfaces.ILightHelper;
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
		for (ChunkInfo info : oldChunks) {
			LightAPI.updateChunk(info);
		}
		for (ChunkInfo info : LightAPI.collectChunks(newLoc)) {
			boolean good = true;
			for (int i = 0; i < oldChunks.size(); i++) {
				ChunkInfo info2 = oldChunks.get(i);
				if (info2.getChunkX() == info.getChunkX() && info2.getChunkZ() == info.getChunkZ()) {
					good = false;
					break;
				}
			}
			
			if (good) {
				LightAPI.updateChunk(info);
			}
		}
	}
	
	//private
	
}
