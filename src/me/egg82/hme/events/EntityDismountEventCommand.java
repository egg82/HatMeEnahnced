package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.spigotmc.event.entity.EntityDismountEvent;

import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;

public class EntityDismountEventCommand extends EventCommand<EntityDismountEvent> {
	//vars
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public EntityDismountEventCommand(EntityDismountEvent event) {
		super(event);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		Entity player = event.getDismounted();
		Entity entity = event.getEntity();
		UUID uuid = player.getUniqueId();
		UUID entityUuid = entity.getUniqueId();
		
		// Your hat can't dismount you!
		if (entityUuid.equals(mobRegistry.getRegister(uuid))) {
			entityUtil.addPassenger(player, entity);
		}
	}
}
