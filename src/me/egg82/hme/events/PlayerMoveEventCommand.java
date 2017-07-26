package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.plugin.utils.LocationUtil;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowRegistry;

public class PlayerMoveEventCommand extends EventCommand<PlayerMoveEvent> {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerMoveEventCommand(PlayerMoveEvent event) {
		super(event);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		if (!glowRegistry.hasRegister(event.getPlayer().getUniqueId())) {
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
