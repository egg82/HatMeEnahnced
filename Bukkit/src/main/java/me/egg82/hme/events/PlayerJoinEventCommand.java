package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.lists.GlowMaterialSet;
import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class PlayerJoinEventCommand extends EventHandler<PlayerJoinEvent> {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private IConcurrentSet<Material> glowMaterialSet = ServiceLocator.getService(GlowMaterialSet.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerJoinEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null) {
			return;
		}
		
		if (glowMaterialSet.contains(helmet.getType())) {
			UUID uuid = player.getUniqueId();
			
			if (glowSet.add(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
			}
		}
	}
}
