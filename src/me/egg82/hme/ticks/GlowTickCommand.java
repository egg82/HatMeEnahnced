package me.egg82.hme.ticks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.interfaces.ILightHelper;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.commands.TickCommand;
import ninja.egg82.registry.interfaces.IRegistry;

public class GlowTickCommand extends TickCommand {
	//vars
	private IRegistry reg = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_REGISTRY);
	private IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_MATERIAL_REGISTRY);
	private ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(PluginServiceType.LIGHT_HELPER);
	
	//constructor
	public GlowTickCommand() {
		super();
		ticks = 5l;
	}
	
	//public
	
	//private
	protected void execute() {
		String[] names = reg.registryNames();
		for (String name : names) {
			e(name, (Player) reg.getRegister(name));
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
		
		if (helmet == null || !glowMaterialRegistry.contains(helmet.getType().toString().toLowerCase())) {
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX());
			loc.setY(loc.getBlockY() + 1.0d);
			loc.setZ(loc.getBlockZ());
			lightHelper.removeLight(loc, true);
			
			reg.setRegister(uuid, null);
		}
	}
}
