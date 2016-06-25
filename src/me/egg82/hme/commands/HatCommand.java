package me.egg82.hme.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ninja.egg82.events.patterns.command.CommandEvent;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.PluginCommand;
import ninja.egg82.plugin.enums.SpigotCommandErrorType;
import ninja.egg82.plugin.enums.SpigotMessageType;
import ninja.egg82.plugin.enums.SpigotReflectType;
import ninja.egg82.plugin.enums.SpigotServiceType;
import ninja.egg82.plugin.reflection.player.interfaces.IPlayerUtil;
import ninja.egg82.registry.interfaces.IRegistry;
import ninja.egg82.utils.Util;
import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.LightHelper;

public class HatCommand extends PluginCommand {
	//vars
	IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	private IPlayerUtil playerUtil = (IPlayerUtil) ((IRegistry) ServiceLocator.getService(SpigotServiceType.REFLECT_REGISTRY)).getRegister(SpigotReflectType.PLAYER);
	
	//constructor
	public HatCommand() {
		super();
	}
	
	//public
	
	//private
	@SuppressWarnings("deprecation")
	protected void execute() {
		if (isValid(true, PermissionsType.HAT, new int[]{0,1}, null)) {
			if (args.length == 0) {
				ItemStack hand = playerUtil.getItemInMainHand((Player) sender);
				ItemStack hand2 = new ItemStack(hand);
				hand2.setAmount(1);
				if (hat((Player) sender, hand2)) {
					if (hand.getAmount() == 1) {
						playerUtil.setItemInMainHand((Player) sender, null);
					} else {
						hand.setAmount(hand.getAmount() - 1);
					}
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("-a")) {
					if (!permissionsManager.playerHasPermission((Player) sender, PermissionsType.STACK)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					if (hat((Player) sender, ((Player) sender).getInventory().getItemInMainHand())) {
						((Player) sender).getInventory().setItemInMainHand(null);
					}
				} else {
					if (!permissionsManager.playerHasPermission((Player) sender, PermissionsType.GIVE)) {
						sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
						dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
						return;
					}
					
					Object[] enums = Util.getStaticFields(Material.class);
					Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
					boolean found = false;
					for (Material m : materials) {
						if (m.toString().equalsIgnoreCase(args[0])) {
							hat((Player) sender, new ItemStack(m, 1));
							found = true;
							break;
						}
					}
					
					if (!found) {
						try {
							hat((Player) sender, new ItemStack(Material.getMaterial(Integer.parseInt(args[0])), 1));
						} catch (Exception ex) {
							sender.sendMessage(SpigotMessageType.INCORRECT_USAGE);
							sender.getServer().dispatchCommand(sender, "help " + command.getName());
							dispatch(CommandEvent.ERROR, SpigotCommandErrorType.INCORRECT_USAGE);
							return;
						}
					}
				}
			}
			
			dispatch(CommandEvent.COMPLETE, null);
		}
	}
	@SuppressWarnings("deprecation")
	private boolean hat(Player player, ItemStack stack) {
		if (!permissionsManager.playerHasPermission((Player) sender, PermissionsType.HAT + "." + stack.getTypeId()) && !permissionsManager.playerHasPermission((Player) sender, PermissionsType.HAT + "." + stack.getType().toString().toLowerCase()) && !permissionsManager.playerHasPermission(player, PermissionsType.ANY)) {
			sender.sendMessage(SpigotMessageType.NO_PERMISSIONS);
			dispatch(CommandEvent.ERROR, SpigotCommandErrorType.NO_PERMISSIONS);
			return false;
		}
		
		String lowerName = player.getName().toLowerCase();
		PlayerInventory inv = player.getInventory();
		Material type = stack.getType();
		
		if (type == Material.AIR) {
			sender.sendMessage(MessageType.NO_AIR);
			dispatch(CommandEvent.ERROR, CommandErrorType.NO_AIR);
			return false;
		}
		if (inv.getHelmet() != null) {
			int empty = inv.firstEmpty();
			if (empty == -1) {
				sender.sendMessage(MessageType.NO_SPACE);
				dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
				return false;
			}
			ItemStack head = inv.getHelmet();
			inv.setItem(empty, head);
			inv.setHelmet(null);
		}
		
		if (
				type == Material.TORCH ||
				type == Material.LAVA ||
				type == Material.STATIONARY_LAVA ||
				type == Material.LAVA_BUCKET ||
				type == Material.FIRE ||
				type == Material.FIREBALL ||
				type == Material.GLOWSTONE ||
				type == Material.GLOWSTONE_DUST ||
				type == Material.BURNING_FURNACE ||
				type == Material.REDSTONE_TORCH_ON ||
				type == Material.JACK_O_LANTERN ||
				type == Material.REDSTONE_LAMP_ON ||
				type == Material.BEACON ||
				type == Material.REDSTONE_BLOCK ||
				type == Material.SEA_LANTERN
				) {
			glowRegistry.setRegister(lowerName, player);
			
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX());
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ());
			LightHelper.addLight(loc, true);
		}
		
		inv.setHelmet(stack);
		
		sender.sendMessage("What a lovely hat!");
		
		return true;
	}
}
