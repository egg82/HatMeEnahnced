package me.egg82.hme.reflection.light;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import com.boydti.fawe.example.NMSMappedFaweQueue;
import com.boydti.fawe.util.SetQueue;

import ninja.egg82.core.CollectionUtil;

public class FAWELightHelper implements ILightHelper {
	//vars
	private ConcurrentHashMap<String, Location> lightLocations = new ConcurrentHashMap<String, Location>();
	private ConcurrentHashMap<String, NMSMappedFaweQueue<?, ?, ?, ?>> queues = new ConcurrentHashMap<String, NMSMappedFaweQueue<?, ?, ?, ?>>();
	
	//constructor
	public FAWELightHelper() {
		
	}
	
	//public
	public void addLight(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		loc = CollectionUtil.putIfAbsent(lightLocations, key, loc);
		
		NMSMappedFaweQueue<?, ?, ?, ?> queue = getQueue(loc);
		try {
			queue.setBlockLight(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 15);
		} catch (Exception ex) {
			
		}
	}
	public void removeLight(Location loc, boolean async) {
		String key = loc.getWorld() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		lightLocations.remove(key);
		
		NMSMappedFaweQueue<?, ?, ?, ?> queue = getQueue(loc);
		try {
			queue.setBlockLight(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0);
			queue.relight(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		} catch (Exception ex) {
			
		}
	}
	public void recreateLight(Location oldLoc, Location newLoc, boolean async) {
		String key1 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		String key2 = oldLoc.getWorld() + "," + oldLoc.getX() + "," + oldLoc.getY() + "," + oldLoc.getZ();
		lightLocations.remove(key1);
		newLoc = CollectionUtil.putIfAbsent(lightLocations, key2, newLoc);
		
		NMSMappedFaweQueue<?, ?, ?, ?> queue1 = getQueue(oldLoc);
		NMSMappedFaweQueue<?, ?, ?, ?> queue2 = getQueue(newLoc);
		
		try {
			queue1.setBlockLight(oldLoc.getBlockX(), oldLoc.getBlockY(), oldLoc.getBlockZ(), 0);
			queue1.relight(oldLoc.getBlockX(), oldLoc.getBlockY(), oldLoc.getBlockZ());
			queue2.setBlockLight(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ(), 15);
		} catch (Exception ex) {
			
		}
	}
	public void removeAllLights() {
		lightLocations.forEach((k, v) -> {
			NMSMappedFaweQueue<?, ?, ?, ?> queue = getQueue(v);
			try {
				queue.setBlockLight(v.getBlockX(), v.getBlockY(), v.getBlockZ(), 0);
				queue.relight(v.getBlockX(), v.getBlockY(), v.getBlockZ());
			} catch (Exception ex) {
				
			}
		});
		lightLocations.clear();
	}
	
	//private
	private NMSMappedFaweQueue<?, ?, ?, ?> getQueue(Location loc) {
		return queues.put(loc.getWorld().getName(), (NMSMappedFaweQueue<?, ?, ?, ?>) SetQueue.IMP.getNewQueue(loc.getWorld().getName(), true, true));
	}
}
