package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class EntityTargetLivingEntityEventCommand extends EventHandler<EntityTargetLivingEntityEvent> {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityTargetLivingEntityEventCommand() {
		super();
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getTarget() == null) {
			return;
		}
		
		UUID entityUuid = event.getEntity().getUniqueId();
		UUID targetUuid = event.getTarget().getUniqueId();
		
		// Your own hat can't target you!
		if (entityUuid.equals(mobRegistry.getRegister(targetUuid))) {
			event.setCancelled(true);
		}
	}
}
