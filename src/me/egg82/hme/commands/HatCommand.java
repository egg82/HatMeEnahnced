package me.egg82.hme.commands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.enums.LanguageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.exceptions.InvalidPermissionsHatTypeException;
import me.egg82.hme.exceptions.InventoryFullException;
import me.egg82.hme.exceptions.PlayerImmuneException;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowMaterialRegistry;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.HatRegistry;
import me.egg82.hme.services.MaterialRegistry;
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
import ninja.egg82.plugin.reflection.player.IPlayerHelper;
import ninja.egg82.plugin.utils.CommandUtil;
import ninja.egg82.plugin.utils.LanguageUtil;

public class HatCommand extends PluginCommand {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private IRegistry<String> glowMaterialRegistry = ServiceLocator.getService(GlowMaterialRegistry.class);
	private IRegistry<String> materialRegistry = ServiceLocator.getService(MaterialRegistry.class);
	private IRegistry<UUID> hatRegistry = ServiceLocator.getService(HatRegistry.class);
	
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
		if (!CommandUtil.hasPermission(sender, PermissionsType.HAT)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
			onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.HAT)));
			return;
		}
		if (!CommandUtil.isArrayOfAllowedLength(args, 0, 1)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INCORRECT_COMMAND_USAGE));
			sender.getServer().dispatchCommand(sender, "help hat");
			onError().invoke(this, new ExceptionEventArgs<IncorrectCommandUsageException>(new IncorrectCommandUsageException(sender, this, args)));
			return;
		}
		if (!CommandUtil.isPlayer(sender)) {
			sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.SENDER_NOT_ALLOWED));
			onError().invoke(this, new ExceptionEventArgs<SenderNotAllowedException>(new SenderNotAllowedException(sender, this)));
			return;
		}
		
		Player player = (Player) sender;
		
		ItemStack hand = playerUtil.getItemInMainHand(player);
		if (hand == null || hand.getType() == Material.AIR || hand.getAmount() == 0) {
			if (args.length == 0) {
				// Hat self, mob/player
				
				// Check if player can hat mobs or players
				if (!CommandUtil.hasPermission(player, PermissionsType.MOB) && !CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
					sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
					onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.HAT)));
					return;
				}
				
				hatRegistry.setRegister(player.getUniqueId(), player.getUniqueId());
				sender.sendMessage(ChatColor.YELLOW + "Right-click on any mob or player to give yourself a lovely hat!");
			} else {
				if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					ItemStack blockHat = new ItemStack((Material) materialRegistry.getRegister(args[0].toLowerCase()), 1);
					PlayerInventory inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsHatTypeException>(new InvalidPermissionsHatTypeException(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())));
						return;
					}
					
					// Check if player can /give
					if (!CommandUtil.hasPermission(player, PermissionsType.GIVE)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.GIVE)));
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
						onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(inventory, inventory.getHelmet())));
						return;
					}
					
					hat(player.getUniqueId(), player, inventory, blockHat);
				} else {
					// Hat others, mob/player
					
					// Check if player can hat mobs or players
					if (!CommandUtil.hasPermission(player, PermissionsType.MOB) && !CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.MOB)));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.PLAYER)));
						return;
					}
					
					// Check if player can hat others
					if (!CommandUtil.hasPermission(player, PermissionsType.OTHERS)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.OTHERS)));
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.PLAYER_NOT_FOUND));
						onError().invoke(this, new ExceptionEventArgs<PlayerNotFoundException>(new PlayerNotFoundException(args[0])));
						return;
					}
					
					// Check if player is immune
					if (CommandUtil.hasPermission(hatPlayer, PermissionsType.IMMUNE)) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_IMMUNE));
						onError().invoke(this, new ExceptionEventArgs<PlayerImmuneException>(new PlayerImmuneException(hatPlayer)));
						return;
					}
					
					hatRegistry.setRegister(player.getUniqueId(), hatPlayer.getUniqueId());
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
				if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())) {
					sender.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
					onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsHatTypeException>(new InvalidPermissionsHatTypeException(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())));
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
					sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
					onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(inventory, inventory.getHelmet())));
					return;
				}
				
				hat(player.getUniqueId(), player, inventory, blockHat);
			} else {
				if (args[0].equalsIgnoreCase("-a")) {
					// Hat entire stack
					blockHat = new ItemStack(hand);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsHatTypeException>(new InvalidPermissionsHatTypeException(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())));
						return;
					}
					
					// Check if player can use -a
					if (!CommandUtil.hasPermission(player, PermissionsType.STACK)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.STACK)));
						return;
					}
					
					blockHat.setAmount(hand.getAmount());
					playerUtil.setItemInMainHand(player, null);
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
						onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(inventory, inventory.getHelmet())));
						return;
					}
					
					hat(player.getUniqueId(), player, inventory, blockHat);
				} else if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					blockHat = new ItemStack((Material) materialRegistry.getRegister(args[0].toLowerCase()), 1);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsHatTypeException>(new InvalidPermissionsHatTypeException(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())));
						return;
					}
					
					// Check if player can /give
					if (!CommandUtil.hasPermission(player, PermissionsType.GIVE)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.GIVE)));
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
						onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(inventory, inventory.getHelmet())));
						return;
					}
					
					hat(player.getUniqueId(), player, inventory, blockHat);
				} else {
					// Hat others
					
					// Check if player can hat others
					if (!CommandUtil.hasPermission(player, PermissionsType.OTHERS)) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.INVALID_PERMISSIONS));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsException>(new InvalidPermissionsException(sender, PermissionsType.OTHERS)));
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(LanguageUtil.getString(SpigotLanguageType.PLAYER_NOT_FOUND));
						onError().invoke(this, new ExceptionEventArgs<PlayerNotFoundException>(new PlayerNotFoundException(args[0])));
						return;
					}
					
					// Check if player is immune
					if (CommandUtil.hasPermission(hatPlayer, PermissionsType.IMMUNE)) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.PLAYER_IMMUNE));
						onError().invoke(this, new ExceptionEventArgs<PlayerImmuneException>(new PlayerImmuneException(hatPlayer)));
						return;
					}
					
					blockHat = new ItemStack(hand);
					inventory = hatPlayer.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())) {
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVALID_PERMISSIONS_HAT_TYPE));
						onError().invoke(this, new ExceptionEventArgs<InvalidPermissionsHatTypeException>(new InvalidPermissionsHatTypeException(player, PermissionsType.HAT + "." + blockHat.getType().name().toLowerCase())));
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
						sender.sendMessage(LanguageUtil.getString(LanguageType.INVENTORY_FULL));
						onError().invoke(this, new ExceptionEventArgs<InventoryFullException>(new InventoryFullException(inventory, inventory.getHelmet())));
						return;
					}
					
					hat(hatPlayer.getUniqueId(), hatPlayer, inventory, blockHat);
				}
			}
		}
		
		onComplete().invoke(this, CompleteEventArgs.EMPTY);
	}
	
	protected void onUndo() {
		
	}
	
	private void hat(UUID uuid, Player player, PlayerInventory inventory, ItemStack helmet) {
		if (glowMaterialRegistry.hasRegister(helmet.getType().name())) {
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
				
				glowRegistry.setRegister(uuid, null);
			}
		}
		
		inventory.setHelmet(helmet);
		
		player.sendMessage("What a lovely hat!");
		if (player != sender) {
			sender.sendMessage("What a lovely hat!");
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
