package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerKickEvent;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.interfaces.ILightHelper;

public class PlayerKickEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(PluginServiceType.LIGHT_HELPER);
	
	//constructor
	public PlayerKickEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
		PlayerKickEvent e = (PlayerKickEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		glowRegistry.computeIfPresent(e.getPlayer().getUniqueId().toString(), (k, v) -> {
			Location loc = e.getPlayer().getLocation().clone();
			loc.setX(loc.getBlockX());
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ());
			
			lightHelper.removeLight(loc, true);
			return null;
		});
	}
}
