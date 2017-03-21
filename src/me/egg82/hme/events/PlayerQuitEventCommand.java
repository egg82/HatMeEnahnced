package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerQuitEvent;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.interfaces.ILightHelper;

public class PlayerQuitEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(PluginServiceType.LIGHT_HELPER);
	
	//constructor
	public PlayerQuitEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
		PlayerQuitEvent e = (PlayerQuitEvent) event;
		
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
