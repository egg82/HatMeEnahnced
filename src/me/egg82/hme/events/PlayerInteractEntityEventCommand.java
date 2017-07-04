package me.egg82.hme.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.HatRegistry;
import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;
import ninja.egg82.plugin.utils.CommandUtil;

public class PlayerInteractEntityEventCommand extends EventCommand {
	//vars
	private IRegistry hatRegistry = (IRegistry) ServiceLocator.getService(HatRegistry.class);
	private IRegistry mobRegistry = (IRegistry) ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityUtil = (IEntityHelper) ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public PlayerInteractEntityEventCommand(Event event) {
		super(event);
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
		
		if (e.isCancelled()) {
			return;
		}
		
		Player player = e.getPlayer();
		Entity entity = e.getRightClicked();
		String entityUuid = entity.getUniqueId().toString();
		String uuid = player.getUniqueId().toString();
		
		// Is the player trying to /hat themselves (or another player)?
		String hatUuid = (String) hatRegistry.getRegister(player.getUniqueId().toString());
		if (hatUuid == null) {
			return;
		}
		
		// Need to make sure the entity isn't already a hat
		if (mobRegistry.hasValue(entityUuid)) {
			player.sendMessage(MessageType.ALREADY_HAT);
			hatRegistry.setRegister(uuid, String.class, null);
			return;
		}
		
		// Is the target a player or a mob?
		if (entity instanceof Player) {
			if (!CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
				player.sendMessage(MessageType.NO_PLAYER);
				hatRegistry.setRegister(uuid, String.class, null);
				return;
			}
			if (CommandUtil.hasPermission(entity, PermissionsType.IMMUNE)) {
				player.sendMessage(MessageType.IMMUNE);
				hatRegistry.setRegister(uuid, String.class, null);
				return;
			}
		} else {
			if (!CommandUtil.hasPermission(player, PermissionsType.MOB + "." + entity.getType().toString().toLowerCase())) {
				player.sendMessage(MessageType.NO_MOB);
				hatRegistry.setRegister(uuid, String.class, null);
				return;
			}
		}
		
		hatRegistry.setRegister(uuid, String.class, null);
		
		if (uuid.equals(hatUuid)) {
			mobRegistry.setRegister(uuid, String.class, entityUuid);
			entityUtil.removeAllPassengers(player);
			entityUtil.addPassenger(player, entity);
		} else {
			Player hatPlayer = CommandUtil.getPlayerByUuid(hatUuid);
			
			// Need to make sure they're online
			if (!hatPlayer.isOnline()) {
				player.sendMessage(MessageType.OFFLINE);
				return;
			}
			
			mobRegistry.setRegister(hatUuid, String.class, entityUuid);
			entityUtil.removeAllPassengers(hatPlayer);
			entityUtil.addPassenger(hatPlayer, entity);
		}
	}
}
