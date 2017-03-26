package me.egg82.hme.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ninja.egg82.events.CommandEvent;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.PluginCommand;
import ninja.egg82.plugin.enums.SpigotCommandErrorType;
import ninja.egg82.plugin.enums.SpigotMessageType;
import ninja.egg82.plugin.reflection.player.IPlayerUtil;
import ninja.egg82.utils.ReflectUtil;
import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MaterialRegistry;
import me.egg82.hme.util.ILightHelper;

public class HatCommand extends PluginCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry materialRegistry = (IRegistry) ServiceLocator.getService(MaterialRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	private IPlayerUtil playerUtil = (IPlayerUtil) ServiceLocator.getService(IPlayerUtil.class);
	
	//constructor
	public HatCommand() {
		super();
	}
	
	//public
	
	//private
	@SuppressWarnings("deprecation")
	protected void onExecute(long elapsedMilliseconds) {
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
					
					Object[] enums = ReflectUtil.getStaticFields(Material.class);
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
		
		String uuid = player.getUniqueId().toString();
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
		
		if (materialRegistry.hasRegister(type.toString().toLowerCase())) {
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX());
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ());
				lightHelper.addLight(loc, true);
				
				glowRegistry.setRegister(uuid, Player.class, player);
			}
		}
		
		inv.setHelmet(stack);
		
		sender.sendMessage("What a lovely hat!");
		
		return true;
	}
}
