package me.egg82.hme.commands;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ninja.egg82.events.CommandEvent;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.PluginCommand;
import ninja.egg82.plugin.enums.SpigotCommandErrorType;
import ninja.egg82.plugin.enums.SpigotMessageType;
import ninja.egg82.plugin.reflection.entity.IEntityUtil;
import ninja.egg82.plugin.utils.CommandUtil;
import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MobRegistry;
import me.egg82.hme.util.ILightHelper;

public class UnhatCommand extends PluginCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry mobRegistry = (IRegistry) ServiceLocator.getService(MobRegistry.class);
	
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	private IEntityUtil entityUtil = (IEntityUtil) ServiceLocator.getService(IEntityUtil.class);
	
	//constructor
	public UnhatCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		if (!CommandUtil.hasPermission(sender, PermissionsType.HAT)) {
			sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
			dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
			return;
		}
		if (!CommandUtil.isArrayOfAllowedLength(args, 0, 1)) {
			sender.sendMessage(SpigotMessageType.INCORRECT_USAGE);
			sender.getServer().dispatchCommand(sender, "help " + command.getName());
			dispatch(CommandEvent.ERROR, SpigotCommandErrorType.INCORRECT_USAGE);
			return;
		}
		if (!CommandUtil.isPlayer(sender)) {
			sender.sendMessage(SpigotMessageType.CONSOLE_NOT_ALLOWED);
			dispatch(CommandEvent.ERROR, SpigotCommandErrorType.CONSOLE_NOT_ALLOWED);
			return;
		}
		
		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString();
		
		if (args.length == 0) {
			if (!removeBlockHat(player.getInventory())) {
				sender.sendMessage(MessageType.NO_SPACE);
				dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
				return;
			}
			
			unhat(uuid, player);
		} else {
			if (!CommandUtil.hasPermission(sender, PermissionsType.OTHERS)) {
				sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
				dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
				return;
			}
			
			Player other = CommandUtil.getPlayerByName(args[0]);
			// Check if player exists
			if (other == null) {
				sender.sendMessage(SpigotMessageType.PLAYER_NOT_FOUND);
				dispatch(CommandEvent.ERROR, SpigotCommandErrorType.PLAYER_NOT_FOUND);
				return;
			}
			// Is player immune?
			if (CommandUtil.hasPermission(other, PermissionsType.IMMUNE)) {
				sender.sendMessage(MessageType.IMMUNE);
				dispatch(CommandEvent.ERROR, CommandErrorType.IMMUNE);
				return;
			}
			String otherUuid = other.getUniqueId().toString();
			
			if (!removeBlockHat(other.getInventory())) {
				sender.sendMessage(MessageType.NO_SPACE);
				dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
				return;
			}
			
			unhat(otherUuid, other);
		}
		
		dispatch(CommandEvent.COMPLETE, null);
	}
	private void unhat(String uuid, Player player) {
		entityUtil.removeAllPassengers(player);
		mobRegistry.setRegister(uuid, String.class, null);
		
		if (glowRegistry.hasRegister(uuid)) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
			
			glowRegistry.setRegister(uuid, Player.class, null);
		}
		
		sender.sendMessage("No more hat :(");
	}
	
	private boolean removeBlockHat(PlayerInventory inventory) {
		ItemStack helmet = inventory.getHelmet();
		
		if (helmet == null || helmet.getType() == Material.AIR || helmet.getAmount() == 0) {
			return true;
		}
		
		int slot = -1;
		if (helmet.getDurability() == 0) {
			inventory.setHelmet(null);
			Map<Integer, ? extends ItemStack> slots = inventory.all(helmet.getType());
			
			for (Map.Entry<Integer, ? extends ItemStack> entry : slots.entrySet()) {
				int amount = entry.getValue().getAmount();
				if (amount - helmet.getAmount() <= 64) {
					helmet.setAmount(helmet.getAmount() + amount);
					slot = entry.getKey();
					break;
				}
			}
			
			if (slot == -1) {
				slot = inventory.firstEmpty();
			}
		} else {
			slot = inventory.firstEmpty();
		}
		
		if (slot == -1) {
			return false;
		}
		
		inventory.setHelmet(null);
		inventory.setItem(slot, helmet);
		return true;
	}
}
