package me.egg82.hme.commands;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.ChatColor;
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
import ninja.egg82.plugin.reflection.player.IPlayerHelper;
import ninja.egg82.plugin.utils.CommandUtil;
import ninja.egg82.utils.ReflectUtil;
import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.HatRegistry;
import me.egg82.hme.services.MaterialRegistry;
import me.egg82.hme.services.GlowMaterialRegistry;
import me.egg82.hme.util.ILightHelper;

public class HatCommand extends PluginCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(GlowMaterialRegistry.class);
	private IRegistry materialRegistry = (IRegistry) ServiceLocator.getService(MaterialRegistry.class);
	private IRegistry hatRegistry = (IRegistry) ServiceLocator.getService(HatRegistry.class);
	
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	private IPlayerHelper playerUtil = (IPlayerHelper) ServiceLocator.getService(IPlayerHelper.class);
	
	//constructor
	@SuppressWarnings("deprecation")
	public HatCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		super(sender, command, label, args);
		
		Object[] enums = ReflectUtil.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			materialRegistry.setRegister(m.toString().toLowerCase(), Material.class, m);
			materialRegistry.setRegister(Integer.toString(m.getId()), Material.class, m);
		}
	}
	
	//public
	
	//private
	@SuppressWarnings("deprecation")
	protected void onExecute(long elapsedMilliseconds) {
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
		
		ItemStack hand = playerUtil.getItemInMainHand(player);
		if (hand == null || hand.getType() == Material.AIR || hand.getAmount() == 0) {
			// TODO Hand is empty. Mob/Player hat time!
			if (args.length == 0) {
				// Hat self, mob/player
				
				// Check if player can hat mobs or players
				if (!CommandUtil.hasPermission(player, PermissionsType.MOB) && !CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
					sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
					dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
					return;
				}
				
				hatRegistry.setRegister(player.getUniqueId().toString(), String.class, player.getUniqueId().toString());
				sender.sendMessage(ChatColor.YELLOW + "Right-click on any mob or player to give yourself a lovely hat!");
			} else {
				if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					ItemStack blockHat = new ItemStack((Material) materialRegistry.getRegister(args[0].toLowerCase()), 1);
					PlayerInventory inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().toString().toLowerCase())) {
						sender.sendMessage(MessageType.NO_PERMISSIONS_HAT);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS_HAT);
						return;
					}
					
					// Check if player can /give
					if (!CommandUtil.hasPermission(player, PermissionsType.GIVE)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(MessageType.NO_SPACE);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
						return;
					}
					
					hat(player.getUniqueId().toString(), player, inventory, blockHat);
				} else {
					// Hat others, mob/player
					
					// Check if player can hat mobs or players
					if (!CommandUtil.hasPermission(player, PermissionsType.MOB) && !CommandUtil.hasPermission(player, PermissionsType.PLAYER)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					// Check if player can hat others
					if (!CommandUtil.hasPermission(player, PermissionsType.OTHERS)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(SpigotMessageType.PLAYER_NOT_FOUND);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.PLAYER_NOT_FOUND);
						return;
					}
					
					// Check if player is immune
					if (CommandUtil.hasPermission(hatPlayer, PermissionsType.IMMUNE)) {
						sender.sendMessage(MessageType.IMMUNE);
						dispatch(CommandEvent.ERROR, CommandErrorType.IMMUNE);
						return;
					}
					
					hatRegistry.setRegister(player.getUniqueId().toString(), String.class, hatPlayer.getUniqueId().toString());
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
				if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().toString().toLowerCase())) {
					sender.sendMessage(MessageType.NO_PERMISSIONS_HAT);
					dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS_HAT);
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
					sender.sendMessage(MessageType.NO_SPACE);
					dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
					return;
				}
				
				hat(player.getUniqueId().toString(), player, inventory, blockHat);
			} else {
				if (args[0].equalsIgnoreCase("-a")) {
					// Hat entire stack
					blockHat = new ItemStack(hand);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().toString().toLowerCase())) {
						sender.sendMessage(MessageType.NO_PERMISSIONS_HAT);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS_HAT);
						return;
					}
					
					// Check if player can use -a
					if (!CommandUtil.hasPermission(player, PermissionsType.STACK)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					blockHat.setAmount(hand.getAmount());
					playerUtil.setItemInMainHand(player, null);
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(MessageType.NO_SPACE);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
						return;
					}
					
					hat(player.getUniqueId().toString(), player, inventory, blockHat);
				} else if (materialRegistry.hasRegister(args[0].toLowerCase())) {
					// Hat give
					blockHat = new ItemStack((Material) materialRegistry.getRegister(args[0].toLowerCase()), 1);
					inventory = player.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().toString().toLowerCase())) {
						sender.sendMessage(MessageType.NO_PERMISSIONS_HAT);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS_HAT);
						return;
					}
					
					// Check if player can /give
					if (!CommandUtil.hasPermission(player, PermissionsType.GIVE)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					// Remove old hat (if any)
					if (!removeHat(inventory)) {
						sender.sendMessage(MessageType.NO_SPACE);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
						return;
					}
					
					hat(player.getUniqueId().toString(), player, inventory, blockHat);
				} else {
					// Hat others
					
					// Check if player can hat others
					if (!CommandUtil.hasPermission(player, PermissionsType.OTHERS)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					// Check player exists
					Player hatPlayer = CommandUtil.getPlayerByName(args[0]);
					if (hatPlayer == null) {
						sender.sendMessage(SpigotMessageType.PLAYER_NOT_FOUND);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.PLAYER_NOT_FOUND);
						return;
					}
					
					// Check if player is immune
					if (CommandUtil.hasPermission(hatPlayer, PermissionsType.IMMUNE)) {
						sender.sendMessage(MessageType.IMMUNE);
						dispatch(CommandEvent.ERROR, CommandErrorType.IMMUNE);
						return;
					}
					
					blockHat = new ItemStack(hand);
					inventory = hatPlayer.getInventory();
					
					// Check if player can use this type of hat
					if (!CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getTypeId()) && !CommandUtil.hasPermission(player, PermissionsType.HAT + "." + blockHat.getType().toString().toLowerCase())) {
						sender.sendMessage(MessageType.NO_PERMISSIONS_HAT);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS_HAT);
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
						sender.sendMessage(MessageType.NO_SPACE);
						dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
						return;
					}
					
					hat(hatPlayer.getUniqueId().toString(), hatPlayer, inventory, blockHat);
				}
			}
		}
		
		dispatch(CommandEvent.COMPLETE, null);
	}
	
	protected void onUndo() {
		
	}
	
	private void hat(String uuid, Player player, PlayerInventory inventory, ItemStack helmet) {
		if (glowMaterialRegistry.hasRegister(helmet.getType().toString().toLowerCase())) {
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
				
				glowRegistry.setRegister(uuid, Player.class, player);
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
