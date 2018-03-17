package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityDeathEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityDeathEventCommand extends EventCommand<EntityDeathEvent> {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
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
