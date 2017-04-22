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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.BasePlugin;
import ninja.egg82.plugin.handlers.PermissionsManager;
import ninja.egg82.plugin.utils.SpigotReflectUtil;
import ninja.egg82.plugin.utils.VersionUtil;
import ninja.egg82.startup.InitRegistry;
import ninja.egg82.utils.ReflectUtil;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.services.GlowMaterialRegistry;
import me.egg82.hme.util.ILightHelper;
import me.egg82.hme.util.LightAPIHelper;
import me.egg82.hme.util.NullLightHelper;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HatMeEnhanced extends BasePlugin {
	//vars
	private Metrics metrics = null;
	
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
		
		SpigotReflectUtil.addServicesFromPackage("me.egg82.hme.services");
		
		PluginManager manager = getServer().getPluginManager();
		
		if (manager.getPlugin("LightAPI") != null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[HatMeEnhanced] Enabling support for LightAPI.");
			ServiceLocator.provideService(LightAPIHelper.class);
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HatMeEnhanced] LightAPI was not found. Lights won't appear with blocks that light up.");
			ServiceLocator.provideService(NullLightHelper.class);
		}
		
		updateTimer = new Timer(24 * 60 * 60 * 1000, onUpdateTimer);
	}
	@SuppressWarnings("deprecation")
	public void onEnable() {
		super.onEnable();
		
		try {
			metrics = new Metrics(this);
		} catch (Exception ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[TrollCommands++] WARNING: Connection to metrics server could not be established. This affects nothing for server owners. Nothing to worry about!");
		}
		
		if (metrics != null) {
			metrics.addCustomChart(new Metrics.SingleLineChart("hats") {
				@Override
				public int getValue() {
					int numHats = 0;
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						ItemStack head = p.getInventory().getHelmet();
						if (head != null && head.getAmount() > 0 && head.getType() != Material.AIR) {
							numHats++;
						}
					}
					return numHats;
				}
			});
			metrics.addCustomChart(new Metrics.SingleLineChart("glowing_hats") {
				@Override
				public int getValue() {
					IRegistry glowMaterialRegistry = (IRegistry) ServiceLocator.getService(GlowMaterialRegistry.class);
					int numHats = 0;
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						ItemStack head = p.getInventory().getHelmet();
						if (head != null && head.getAmount() > 0 && glowMaterialRegistry.hasRegister(head.getType().name().toLowerCase())) {
							numHats++;
						}
					}
					return numHats;
				}
			});
		}
		
		numCommands = SpigotReflectUtil.addCommandsFromPackage("me.egg82.hme.commands");
		numEvents = SpigotReflectUtil.addEventsFromPackage("me.egg82.hme.events");
		numPermissions = SpigotReflectUtil.addPermissionsFromClass(PermissionsType.class);
		numTicks = SpigotReflectUtil.addTicksFromPackage("me.egg82.hme.ticks");
		
		PermissionsManager permissionsManager = (PermissionsManager) ServiceLocator.getService(PermissionsManager.class);
		
		Object[] enums = ReflectUtil.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			if (m != null) {
				permissionsManager.addPermission("hme.hat." + Integer.toString(m.getId()));
				permissionsManager.addPermission("hme.hat." + m.toString().toLowerCase());
			}
		}
		
		enums = ReflectUtil.getStaticFields(EntityType.class);
		EntityType[] entityTypes = Arrays.copyOf(enums, enums.length, EntityType[].class);
		for (EntityType e : entityTypes) {
			if (e != null) {
				permissionsManager.addPermission("hme.mob." + e.toString().toLowerCase());
			}
		}
		
		enableMessage(Bukkit.getConsoleSender());
		checkUpdate();
		updateTimer.setRepeats(true);
		updateTimer.start();
	}
	public void onDisable() {
		super.onDisable();
		
		ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
		lightHelper.removeAllLights();
		
		SpigotReflectUtil.clearAll();
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
			String latestVersion = updater.getLatestName();
			latestVersion = latestVersion.substring(latestVersion.lastIndexOf('v') + 1);
			String currentVersion = getDescription().getVersion();
			
			int[] latest = VersionUtil.parseVersion(latestVersion, '.');
			int[] current = VersionUtil.parseVersion(currentVersion, '.');
			
			for (int i = 0; i < Math.min(latest.length, current.length); i++) {
				if (latest[i] < current[i]) {
					return;
				}
			}
			
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "--== " + ChatColor.YELLOW + "HatMeEnhanced UPDATE AVAILABLE (Latest: " + latestVersion + " Current: " + currentVersion + ") " + ChatColor.GREEN + " ==--");
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
		sender.sendMessage(ChatColor.WHITE + "[HatMeEnhanced] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + ((InitRegistry) ServiceLocator.getService(InitRegistry.class)).getRegister("game.version"));
	}
	private void disableMessage(ConsoleCommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
	}
}
