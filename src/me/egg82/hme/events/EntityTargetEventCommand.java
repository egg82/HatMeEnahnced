package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.event.entity.EntityTargetEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityTargetEventCommand extends EventCommand<EntityTargetEvent> {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityTargetEventCommand(EntityTargetEvent event) {
		super(event);
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
		if (entityUuid.equals(mobRegistry.getRegister(targetUuid, UUID.class))) {
			event.setCancelled(true);
		}
	}
}
