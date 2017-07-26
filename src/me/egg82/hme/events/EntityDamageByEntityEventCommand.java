package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityDamageByEntityEventCommand extends EventCommand<EntityDamageByEntityEvent> {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityDamageByEntityEventCommand(EntityDamageByEntityEvent event) {
		super(event);
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
		if (damagerUuid.equals(mobRegistry.getRegister(entityUuid, UUID.class))) {
			event.setCancelled(true);
		}
	}
}
