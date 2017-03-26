package me.egg82.hme.services;

import org.bukkit.Material;

import ninja.egg82.patterns.Registry;

public class MaterialRegistry extends Registry {
	//vars
	
	//constructor
	public MaterialRegistry() {
		setRegister(Material.TORCH.toString().toLowerCase(), Material.class, Material.TORCH);
		setRegister(Material.LAVA.toString().toLowerCase(), Material.class, Material.LAVA);
		setRegister(Material.STATIONARY_LAVA.toString().toLowerCase(), Material.class, Material.STATIONARY_LAVA);
		setRegister(Material.LAVA_BUCKET.toString().toLowerCase(), Material.class, Material.LAVA_BUCKET);
		setRegister(Material.FIRE.toString().toLowerCase(), Material.class, Material.FIRE);
		setRegister(Material.FIREBALL.toString().toLowerCase(), Material.class, Material.FIREBALL);
		setRegister(Material.GLOWSTONE.toString().toLowerCase(), Material.class, Material.GLOWSTONE);
		setRegister(Material.GLOWSTONE_DUST.toString().toLowerCase(), Material.class, Material.GLOWSTONE_DUST);
		setRegister(Material.BURNING_FURNACE.toString().toLowerCase(), Material.class, Material.BURNING_FURNACE);
		setRegister(Material.GLOWING_REDSTONE_ORE.toString().toLowerCase(), Material.class, Material.GLOWING_REDSTONE_ORE);
		setRegister(Material.REDSTONE_TORCH_ON.toString().toLowerCase(), Material.class, Material.REDSTONE_TORCH_ON);
		setRegister(Material.JACK_O_LANTERN.toString().toLowerCase(), Material.class, Material.JACK_O_LANTERN);
		setRegister(Material.REDSTONE_LAMP_ON.toString().toLowerCase(), Material.class, Material.REDSTONE_LAMP_ON);
		setRegister(Material.REDSTONE_BLOCK.toString().toLowerCase(), Material.class, Material.REDSTONE_BLOCK);
		setRegister(Material.BEACON.toString().toLowerCase(), Material.class, Material.BEACON);
		setRegister(Material.SEA_LANTERN.toString().toLowerCase(), Material.class, Material.SEA_LANTERN);
	}
	
	//public
	
	//private
	
}
