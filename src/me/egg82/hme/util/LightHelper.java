package me.egg82.hme.util;

import java.util.List;

import org.bukkit.Location;

import ru.beykerykt.lightapi.chunks.ChunkInfo;
import ru.beykerykt.lightapi.chunks.Chunks;
import ru.beykerykt.lightapi.light.LightDataRequest;
import ru.beykerykt.lightapi.light.Lights;

public class LightHelper {
	//vars
	
	//constructor
	public LightHelper() {
		
	}
	
	//public
	public static void addLight(Location loc, boolean async) {
		LightDataRequest req = null;
		
		try {
			req = Lights.createLight(loc, 15, async);
		} catch (Exception ex) {
			
		}
		
		if (req != null) {
			Chunks.addChunkToQueue(req);
		} else {
			List<ChunkInfo> chunks = Chunks.collectModifiedChunks(loc);
			if (chunks != null) {
				for (ChunkInfo info : chunks) {
					Chunks.sendChunkUpdate(info);
				}
			}
		}
	}
	public static void removeLight(Location loc, boolean async) {
		LightDataRequest req = null;
		
		try {
			req = Lights.deleteLight(loc, async);
		} catch (Exception ex) {
			
		}
		
		if (req != null) {
			Chunks.addChunkToQueue(req);
		} else {
			List<ChunkInfo> chunks = Chunks.collectModifiedChunks(loc);
			if (chunks != null) {
				for (ChunkInfo info : chunks) {
					Chunks.sendChunkUpdate(info);
				}
			}
		}
	}
	public static void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		LightDataRequest req = null;
		List<ChunkInfo> chunks = null;
		
		try {
			Lights.deleteLight(oldLoc, async);
			req = Lights.createLight(newLoc, 15, async);
		} catch (Exception ex) {
			
		}
		
		if (req != null) {
			Chunks.addChunkToQueue(req);
		} else {
			chunks = Chunks.collectModifiedChunks(oldLoc);
			if (chunks != null) {
				for (ChunkInfo info : chunks) {
					Chunks.sendChunkUpdate(info);
				}
			}
			chunks = Chunks.collectModifiedChunks(newLoc);
			if (chunks != null) {
				for (ChunkInfo info : chunks) {
					Chunks.sendChunkUpdate(info);
				}
			}
		}
	}
	
	//private
	
}
