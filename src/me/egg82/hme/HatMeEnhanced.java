package me.egg82.hme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Timer;

import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.BasePlugin;
import ninja.egg82.plugin.enums.SpigotRegType;
import ninja.egg82.plugin.utils.ReflectUtil;
import ninja.egg82.registry.Registry;
import ninja.egg82.registry.interfaces.IRegistry;
import ninja.egg82.utils.Util;

import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.util.LightAPIHelper;
import me.egg82.hme.util.nulls.NullLightHelper;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HatMeEnhanced extends BasePlugin {
	//vars
	private Timer updateTimer = null;
	
	private int numCommands = 0;
	private int numEvents = 0;
	private int numPermissions = 0;
	private int numTicks = 0;
	
	//constructor
	public HatMeEnhanced() {
		
	}
	
	//public
	public void onLoad() {
		super.onLoad();
		
		Object[] enums = Util.getStaticFields(PluginServiceType.class);
		String[] services = Arrays.copyOf(enums, enums.length, String[].class);
		for (String s : services) {
			ServiceLocator.provideService(s, Registry.class);
		}
		
		PluginManager manager = getServer().getPluginManager();
		
		if (manager.getPlugin("LightAPI") != null) {
			ServiceLocator.provideService(PluginServiceType.LIGHT_HELPER, LightAPIHelper.class);
		} else {
			ServiceLocator.provideService(PluginServiceType.LIGHT_HELPER, NullLightHelper.class);
		}
		
		updateTimer = new Timer(24 * 60 * 60 * 1000, onUpdateTimer);
	}
	@SuppressWarnings("deprecation")
	public void onEnable() {
		super.onEnable();
		
		try {
			@SuppressWarnings("unused")
			Metrics m = new Metrics(this);
		} catch (Exception ex) {
			
		}
		
		numCommands = ReflectUtil.addCommandsFromPackage(commandHandler, "me.egg82.hme.commands");
		numEvents = ReflectUtil.addEventsFromPackage(eventListener, "me.egg82.hme.events");
		numPermissions = ReflectUtil.addPermissionsFromClass(permissionsManager, PermissionsType.class);
		numTicks = ReflectUtil.addTicksFromPackage(tickHandler, "me.egg82.hme.ticks");
		
		Object[] enums = Util.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			if (m != null) {
				permissionsManager.addPermission("hme.hat." + Integer.toString(m.getId()));
				permissionsManager.addPermission("hme.hat." + m.toString().toLowerCase());
			}
		}
		
		addGlowMaterials();
		
		enableMessage(Bukkit.getConsoleSender());
		checkUpdate();
		updateTimer.setRepeats(true);
		updateTimer.start();
	}
	public void onDisable() {
		commandHandler.clearCommands();
		eventListener.clearEvents();
		permissionsManager.clearPermissions();
		tickHandler.clearTickCommands();
		
		disableMessage(Bukkit.getConsoleSender());
	}
	
	//private
	private ActionListener onUpdateTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			checkUpdate();
		}
	};
	private void checkUpdate() {
		Updater updater = new Updater(this, 100559, getFile(), UpdateType.NO_DOWNLOAD, false);
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "--== " + ChatColor.YELLOW + "HatMeEnhanced UPDATE AVAILABLE" + ChatColor.GREEN + " ==--");
		}
	}
	
	private void enableMessage(ConsoleCommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + " _   _       _  ___  ___     _____      _                              _ ");
		sender.sendMessage(ChatColor.YELLOW + "| | | |     | | |  \\/  |    |  ___|    | |                            | |");
		sender.sendMessage(ChatColor.YELLOW + "| |_| | __ _| |_| .  . | ___| |__ _ __ | |__   __ _ _ __   ___ ___  __| |");
		sender.sendMessage(ChatColor.YELLOW + "|  _  |/ _` | __| |\\/| |/ _ |  __| '_ \\| '_ \\ / _` | '_ \\ / __/ _ \\/ _` |");
		sender.sendMessage(ChatColor.YELLOW + "| | | | (_| | |_| |  | |  __| |__| | | | | | | (_| | | | | (_|  __| (_| |");
		sender.sendMessage(ChatColor.YELLOW + "\\_| |_/\\__,_|\\__\\_|  |_/\\___\\____|_| |_|_| |_|\\__,_|_| |_|\\___\\___|\\__,_|");
		sender.sendMessage(ChatColor.GREEN + "[Version " + getDescription().getVersion() + "] " + ChatColor.RED + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.WHITE + numPermissions + " permissions " + ChatColor.YELLOW + numTicks + " tick handlers");
		sender.sendMessage(ChatColor.WHITE + "[HatMeEnhanced] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + initReg.getRegister(SpigotRegType.GAME_VERSION));
	}
	private void disableMessage(ConsoleCommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
	}
	
	private void addGlowMaterials() {
		IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(PluginServiceType.GLOW_MATERIAL_REGISTRY);
		
		glowMaterialRegistry.setRegister(Material.TORCH.toString().toLowerCase(), Material.TORCH);
		glowMaterialRegistry.setRegister(Material.LAVA.toString().toLowerCase(), Material.LAVA);
		glowMaterialRegistry.setRegister(Material.STATIONARY_LAVA.toString().toLowerCase(), Material.STATIONARY_LAVA);
		glowMaterialRegistry.setRegister(Material.LAVA_BUCKET.toString().toLowerCase(), Material.LAVA_BUCKET);
		glowMaterialRegistry.setRegister(Material.FIRE.toString().toLowerCase(), Material.FIRE);
		glowMaterialRegistry.setRegister(Material.FIREBALL.toString().toLowerCase(), Material.FIREBALL);
		glowMaterialRegistry.setRegister(Material.GLOWSTONE.toString().toLowerCase(), Material.GLOWSTONE);
		glowMaterialRegistry.setRegister(Material.GLOWSTONE_DUST.toString().toLowerCase(), Material.GLOWSTONE_DUST);
		glowMaterialRegistry.setRegister(Material.BURNING_FURNACE.toString().toLowerCase(), Material.BURNING_FURNACE);
		glowMaterialRegistry.setRegister(Material.GLOWING_REDSTONE_ORE.toString().toLowerCase(), Material.GLOWING_REDSTONE_ORE);
		glowMaterialRegistry.setRegister(Material.REDSTONE_TORCH_ON.toString().toLowerCase(), Material.REDSTONE_TORCH_ON);
		glowMaterialRegistry.setRegister(Material.JACK_O_LANTERN.toString().toLowerCase(), Material.JACK_O_LANTERN);
		glowMaterialRegistry.setRegister(Material.REDSTONE_LAMP_ON.toString().toLowerCase(), Material.REDSTONE_LAMP_ON);
		glowMaterialRegistry.setRegister(Material.REDSTONE_BLOCK.toString().toLowerCase(), Material.REDSTONE_BLOCK);
		glowMaterialRegistry.setRegister(Material.BEACON.toString().toLowerCase(), Material.BEACON);
		glowMaterialRegistry.setRegister(Material.SEA_LANTERN.toString().toLowerCase(), Material.SEA_LANTERN);
	}
}
