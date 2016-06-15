package me.egg82.hme.commands.base;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.egg82.hme.enums.CommandErrorType;
import me.egg82.hme.enums.MessageType;
import ninja.egg82.events.patterns.command.CommandEvent;
import ninja.egg82.plugin.commands.PluginCommand;

public class BasePluginCommand extends PluginCommand {
	//vars
	
	//constructor
	public BasePluginCommand(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	//public
	public void onQuit(String name, Player player) {
		
	}
	public void onDeath(String name, Player player) {
		
	}
	public void onLogin(String name, Player player) {
		
	}
	
	//private
	protected boolean isValid(boolean needsPlayer, String permissions, int[] argsLengths) {
		if (needsPlayer && !(sender instanceof Player)) {
			sender.sendMessage(MessageType.CONSOLE_NOT_ALLOWED);
			dispatch(CommandEvent.ERROR, CommandErrorType.CONSOLE_NOT_ALLOWED);
			return false;
		}
		if (sender instanceof Player && !permissionsManager.playerHasPermission((Player) sender, permissions)) {
			sender.sendMessage(MessageType.NO_PERMISSIONS);
			dispatch(CommandEvent.ERROR, CommandErrorType.NO_PERMISSIONS);
			return false;
		}
		
		if (!ArrayUtils.contains(argsLengths, args.length)) {
			sender.sendMessage(MessageType.INCORRECT_USAGE);
			sender.getServer().dispatchCommand(sender, "help " + command.getName());
			dispatch(CommandEvent.ERROR, CommandErrorType.INCORRECT_USAGE);
			return false;
		}
		
		return true;
	}
}
