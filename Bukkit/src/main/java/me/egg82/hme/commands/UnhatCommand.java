package me.egg82.hme.commands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.registries.MobRegistry;
import ninja.egg82.bukkit.reflection.entity.IEntityHelper;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.CommandHandler;

public class UnhatCommand extends CommandHandler {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private IRegistry<UUID, UUID> mobRegistry = ServiceLocator.getService(MobRegistry.class);
	
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	private IEntityHelper entityUtil = ServiceLocator.getService(IEntityHelper.class);
	
	//constructor
	public UnhatCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		if (!CommandUtil.isArrayOfAllowedLength(args, 0, 1)) {
			sender.sendMessage(ChatColor.RED + "Incorrect command usage!");
			String name = getClass().getSimpleName();
			name = name.substring(0, name.length() - 7).toLowerCase();
			Bukkit.getServer().dispatchCommand((CommandSender) sender.getHandle(), "? " + name);
			return;
		}
		
		if (args.length == 0) {
			if (sender.isConsole()) {
				sender.sendMessage(ChatColor.RED + "Console cannot run this command without the [player] argument!");
				return;
			}
			
			Player player = (Player) sender.getHandle();
			
			if (!removeHat(player.getInventory())) {
				sender.sendMessage(ChatColor.RED + "You don't have any space left in your inventory to remove your old hat!");
				return;
			}
			
			unhat(player);
		} else {
			if (!sender.hasPermission(PermissionsType.OTHERS)) {
				sender.sendMessage(ChatColor.RED + "You do not have permissions to /unhat other players!");
				return;
			}
			
			Player other = CommandUtil.getPlayerByName(args[0]);
			// Check if player exists
			if (other == null) {
				sender.sendMessage(ChatColor.RED + "Player could not be found.");
				return;
			}
			// Is player immune?
			if (other.hasPermission(PermissionsType.IMMUNE)) {
				sender.sendMessage(ChatColor.RED + "Player is immune.");
				return;
			}
			
			if (!removeHat(other.getInventory())) {
				sender.sendMessage(ChatColor.RED + "That player doesn't have any space left in their inventory to remove their old hat!");
				return;
			}
			
			unhat(other);
		}
	}
	
	protected void onUndo() {
		
	}
	
	private void unhat(Player player) {
		mobRegistry.removeRegister(player.getUniqueId());
		entityUtil.removeAllPassengers(player);
		
		if (glowSet.remove(player.getUniqueId())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
		}
		
		player.sendMessage(ChatColor.GREEN + "No more hat :(");
		if (!player.getUniqueId().equals(sender.getUuid())) {
			sender.sendMessage(ChatColor.GREEN + "No more hat :(");
		}
	}
	
	private boolean removeHat(PlayerInventory inventory) {
		ItemStack helmet = inventory.getHelmet();
		
		if (helmet == null || helmet.getType() == Material.AIR || helmet.getAmount() == 0) {
			return true;
		}
		
		Map<Integer, ItemStack> dropped = inventory.addItem(helmet);
		if (dropped.size() > 0) {
			return false;
		}
		
		inventory.setHelmet(null);
		return true;
	}
}
