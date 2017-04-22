package me.egg82.hme.events;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityDamageByEntityEventCommand extends EventCommand {
	//vars
	private IRegistry mobRegistry = (IRegistry) ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityDamageByEntityEventCommand(Event event) {
		super(event);
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		String damagerUuid = e.getDamager().getUniqueId().toString();
		String entityUuid = e.getEntity().getUniqueId().toString();
		
		if (damagerUuid.equals(mobRegistry.getRegister(entityUuid))) {
			e.setCancelled(true);
		}
	}
}
