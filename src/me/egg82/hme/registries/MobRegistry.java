package me.egg82.hme.registries;

import java.util.UUID;

import ninja.egg82.patterns.registries.Registry;

public class MobRegistry extends Registry<UUID, UUID> {
	//vars
	
	//constructor
	public MobRegistry() {
		super(UUID.class, UUID.class);
	}
	
	//public
	
	//private
	
}
