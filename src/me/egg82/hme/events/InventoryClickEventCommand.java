package me.egg82.hme.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.GlowMaterialRegistry;
import me.egg82.hme.util.ILightHelper;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class InventoryClickEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(GlowMaterialRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public InventoryClickEventCommand(Event e) {
		super(e);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseoncds) {
		InventoryClickEvent e = (InventoryClickEvent) event;
		PlayerInventory inv = null;
		
		if (e.isCancelled()) {
			return;
		}
		
		try {
			inv = (PlayerInventory) e.getClickedInventory();
		} catch (Exception ex) {
			return;
		}
		
		if (inv == null) {
			return;
		}
		
		ItemStack helmet = inv.getHelmet();
		Player player = (Player) e.getWhoClicked();
		String uuid = player.getUniqueId().toString();
		
		if (helmet != null && glowMaterialRegistry.hasRegister(helmet.getType().toString().toLowerCase())) {
			if (!glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.addLight(loc, false);
				
				glowRegistry.setRegister(uuid, Player.class, player);
			}
		} else {
			if (glowRegistry.hasRegister(uuid)) {
				Location loc = player.getLocation().clone();
				loc.setX(loc.getBlockX() + 0.5d);
				loc.setY(loc.getBlockY() + 1.0d);
				loc.setZ(loc.getBlockZ() + 0.5d);
				lightHelper.removeLight(loc, false);
				
				glowRegistry.setRegister(uuid, Player.class, null);
			}
		}
	}
}
