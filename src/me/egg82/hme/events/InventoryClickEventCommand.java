package me.egg82.hme.events;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.lists.GlowMaterialSet;
import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.utils.InventoryUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.EventHandler;

public class InventoryClickEventCommand extends EventHandler<InventoryClickEvent> {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private IConcurrentSet<Material> glowMaterialSet = ServiceLocator.getService(GlowMaterialSet.class);
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
		
		if (helmet != null && glowMaterialSet.contains(helmet.getType())) {
			if (glowSet.add(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
			}
		} else {
			if (glowSet.remove(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.removeLight(loc, false);
			}
		}
	}
}
