package me.egg82.hme.lists;

import org.bukkit.Material;

import ninja.egg82.bukkit.reflection.material.IMaterialHelper;
import ninja.egg82.concurrent.DynamicConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;

public class GlowMaterialSet extends DynamicConcurrentSet<Material> {
	//vars
	private static final long serialVersionUID = -3079214005056170469L;
	
	private IMaterialHelper materialHelper = ServiceLocator.getService(IMaterialHelper.class);
	
	//constructor
	public GlowMaterialSet() {
		super();
		
		addMaterial("torch");
		addMaterial("lava");
		addMaterial("stationary_lava");
		addMaterial("lava_bucket");
		addMaterial("fire");
		addMaterial("fireball");
		addMaterial("glowstone");
		addMaterial("glowstone_dust");
		addMaterial("burning_furnace");
		addMaterial("glowing_redstone_ore");
		addMaterial("redstone_torch_on");
		addMaterial("jack_o_lantern");
		addMaterial("redstone_lamp_on");
		addMaterial("redstone_block");
		addMaterial("beacon");
		addMaterial("sea_lantern");
	}
	
	//public
	
	//private
	private void addMaterial(String name) {
		try {
			add(materialHelper.getByName(name));
		} catch (Exception ex) {
			
		}
	}
}
