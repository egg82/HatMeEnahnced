package me.egg82.hme.registries;

import org.bukkit.Material;

import ninja.egg82.patterns.registries.Registry;

public class MaterialRegistry extends Registry<String, Material> {
	//vars
	
	//constructor
	@SuppressWarnings("deprecation")
	public MaterialRegistry() {
		super(String.class, Material.class);
		
		for (Material m : Material.values()) {
			setRegister(m.name(), m);
			setRegister(String.valueOf(m.getId()), m);
		}
	}
	
	//public
	
	//private
	
}
