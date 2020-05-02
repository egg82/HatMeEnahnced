package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.bukkit.reflection.entity.IEntityHelper;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class PlayerQuitEventCommand extends EventHandler<PlayerQuitEvent> {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerQuitEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if (glowSet.remove(uuid)) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			
			lightHelper.removeLight(loc, false);
		}
		
		if (mobRegistry.hasRegister(uuid)) {
			entityUtil.removeAllPassengers(player);
			mobRegistry.removeRegister(uuid);
		}
		UUID bottomUuid = mobRegistry.getKey(uuid);
		if (bottomUuid != null) {
			entityUtil.removePassenger(CommandUtil.getPlayerByUuid(bottomUuid), player);
			mobRegistry.removeRegister(bottomUuid);
		}
	}
}
