package me.egg82.hme.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ninja.egg82.events.patterns.command.CommandEvent;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.PluginCommand;
import ninja.egg82.registry.interfaces.IRegistry;

import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.LightHelper;

public class UnhatCommand extends PluginCommand {
	//vars
	IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	
	//constructor
	public UnhatCommand(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	//public
	
	//private
	protected void execute() {
		if (!(sender instanceof Player)) {
			sender.sendMessage(MessageType.CONSOLE_NOT_ALLOWED);
			dispatch(CommandEvent.ERROR, CommandErrorType.CONSOLE_NOT_ALLOWED);
			return;
		}
		if (!permissionsManager.playerHasPermission((Player) sender, PermissionsType.HAT)) {
			sender.sendMessage(MessageType.NO_PERMISSIONS);
			dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS);
			return;
		}
		
		if (args.length == 0) {
			unhat((Player) sender);
		} else {
			sender.sendMessage(MessageType.INCORRECT_USAGE);
			sender.getServer().dispatchCommand(sender, "help " + command.getName());
			dispatch(CommandEvent.ERROR, CommandErrorType.INCORRECT_USAGE);
		}
	}
	private void unhat(Player player) {
		PlayerInventory inv = player.getInventory();
		
		if (inv.getHelmet() != null) {
			int empty = inv.firstEmpty();
			if (empty == -1) {
				sender.sendMessage(MessageType.NO_SPACE);
				dispatch(CommandEvent.ERROR, CommandErrorType.NO_SPACE);
				return;
			}
			ItemStack head = inv.getHelmet();
			inv.setItem(empty, head);
			inv.setHelmet(null);
		}
		
		sender.sendMessage("No more hat :(");
		
		if (glowRegistry.contains(player.getName().toLowerCase())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX());
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ());
			LightHelper.removeLight(loc, true);
		}
		
		glowRegistry.setRegister(player.getName().toLowerCase(), null);
	}
}
