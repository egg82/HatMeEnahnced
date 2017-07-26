package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.egg82.hme.enums.LanguageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.HatRegistry;
import me.egg82.hme.services.MobRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;
import ninja.egg82.plugin.utils.CommandUtil;
import ninja.egg82.plugin.utils.LanguageUtil;

public class PlayerInteractEntityEventCommand extends EventCommand<PlayerInteractEntityEvent> {
	//vars
	private IRegistry<UUID> hatRegistry = ServiceLocator.getService(HatRegistry.class);
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public PlayerInteractEntityEventCommand(PlayerInteractEntityEvent event) {
		super(event);
	}
	
	//public

	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		UUID entityUuid = entity.getUniqueId();
		UUID uuid = player.getUniqueId();
		
		if (!hatRegistry.hasRegister(uuid)) {
			return;
		}
		
		UUID hatUuid = hatRegistry.getRegister(uuid, UUID.class);
		
		// Need to make sure the entity isn't already a hat
		if (mobRegistry.hasValue(entityUuid)) {
			player.sendMessage(LanguageUtil.getString(LanguageType.MOB_OWNED));
			hatRegistry.removeRegister(uuid);
			return;
		}
		
		// Is the target a player or a mob?
		if (entity instanceof Player) {
			if (!CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
				player.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
				hatRegistry.removeRegister(uuid);
				return;
			}
			if (CommandUtil.hasPermission(entity, PermissionsType.IMMUNE)) {
				player.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_IMMUNE));
				hatRegistry.removeRegister(uuid);
				return;
			}
		} else {
			if (!CommandUtil.hasPermission(player, PermissionsType.MOB + "." + entity.getType().name().toLowerCase())) {
				player.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
				hatRegistry.removeRegister(uuid);
				return;
			}
		}
		
		hatRegistry.removeRegister(uuid);
		
		if (uuid.equals(hatUuid)) {
			mobRegistry.setRegister(uuid, entityUuid);
			entityUtil.removeAllPassengers(player);
			entityUtil.addPassenger(player, entity);
		} else {
			Player hatPlayer = CommandUtil.getPlayerByUuid(hatUuid);
			
			// Need to make sure they're online
			if (hatPlayer == null) {
				player.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_OFFLINE));
				return;
			}
			
			mobRegistry.setRegister(hatUuid, entityUuid);
			entityUtil.removeAllPassengers(hatPlayer);
			entityUtil.addPassenger(hatPlayer, entity);
		}
	}
}
