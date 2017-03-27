package me.egg82.hme.ticks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.services.GlowRegistry;
import me.egg82.hme.services.MaterialRegistry;
import me.egg82.hme.util.ILightHelper;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.TickCommand;

public class GlowTickCommand extends TickCommand {
	//vars
	private IRegistry glowRegistry = (IRegistry) ServiceLocator.getService(GlowRegistry.class);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(MaterialRegistry.class);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
	
	//constructor
	public GlowTickCommand() {
		super();
		ticks = 5l;
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		String[] names = glowRegistry.getRegistryNames();
		for (String name : names) {
			e(name, (Player) glowRegistry.getRegister(name));
		}
	}
	private void e(String uuid, Player player) {
		if (player == null) {
			return;
		}
		
		PlayerInventory inv = player.getInventory();
		
		if (inv == null) {
			return;
		}
		
		ItemStack helmet = inv.getHelmet();
		
		if (helmet == null || !glowMaterialRegistry.hasRegister(helmet.getType().toString().toLowerCase())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5d);
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ() + 0.5d);
			lightHelper.removeLight(loc, false);
			
			glowRegistry.setRegister(uuid, Player.class, null);
		}
	}
}
