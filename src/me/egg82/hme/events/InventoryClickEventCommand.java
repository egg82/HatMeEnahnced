package me.egg82.hme.events;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.EventCommand;
import ninja.egg82.registry.interfaces.IRegistry;

import me.egg82.hme.enums.PluginServiceType;

public class InventoryClickEventCommand extends EventCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	
	//constructor
	public InventoryClickEventCommand() {
		super();
	}
	
	//public
	
	//private
	protected void execute() {
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
			glowRegistry.computeIfPresent(e.getWhoClicked().getUniqueId().toString(), (k, v) -> {
				return null;
			});
		}
	}
}
