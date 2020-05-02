package me.egg82.hme.ticks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.lists.GlowMaterialSet;
import me.egg82.hme.lists.GlowSet;
import me.egg82.hme.reflection.light.ILightHelper;
import ninja.egg82.bukkit.handlers.TickHandler;
import ninja.egg82.bukkit.utils.CommandUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.patterns.ServiceLocator;

public class GlowTickCommand extends TickHandler {
	//vars
	private IConcurrentSet<UUID> glowSet = ServiceLocator.getService(GlowSet.class);
	private IConcurrentSet<Material> glowMaterialSet = ServiceLocator.getService(GlowMaterialSet.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public GlowTickCommand() {
		super(0L, 5L);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		for (UUID key : glowSet) {
			e(key, CommandUtil.getPlayerByUuid(key));
		}
	}
	private void e(UUID uuid, Player player) {
		if (player == null) {
			return;
		}
		
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null || !glowMaterialSet.contains(helmet.getType())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
			
			glowSet.remove(uuid);
		}
	}
}
