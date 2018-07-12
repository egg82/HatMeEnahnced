package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.registries.HatRegistry;
import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.bukkit.reflection.entity.IEntityHelper;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class PlayerInteractEntityEventCommand extends EventHandler<PlayerInteractEntityEvent> {
	//vars
	private IRegistry<UUID, UUID> hatRegistry = ServiceLocator.getService(HatRegistry.class);
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public PlayerInteractEntityEventCommand() {
		super();
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
		
		if (!hatRegistry.hasRegister(uuid) || uuid.equals(entityUuid)) {
			return;
		}
		
		UUID hatUuid = hatRegistry.getRegister(uuid);
		
		// Need to make sure the entity isn't already a hat
		if (mobRegistry.hasValue(entityUuid)) {
			player.sendMessage(ChatColor.RED + "That mob/player is already someone else's hat!");
			hatRegistry.removeRegister(uuid);
			return;
		}
		
		// Is the target a player or a mob?
		if (entity instanceof Player) {
			if (!player.hasPermission(PermissionsType.PLAYER)) {
				player.sendMessage(ChatColor.RED + "You do not have permissions to use mob or player hats!");
				hatRegistry.removeRegister(uuid);
				return;
			}
			if (entity.hasPermission(PermissionsType.IMMUNE)) {
				player.sendMessage(ChatColor.RED + "Player is immune.");
				hatRegistry.removeRegister(uuid);
				return;
			}
		} else {
			if (!player.hasPermission(PermissionsType.MOB + "." + entity.getType().name().toLowerCase())) {
				player.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
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
				player.sendMessage(ChatColor.RED + "Player could not be found.");
				return;
			}
			
			mobRegistry.setRegister(hatUuid, entityUuid);
			entityUtil.removeAllPassengers(hatPlayer);
			entityUtil.addPassenger(hatPlayer, entity);
		}
	}
}
