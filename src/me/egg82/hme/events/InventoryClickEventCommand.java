package me.egg82.hme.events;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;

public class InventoryClickEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	
	//constructor
	public InventoryClickEventCommand() {
		super();
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
		
		if (inv.getHelmet() != null) {
			glowRegistry.setRegister(e.getWhoClicked().getUniqueId().toString(), Player.class, null);
		}
	}
}
