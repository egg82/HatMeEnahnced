package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowMaterialRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class PlayerJoinEventCommand extends EventCommand<PlayerJoinEvent> {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private IRegistry<String> glowMaterialRegistry = ServiceLocator.getService(GlowMaterialRegistry.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public PlayerJoinEventCommand(PlayerJoinEvent event) {
		super(event);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null) {
			return;
		}
		
		if (glowMaterialRegistry.hasRegister(helmet.getType().name())) {
			UUID uuid = player.getUniqueId();
			
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
				
				glowRegistry.setRegister(uuid, null);
			}
		}
	}
}
