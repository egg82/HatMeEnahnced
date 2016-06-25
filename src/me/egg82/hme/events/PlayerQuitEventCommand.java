package me.egg82.hme.events;

import org.bukkit.event.player.PlayerQuitEvent;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.LightHelper;

public class PlayerQuitEventCommand extends EventCommand {
	//vars
	IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	
	//constructor
	public PlayerQuitEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
		PlayerQuitEvent e = (PlayerQuitEvent) event;
		if (glowRegistry.contains(e.getPlayer().getName().toLowerCase())) {
			LightHelper.removeLight(e.getPlayer().getLocation(), true);
		}
	}
}
