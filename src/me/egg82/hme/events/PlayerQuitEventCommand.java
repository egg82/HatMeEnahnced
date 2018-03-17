package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;
import ninja.egg82.plugin.utils.CommandUtil;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MobRegistry;

public class PlayerQuitEventCommand extends EventCommand<PlayerQuitEvent> {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	
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
		
		if (glowRegistry.hasRegister(uuid)) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			
			lightHelper.removeLight(loc, false);
			glowRegistry.removeRegister(uuid);
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
