package me.egg82.hme.exceptions;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryFullException extends RuntimeException {
	//vars
	public static final InventoryFullException EMPTY = new InventoryFullException(null, null);
	private static final long serialVersionUID = 448515428459324681L;
	
	private Inventory inventory = null;
	private ItemStack itemToPlace = null;
	
	//constructor
	public InventoryFullException(Inventory inventory, ItemStack itemToPlace) {
		super();
		
		this.inventory = inventory;
		this.itemToPlace = itemToPlace;
	}
	
	//public
	public Inventory getInventory() {
		return inventory;
	}
	public ItemStack getItemToPlace() {
		return itemToPlace;
	}
	
	//private
	
}
