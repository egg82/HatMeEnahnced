package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class EntityDamageByEntityEventCommand extends EventHandler<EntityDamageByEntityEvent> {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityDamageByEntityEventCommand() {
		super();
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		UUID damagerUuid = event.getDamager().getUniqueId();
		UUID entityUuid = event.getEntity().getUniqueId();
		
		// Can't damage your own hat!
		if (damagerUuid.equals(mobRegistry.getRegister(entityUuid))) {
			event.setCancelled(true);
		}
	}
}
