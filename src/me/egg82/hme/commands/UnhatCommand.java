package me.egg82.hme.commands;

import org.bukkit.Location;
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
	public UnhatCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
		if (isValid(true, PermissionsType.HAT, new int[]{0}, null)) {
			unhat((Player) sender);
		}
	}
	private void unhat(Player player) {
		String lowerName = player.getName().toLowerCase();
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
		
		glowRegistry.computeIfPresent(lowerName, (k,v) -> {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX());
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ());
			LightHelper.removeLight(loc, true);
			
			return null;
		});
	}
}
