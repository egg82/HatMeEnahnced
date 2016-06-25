package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.LightHelper;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

public class PlayerTeleportEventCommand extends EventCommand {
	//vars
	IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	
	//constructor
	public PlayerTeleportEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
		PlayerTeleportEvent e = (PlayerTeleportEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		Location from = e.getFrom().clone();
		from.setX(from.getBlockX());
		from.setY(from.getBlockY() + 1.0d);
		from.setZ(from.getBlockZ());
		
		Location to = e.getTo().clone();
		to.setX(to.getBlockX());
		to.setY(to.getBlockY() + 1.0d);
		to.setZ(to.getBlockZ());
		
		if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
			return;
		}
		
		if (glowRegistry.contains(e.getPlayer().getName().toLowerCase())) {
			LightHelper.recreateLight(from, to, true);
		}
	}
}
