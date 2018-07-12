package me.egg82.hme;

import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.egg82.hme.lists.GlowMaterialSet;
import me.egg82.hme.reflection.light.FAWELightHelper;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.reflection.light.LightAPIHelper;
import me.egg82.hme.reflection.light.NullLightHelper;
import me.egg82.hme.ticks.DismountTickCommand;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;
import ninja.egg82.bukkit.BasePlugin;
import ninja.egg82.bukkit.processors.CommandProcessor;
import ninja.egg82.bukkit.processors.EventProcessor;
import ninja.egg82.bukkit.utils.VersionUtil;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.exceptionHandlers.GameAnalyticsExceptionHandler;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.exceptionHandlers.RollbarExceptionHandler;
import ninja.egg82.exceptionHandlers.builders.GameAnalyticsBuilder;
import ninja.egg82.exceptionHandlers.builders.RollbarBuilder;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.messaging.IMessageHandler;
import ninja.egg82.plugin.utils.PluginReflectUtil;
import ninja.egg82.utils.ThreadUtil;

public class HatMeEnhanced extends BasePlugin {
	//vars
	private Metrics metrics = null;
	
	private int numMessages = 0;
	private int numCommands = 0;
	private int numEvents = 0;
	private int numTicks = 0;
	
	private IExceptionHandler exceptionHandler = null;
	private String version = getDescription().getVersion();
	
	//constructor
	public HatMeEnhanced() {
		super();
		
		getLogger().setLevel(Level.WARNING);
		IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		ServiceLocator.removeServices(IExceptionHandler.class);
		
		ServiceLocator.provideService(RollbarExceptionHandler.class, false);
		exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		oldExceptionHandler.disconnect();
		exceptionHandler.connect(new RollbarBuilder("455ef15583a54d6995c5dc1a64861a6c", "production", version, getServerId()), "HatMeEnhanced");
		exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
		exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
	}
	
	//public
	public void onLoad() {
		super.onLoad();
		
		PluginReflectUtil.addServicesFromPackage("me.egg82.hme.registries", true);
		PluginReflectUtil.addServicesFromPackage("me.egg82.hme.lists", true);
		
		PluginManager manager = getServer().getPluginManager();
		
		if (manager.getPlugin("LightAPI") != null) {
			printInfo(ChatColor.GREEN + "[HatMeEnhanced] Enabling support for LightAPI.");
			ServiceLocator.provideService(LightAPIHelper.class);
		} else if (manager.getPlugin("FastAsyncWorldEdit") != null) {
			printInfo(ChatColor.GREEN + "[HatMeEnhanced] Enabling support for FAWE.");
			ServiceLocator.provideService(FAWELightHelper.class);
		} else {
			printWarning(ChatColor.RED + "[HatMeEnhanced] Neither LightAPI nor FAWE were found. Lights won't appear with blocks that light up.");
			ServiceLocator.provideService(NullLightHelper.class);
		}
	}
	public void onEnable() {
		super.onEnable();
		
		numCommands = ServiceLocator.getService(CommandProcessor.class).addHandlersFromPackage("me.egg82.hme.commands", PluginReflectUtil.getCommandMapFromPackage("me.egg82.hme.commands", false, null, "Command"), false);
		numEvents = ServiceLocator.getService(EventProcessor.class).addHandlersFromPackage("me.egg82.hme.events");
		numMessages = ServiceLocator.getService(IMessageHandler.class).addHandlersFromPackage("me.egg82.hme.messages");
		numTicks = PluginReflectUtil.addServicesFromPackage("me.egg82.hme.ticks", false);
		
		Class<?> entityDismount = null;
		try {
			entityDismount = Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
		} catch (Exception ex) {
			
		}
		if (entityDismount != null) {
			// EntityDismountEvent exists, remove the tick handler for it
			ServiceLocator.removeServices(DismountTickCommand.class);
		}
		
		ThreadUtil.rename(getName());
		ThreadUtil.submit(new Runnable() {
			public void run() {
				try {
					metrics = new Metrics(ServiceLocator.getService(JavaPlugin.class));
				} catch (Exception ex) {
					printInfo(ChatColor.YELLOW + "[HatMeEnhanced] WARNING: Connection to metrics server could not be established. This affects nothing for server owners, but it does make me sad :(");
				}
				
				if (metrics != null) {
					metrics.addCustomChart(new Metrics.SingleLineChart("hats", () -> {
						int numHats = 0;
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							ItemStack head = p.getInventory().getHelmet();
							if (head != null && head.getAmount() > 0 && head.getType() != Material.AIR) {
								numHats++;
							}
						}
						return Integer.valueOf(numHats);
					}));
					metrics.addCustomChart(new Metrics.SingleLineChart("glowing_hats",  () -> {
						IConcurrentSet<Material> glowMaterialSet = ServiceLocator.getService(GlowMaterialSet.class);
						int numHats = 0;
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							ItemStack head = p.getInventory().getHelmet();
							if (head != null && head.getAmount() > 0 && glowMaterialSet.contains(head.getType())) {
								numHats++;
							}
						}
						return Integer.valueOf(numHats);
					}));
				}
			}
		});
		ThreadUtil.schedule(checkUpdate, 24L * 60L * 60L * 1000L);
		ThreadUtil.schedule(checkExceptionLimitReached, 60L * 60L * 1000L);
		
		enableMessage();
	}
	public void onDisable() {
		super.onDisable();
		
		ThreadUtil.shutdown(1000L);
		
		ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
		lightHelper.removeAllLights();
		
		ServiceLocator.getService(CommandProcessor.class).clear();
		ServiceLocator.getService(EventProcessor.class).clear();
		
		disableMessage();
	}
	
	//private
	private Runnable checkUpdate = new Runnable() {
		public void run() {
			Updater updater = new Updater(ServiceLocator.getService(JavaPlugin.class), 100559, getFile(), UpdateType.NO_DOWNLOAD, false);
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
				
				printWarning(ChatColor.GREEN + "--== " + ChatColor.YELLOW + "HatMeEnhanced UPDATE AVAILABLE (Latest: " + latestVersion + " Current: " + currentVersion + ") " + ChatColor.GREEN + " ==--");
			}
			
			ThreadUtil.schedule(checkUpdate, 24L * 60L * 60L * 1000L);
		}
	};
	
	private Runnable checkExceptionLimitReached = new Runnable() {
		public void run() {
			if (exceptionHandler.isLimitReached()) {
				IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
				ServiceLocator.removeServices(IExceptionHandler.class);
				
				ServiceLocator.provideService(GameAnalyticsExceptionHandler.class, false);
				exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
				oldExceptionHandler.disconnect();
				exceptionHandler.connect(new GameAnalyticsBuilder("ffe3c97598f4a511d29e5aa4a086d99b", "118e0c9a4258a412624ad0f9104cadf6216c595a", version, getServerId()), getName());
				exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
				exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
			}
			
			ThreadUtil.schedule(checkExceptionLimitReached, 60L * 60L * 1000L);
		}
	};
	
	private void enableMessage() {
		printInfo(ChatColor.AQUA + "HatMeEnhanced enabled.");
		printInfo(ChatColor.GREEN + "[Version " + getDescription().getVersion() + "] " + ChatColor.RED + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.YELLOW + numTicks + " tick handlers " + ChatColor.BLUE + numMessages + " message handlers");
		printInfo(ChatColor.WHITE + "[HatMeEnhanced] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + getGameVersion());
	}
	private void disableMessage() {
		printInfo(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
	}
}
