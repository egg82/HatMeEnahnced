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
import me.egg82.hme.lists.GlowMaterialSet;
import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.registries.HatRegistry;
import me.egg82.hme.registries.MaterialRegistry;
import ninja.egg82.bukkit.reflection.player.IPlayerHelper;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IRegistry;
import ninja.egg82.plugin.handlers.CommandHandler;

public class HatCommand extends CommandHandler {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private IConcurrentSet<Material> glowMaterialSet = ServiceLocator.getService(GlowMaterialSet.class);
	private IRegistry<UUID, UUID> hatRegistry = ServiceLocator.getService(HatRegistry.class);
	private IRegistry<String, Material> materialRegistry = ServiceLocator.getService(MaterialRegistry.class);
	
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	private IPlayerHelper playerUtil = ServiceLocator.getService(IPlayerHelper.class);
	
	//constructor
	public HatCommand() {
		super();
	}
	
	//public
	
	//private
	@SuppressWarnings("deprecation")
	protected void onExecute(long elapsedMilliseconds) {
		if (!CommandUtil.isArrayOfAllowedLength(args, 0, 1)) {
			sender.sendMessage(ChatColor.RED + "Incorrect command usage!");
			String name = getClass().getSimpleName();
			name = name.substring(0, name.length() - 7).toLowerCase();
			Bukkit.getServer().dispatchCommand((CommandSender) sender.getHandle(), "? " + name);
			return;
		}
		if (sender.isConsole()) {
			sender.sendMessage(ChatColor.RED + "Console cannot run this command!");
			return;
		}
		
		Player player = (Player) sender.getHandle();
		
		ItemStack hand = playerUtil.getItemInMainHand(player);
		if (hand == null || hand.getType() == Material.AIR || hand.getAmount() == 0) {
			if (args.length == 0) {
				// Hat self, mob/player
				
				// Check if player can hat mobs or players
				if (!sender.hasPermission(PermissionsType.MOB) && !sender.hasPermission(PermissionsType.PLAYER)) {
					sender.sendMessage(ChatColor.RED + "You do not have permissions to use mob or player hats!");
					return;
				}
				
				hatRegistry.setRegister(sender.getUuid(), sender.getUuid());
				sender.sendMessage(ChatColor.YELLOW + "Right-click on any mob or player to give yourself a lovely hat!");
			} else {
				if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					ItemStack blockHat = new ItemStack(materialRegistry.getRegister(args[0].toLowerCase()), 1);
					PlayerInventory inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!sender.hasPermission("hme.hat." + blockHat.getType().getId()) && !sender.hasPermission("hme.hat." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
						return;
					}
					
					// Check if player can /give
					if (!sender.hasPermission(PermissionsType.GIVE)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to give yourself hats you do not have in your hand!");
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(ChatColor.RED + "You don't have any space left in your inventory to remove your old hat!");
						return;
					}
					
					hat(player, inventory, blockHat);
				} else {
					// Hat others, mob/player
					
					// Check if player can hat mobs or players
					if (!sender.hasPermission(PermissionsType.MOB) && !sender.hasPermission(PermissionsType.PLAYER)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use mob or player hats!");
						return;
					}
					
					// Check if player can hat others
					if (!sender.hasPermission(PermissionsType.OTHERS)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to /hat other players!");
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(ChatColor.RED + "Player could not be found.");
						return;
					}
					
					// Check if player is immune
					if (hatPlayer.hasPermission(PermissionsType.IMMUNE)) {
						sender.sendMessage(ChatColor.RED + "Player is immune.");
						return;
					}
					
					hatRegistry.setRegister(sender.getUuid(), hatPlayer.getUniqueId());
					sender.sendMessage(ChatColor.YELLOW + "Right-click on any mob or player to give " + hatPlayer.getName() + " a lovely hat!");
				}
			}
		} else {
			// Hand has an item. Block/Item hats!
			ItemStack blockHat = null;
			PlayerInventory inventory = null;
			
			if (args.length == 0) {
				// Hat self, no additional args
				blockHat = new ItemStack(hand);
				inventory = player.getInventory();
				
				// Check if player can use this type of hat
				if (!sender.hasPermission("hme.hat." + blockHat.getType().getId()) && !sender.hasPermission("hme.hat." + blockHat.getType().name().toLowerCase())) {
					sender.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
					return;
				}
				
				// Get/Set correct stack amounts
				blockHat.setAmount(1);
				if (hand.getAmount() == 1) {
					playerUtil.setItemInMainHand(player, null);
				} else {
					hand.setAmount(hand.getAmount() - 1);
				}
				
				// Remove old hat (if any)
				if (!removeHat(inventory)) {
					sender.sendMessage(ChatColor.RED + "You don't have any space left in your inventory to remove your old hat!");
					return;
				}
				
				hat(player, inventory, blockHat);
			} else {
				if (args[0].equalsIgnoreCase("-a")) {
					// Hat entire stack
					blockHat = new ItemStack(hand);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!sender.hasPermission("hme.hat." + blockHat.getType().getId()) && !sender.hasPermission("hme.hat." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
						return;
					}
					
					// Check if player can use -a
					if (!sender.hasPermission(PermissionsType.STACK)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use stacked hats!");
						return;
					}
					
					blockHat.setAmount(hand.getAmount());
					playerUtil.setItemInMainHand(player, null);
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(ChatColor.RED + "You don't have any space left in your inventory to remove your old hat!");
						return;
					}
					
					hat(player, inventory, blockHat);
				} else if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					blockHat = new ItemStack(materialRegistry.getRegister(args[0].toLowerCase()), 1);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!sender.hasPermission("hme.hat." + blockHat.getType().getId()) && !sender.hasPermission("hme.hat." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
						return;
					}
					
					// Check if player can /give
					if (!sender.hasPermission(PermissionsType.GIVE)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to give yourself hats you do not have in your hand!");
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(ChatColor.RED + "You don't have any space left in your inventory to remove your old hat!");
						return;
					}
					
					hat(player, inventory, blockHat);
				} else {
					// Hat others
					
					// Check if player can hat others
					if (!sender.hasPermission(PermissionsType.OTHERS)) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to /hat other players!");
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(ChatColor.RED + "Player could not be found.");
						return;
					}
					
					// Check if player is immune
					if (hatPlayer.hasPermission(PermissionsType.IMMUNE)) {
						sender.sendMessage(ChatColor.RED + "Player is immune.");
						return;
					}
					
					blockHat = new ItemStack(hand);
					inventory = hatPlayer.getInventory();
					
					// Check if player can use this type of hat
					if (!sender.hasPermission("hme.hat." + blockHat.getType().getId()) && !sender.hasPermission("hme.hat." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to use that type of hat!");
						return;
					}
					
					// Get/Set correct stack amounts
					blockHat.setAmount(1);
					if (hand.getAmount() == 1) {
						playerUtil.setItemInMainHand(player, null);
					} else {
						hand.setAmount(hand.getAmount() - 1);
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(ChatColor.RED + "That player doesn't have any space left in their inventory to remove their old hat!");
						return;
					}
					
					hat(hatPlayer, inventory, blockHat);
				}
			}
		}
	}
	
	protected void onUndo() {
		
	}
	
	private void hat(Player player, PlayerInventory inventory, ItemStack helmet) {
		if (glowMaterialSet.contains(helmet.getType())) {
			if (glowSet.add(player.getUniqueId())) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
			}
		}
		
		inventory.setHelmet(helmet);
		
		player.sendMessage(ChatColor.GREEN + "What a lovely hat!");
		if (!player.getUniqueId().equals(sender.getUuid())) {
			sender.sendMessage(ChatColor.GREEN + "What a lovely hat!");
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
