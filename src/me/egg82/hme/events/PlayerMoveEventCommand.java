package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.util.ILightHelper;

public class PlayerMoveEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerMoveEventCommand(Event e) {
		super(e);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		PlayerMoveEvent e = (PlayerMoveEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		if (!glowRegistry.hasRegister(e.getPlayer().getUniqueId().toString())) {
			return;
		}
		
		Location from = e.getFrom().clone();
		from.setX(from.getBlockX() + 0.5d);
		from.setY(from.getBlockY() + 1.0d);
		from.setZ(from.getBlockZ() + 0.5d);
		
		Location to = e.getTo().clone();
		to.setX(to.getBlockX() + 0.5d);
		to.setY(to.getBlockY() + 1.0d);
		to.setZ(to.getBlockZ() + 0.5d);
		
		if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
			return;
		}
		
		lightHelper.recreateLight(from, to, true);
	}
}
