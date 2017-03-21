package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.interfaces.ILightHelper;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

public class PlayerJoinEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_MATERIAL_REGISTRY);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(PluginServiceType.LIGHT_HELPER);
	
	//constructor
	public PlayerJoinEventCommand() {
		
	}
	
	//public
	
	//private
	protected void execute() {
		PlayerJoinEvent e = (PlayerJoinEvent) event;
		
		Player player = e.getPlayer();
		PlayerInventory inv = player.getInventory();
		
		if (inv == null) {
			return;
		}
		
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null) {
			return;
		}
		
		if (glowMaterialRegistry.contains(helmet.getType().toString().toLowerCase())) {
			glowRegistry.computeIfAbsent(player.getUniqueId().toString(), (k) -> {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX());
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ());
				lightHelper.addLight(loc, true);
				
				return player;
			});
		}
	}
}
