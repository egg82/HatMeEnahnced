package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.utils.InventoryUtil;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowMaterialRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class InventoryClickEventCommand extends EventCommand<InventoryClickEvent> {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private IRegistry<String> glowMaterialRegistry = ServiceLocator.getService(GlowMaterialRegistry.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public InventoryClickEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		if (event.isCancelled()) {
			return;
		}
		
		Inventory clicked = InventoryUtil.getClickedInventory(event);
		
		if (clicked == null || clicked.getType() != InventoryType.PLAYER) {
			return;
		}
		
		PlayerInventory inv = (PlayerInventory) clicked;
		ItemStack helmet = inv.getHelmet();
		Player player = (Player) inv.getHolder();
		UUID uuid = player.getUniqueId();
		
		if (helmet != null && glowMaterialRegistry.hasRegister(helmet.getType().name())) {
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
				
				glowRegistry.setRegister(uuid, null);
			}
		} else {
			if (glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.removeLight(loc, false);
				
				glowRegistry.removeRegister(uuid);
			}
		}
	}
}
