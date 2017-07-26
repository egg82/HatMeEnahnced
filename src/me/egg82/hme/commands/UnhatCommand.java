package me.egg82.hme.commands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.enums.LanguageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.exceptions.InventoryFullException;
import me.egg82.hme.exceptions.PlayerImmuneException;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MobRegistry;
import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.PluginCommand;
import ninja.egg82.plugin.enums.SpigotLanguageType;
import ninja.egg82.plugin.exceptions.IncorrectCommandUsageException;
import ninja.egg82.plugin.exceptions.InvalidPermissionsException;
import ninja.egg82.plugin.exceptions.PlayerNotFoundException;
import ninja.egg82.plugin.exceptions.SenderNotAllowedException;
import ninja.egg82.plugin.reflection.entity.IEntityHelper;
import ninja.egg82.plugin.utils.CommandUtil;
import ninja.egg82.plugin.utils.LanguageUtil;

public class UnhatCommand extends PluginCommand {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private IRegistry<UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public UnhatCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		if (!CommandUtil.hasPermission(sender, PermissionsType.HAT)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
			onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.HAT)));
			return;
		}
		if (!CommandUtil.isArrayOfAllowedLength(args, 0, 1)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INCORRECT_COMMAND_USAGE));
			sender.getServer().dispatchCommand(sender, "help unhat");
			onError().invoke(this, new ExceptionEventArgs<IncorrectCommandUsageException>(new IncorrectCommandUsageException(sender, this, args)));
			return;
		}
		if (!CommandUtil.isPlayer(sender)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.SENDER_NOT_ALLOWED));
			onError().invoke(this, new ExceptionEventArgs<SenderNotAllowedException>(new SenderNotAllowedException(sender, this)));
			return;
		}
		
		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		
		if (args.length == 0) {
			if (!removeBlockHat(player.getInventory())) {
				sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
				onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(player.getInventory(), player.getInventory().getHelmet())));
				return;
			}
			
			unhat(uuid, player);
		} else {
			if (!CommandUtil.hasPermission(sender, PermissionsType.OTHERS)) {
				sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
				onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.OTHERS)));
				return;
			}
			
			Player other = CommandUtil.getPlayerByName(args[0]);
			// Check if player exists
			if (other == null) {
				sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.PLAYER_NOT_FOUND));
				onError().invoke(this, new ExceptionEventArgs<PlayerNotFoundException>(new PlayerNotFoundException(args[0])));
				return;
			}
			// Is player immune?
			if (CommandUtil.hasPermission(other, PermissionsType.IMMUNE)) {
				sender.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_IMMUNE));
				onError().invoke(this, new ExceptionEventArgs<PlayerImmuneException>(new PlayerImmuneException(other)));
				return;
			}
			UUID otherUuid = other.getUniqueId();
			
			if (!removeBlockHat(other.getInventory())) {
				sender.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_IMMUNE));
				onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(other.getInventory(), other.getInventory().getHelmet())));
				return;
			}
			
			unhat(otherUuid, other);
		}
		
		onComplete().invoke(this, CompleteEventArgs.EMPTY);
	}
	
	protected void onUndo() {
		
	}
	
	private void unhat(UUID uuid, Player player) {
		entityUtil.removeAllPassengers(player);
		mobRegistry.removeRegister(uuid);
		
		if (glowRegistry.hasRegister(uuid)) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
			
			glowRegistry.removeRegister(uuid);
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
				if (amount - helmet.getAmount() <= helmet.getMaxStackSize()) {
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
