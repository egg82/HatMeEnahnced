package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MaterialRegistry;
import me.egg82.hme.util.ILightHelper;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class PlayerJoinEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(MaterialRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerJoinEventCommand() {
		
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
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
		
		if (glowMaterialRegistry.hasRegister(helmet.getType().toString().toLowerCase())) {
			String uuid = player.getUniqueId().toString();
			
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX());
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ());
				lightHelper.addLight(loc, true);
				
				glowRegistry.setRegister(uuid, Player.class, player);
			}
		}
	}
}
