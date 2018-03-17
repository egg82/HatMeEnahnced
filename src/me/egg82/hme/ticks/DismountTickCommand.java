package me.egg82.hme.ticks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.TickCommand;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;
import ninja.egg82.plugin.utils.CommandUtil;

public class DismountTickCommand extends TickCommand {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public DismountTickCommand() {
		super();
		ticks = 5L;
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		for (UUID key : mobRegistry.getKeys()) {
			e(key, CommandUtil.getPlayerByUuid(key), mobRegistry.getRegister(key, UUID.class));
		}
	}
	private void e(UUID uuid, Player player, UUID entityUuid) {
		if (player == null) {
			return;
		}
		
		if (entityUuid.equals(mobRegistry.getRegister(uuid))) {
			Entity entity = Bukkit.getEntity(entityUuid);
			if (!entityUtil.getPassengers(player).contains(entity)) {
				// Your hat can't dismount you!
				entityUtil.addPassenger(player, entity);
			}
		}
	}
}
