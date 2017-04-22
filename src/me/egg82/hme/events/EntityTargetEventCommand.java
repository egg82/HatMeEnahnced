package me.egg82.hme.events;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityTargetEventCommand extends EventCommand {
	//vars
	private IRegistry mobRegistry = (IRegistry) ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityTargetEventCommand(Event event) {
		super(event);
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		EntityTargetEvent e = (EntityTargetEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		String entityUuid = e.getEntity().getUniqueId().toString();
		String targetUuid = e.getTarget().getUniqueId().toString();
		
		if (entityUuid.equals(mobRegistry.getRegister(targetUuid))) {
			e.setCancelled(true);
		}
	}
}
