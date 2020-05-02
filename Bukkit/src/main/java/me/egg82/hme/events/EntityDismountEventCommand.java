package me.egg82.hme.events;

import java.util.UUID;

import org.spigotmc.event.entity.EntityDismountEvent;

import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.bukkit.reflection.entity.IEntityHelper;
import ninja.egg82.bukkit.utils.TaskUtil;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class EntityDismountEventCommand extends EventHandler<EntityDismountEvent> {
	//vars
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityHelper = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public EntityDismountEventCommand() {
		super();
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		// Re-mount hat
		if (event.getEntity().getUniqueId().equals(mobRegistry.getRegister(event.getDismounted().getUniqueId()))) {
			TaskUtil.runSync(new Runnable() {
				public void run() {
					entityHelper.addPassenger(event.getDismounted(), event.getEntity());
				}
			}, 1L);
		}
	}
}
