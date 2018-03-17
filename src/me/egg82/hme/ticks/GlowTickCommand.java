package me.egg82.hme.ticks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.services.GlowMaterialRegistry;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.TickCommand;
import ninja.egg82.plugin.utils.CommandUtil;

public class GlowTickCommand extends TickCommand {
	//vars
	private IRegistry<UUID> glowRegistry = ServiceLocator.getService(GlowRegistry.class);
	private IRegistry<String> glowMaterialRegistry = ServiceLocator.getService(GlowMaterialRegistry.class);
	private ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public GlowTickCommand() {
		super();
		ticks = 5L;
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		for (UUID key : glowRegistry.getKeys()) {
			e(key, CommandUtil.getPlayerByUuid(key));
		}
	}
	private void e(UUID uuid, Player player) {
		if (player == null) {
			return;
		}
		
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null || !glowMaterialRegistry.hasRegister(helmet.getType().name())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
			
			glowRegistry.removeRegister(uuid);
		}
	}
}
