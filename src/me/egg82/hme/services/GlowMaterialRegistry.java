package me.egg82.hme.services;

import org.bukkit.Material;

import ninja.egg82.patterns.Registry;

public class GlowMaterialRegistry extends Registry<String> {
	//vars
	
	//constructor
	public GlowMaterialRegistry() {
		super(String.class);
		
		setRegister(Material.TORCH.name(), Material.TORCH);
		setRegister(Material.LAVA.name(), Material.LAVA);
		setRegister(Material.STATIONARY_LAVA.name(), Material.STATIONARY_LAVA);
		setRegister(Material.LAVA_BUCKET.name(), Material.LAVA_BUCKET);
		setRegister(Material.FIRE.name(), Material.FIRE);
		setRegister(Material.FIREBALL.name(), Material.FIREBALL);
		setRegister(Material.GLOWSTONE.name(), Material.GLOWSTONE);
		setRegister(Material.GLOWSTONE_DUST.name(), Material.GLOWSTONE_DUST);
		setRegister(Material.BURNING_FURNACE.name(), Material.BURNING_FURNACE);
		setRegister(Material.GLOWING_REDSTONE_ORE.name(), Material.GLOWING_REDSTONE_ORE);
		setRegister(Material.REDSTONE_TORCH_ON.name(), Material.REDSTONE_TORCH_ON);
		setRegister(Material.JACK_O_LANTERN.name(), Material.JACK_O_LANTERN);
		setRegister(Material.REDSTONE_LAMP_ON.name(), Material.REDSTONE_LAMP_ON);
		setRegister(Material.REDSTONE_BLOCK.name(), Material.REDSTONE_BLOCK);
		setRegister(Material.BEACON.name(), Material.BEACON);
		setRegister(Material.SEA_LANTERN.name(), Material.SEA_LANTERN);
	}
	
	//public
	
	//private
	
}
