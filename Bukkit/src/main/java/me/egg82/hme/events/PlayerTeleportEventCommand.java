package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import ninja.egg82.bukkit.utils.LocationUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class PlayerTeleportEventCommand extends EventHandler<PlayerTeleportEvent> {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerTeleportEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		if (!glowSet.contains(event.getPlayer().getUniqueId())) {
			return;
		}
		
		Location from = event.getFrom().clone();
		from.setX(from.getBlockX() + 0.5d);
		from.setY(from.getBlockY() + 1.0d);
		from.setZ(from.getBlockZ() + 0.5d);
		
		Location to = event.getTo().clone();
		to.setX(to.getBlockX() + 0.5d);
		to.setY(to.getBlockY() + 1.0d);
		to.setZ(to.getBlockZ() + 0.5d);
		
		if (LocationUtil.areEqualXYZ(from, to)) {
			return;
		}
		
		lightHelper.recreateLight(from, to, true);
	}
}
