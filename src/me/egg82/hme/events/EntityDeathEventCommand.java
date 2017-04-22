package me.egg82.hme.events;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class EntityDeathEventCommand extends EventCommand {
	//vars
	private IRegistry mobRegistry = (IRegistry) ServiceLocator.getService(MobRegistry.class);
	
	//constructor
	public EntityDeathEventCommand(Event event) {
		super(event);
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		EntityDeathEvent e = (EntityDeathEvent) event;
		
		String uuid = e.getEntity().getUniqueId().toString();
		mobRegistry.setRegister(uuid, String.class, null);
		String key = mobRegistry.getName(uuid);
		if (key != null) {
			mobRegistry.setRegister(key, String.class, null);
		}
	}
}
