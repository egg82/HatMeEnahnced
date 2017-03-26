package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.util.ILightHelper;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class PlayerTeleportEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerTeleportEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		PlayerTeleportEvent e = (PlayerTeleportEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		if (!glowRegistry.hasRegister(e.getPlayer().getUniqueId().toString())) {
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
		
		lightHelper.recreateLight(from, to, true);
	}
}
