package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;

import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.util.ILightHelper;

public class PlayerKickEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerKickEventCommand(Event e) {
		super(e);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		PlayerKickEvent e = (PlayerKickEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		String uuid = e.getPlayer().getUniqueId().toString();
		
		if (glowRegistry.hasRegister(uuid)) {
			Location loc = e.getPlayer().getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			
			lightHelper.removeLight(loc, false);
			glowRegistry.setRegister(uuid, Player.class, null);
		}
	}
}
