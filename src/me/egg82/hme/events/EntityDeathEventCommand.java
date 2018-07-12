package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityDeathEvent;

import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class EntityDeathEventCommand extends EventHandler<EntityDeathEvent> {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityDeathEventCommand() {
		super();
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		UUID uuid = event.getEntity().getUniqueId();
		mobRegistry.removeRegister(uuid);
		UUID key = mobRegistry.getKey(uuid);
		if (key != null) {
			mobRegistry.removeRegister(key);
		}
	}
}
