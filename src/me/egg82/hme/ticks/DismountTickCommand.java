package me.egg82.hme.ticks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.bukkit.handlers.TickHandler;
import ninja.egg82.bukkit.reflection.entity.IEntityHelper;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;

public class DismountTickCommand extends TickHandler {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityHelper = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public DismountTickCommand() {
		super(0L, 5L);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		for (UUID key : mobRegistry.getKeys()) {
			e(key, CommandUtil.getPlayerByUuid(key), mobRegistry.getRegister(key));
		}
	}
	private void e(UUID uuid, Player player, UUID entityUuid) {
		if (player == null) {
			return;
		}
		
		if (entityUuid.equals(mobRegistry.getRegister(uuid))) {
			Entity entity = Bukkit.getEntity(entityUuid);
			if (!entityHelper.getPassengers(player).contains(entity)) {
				// Your hat can't dismount you!
				entityHelper.addPassenger(player, entity);
			}
		}
	}
}
