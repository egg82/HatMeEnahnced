package me.egg82.hme;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import ninja.egg82.analytics.exceptions.GameAnalyticsExceptionHandler;
import ninja.egg82.analytics.exceptions.IExceptionHandler;
import ninja.egg82.analytics.exceptions.RollbarExceptionHandler;
import ninja.egg82.bukkit.BasePlugin;
import ninja.egg82.bukkit.processors.CommandProcessor;
import ninja.egg82.bukkit.processors.EventProcessor;
import ninja.egg82.concurrent.IConcurrentSet;
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
		super(25040);
		
		exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		getLogger().setLevel(Level.WARNING);
	}
	
	//public
	public void onLoad() {
		super.onLoad();

		if (!Bukkit.getName().equals("Paper") && !Bukkit.getName().equals("PaperSpigot")) {
            printWarning(ChatColor.AQUA + "============================================");
            printWarning("Please note that HatMeEnhanced works better with Paper!");
            printWarning("https://whypaper.emc.gs/");
            printWarning(ChatColor.AQUA + "============================================");
        }
		
		PluginReflectUtil.addServicesFromPackage("me.egg82.hme.registries", true);
		PluginReflectUtil.addServicesFromPackage("me.egg82.hme.lists", true);
		
		PluginManager manager = getServer().getPluginManager();
		
		if (manager.getPlugin("LightAPI") != null) {
			printInfo(ChatColor.GREEN + "Enabling support for LightAPI.");
			ServiceLocator.provideService(LightAPIHelper.class);
		} else if (manager.getPlugin("FastAsyncWorldEdit") != null) {
			printInfo(ChatColor.GREEN + "Enabling support for FAWE.");
			ServiceLocator.provideService(FAWELightHelper.class);
		} else {
			printWarning(ChatColor.RED + "Neither LightAPI nor FAWE were found. Lights won't appear with blocks that light up.");
			ServiceLocator.provideService(NullLightHelper.class);
		}
	}
	public void onEnable() {
		super.onEnable();
		
		swapExceptionHandlers(new RollbarExceptionHandler("455ef15583a54d6995c5dc1a64861a6c", "production", version, getServerId(), getName()));

		ThreadUtil.rename(getName());

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

		enableMessage();
		
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

		ThreadUtil.submit(checkUpdate);
        if (exceptionHandler.hasLimit()) {
            ThreadUtil.schedule(checkExceptionLimitReached, 2L * 60L * 1000L);
        }
	}
	public void onDisable() {
		super.onDisable();
		
		ThreadUtil.shutdown(1000L);
		
		ILightHelper lightHelper = ServiceLocator.getService(ILightHelper.class);
		lightHelper.removeAllLights();

		List<IMessageHandler> services = ServiceLocator.removeServices(IMessageHandler.class);
        for (IMessageHandler handler : services) {
            try {
                handler.close();
            } catch (Exception ex) {

            }
        }
		
		ServiceLocator.getService(CommandProcessor.class).clear();
		ServiceLocator.getService(EventProcessor.class).clear();
		
		exceptionHandler.close();
		
		disableMessage();
	}
	
	//private
	private Runnable checkUpdate = new Runnable() {
        public void run() {
            if (isUpdateAvailable()) {
                return;
            }
            
            boolean update = false;
            
            try {
                update = checkUpdate();
            } catch (Exception ex) {
                printWarning("Could not check for update.");
                ex.printStackTrace();
                ThreadUtil.schedule(checkUpdate, 60L * 60L * 1000L);
                return;
            }
            
            if (!update) {
                ThreadUtil.schedule(checkUpdate, 60L * 60L * 1000L);
                return;
            }

            String latestVersion = null;
            try {
                latestVersion = getLatestVersion();
            } catch (Exception ex) {
                ThreadUtil.schedule(checkUpdate, 60L * 60L * 1000L);
                return;
            }

            printInfo(ChatColor.AQUA + "Update available! New version: " + ChatColor.YELLOW + latestVersion);

            ThreadUtil.schedule(checkUpdate, 60L * 60L * 1000L);
        }
	};
	private Runnable checkExceptionLimitReached = new Runnable() {
		public void run() {
			if (exceptionHandler.isLimitReached()) {
				swapExceptionHandlers(new GameAnalyticsExceptionHandler("ffe3c97598f4a511d29e5aa4a086d99b", "118e0c9a4258a412624ad0f9104cadf6216c595a", version, getServerId(), getName()));
			}
			
			if (exceptionHandler.hasLimit()) {
				ThreadUtil.schedule(checkExceptionLimitReached, 10L * 60L * 1000L);
			}
		}
	};
	
	private void swapExceptionHandlers(IExceptionHandler newHandler) {
		List<IExceptionHandler> oldHandlers = ServiceLocator.removeServices(IExceptionHandler.class);
		
		exceptionHandler = newHandler;
		ServiceLocator.provideService(exceptionHandler);
		
		Logger logger = getLogger();
		if (exceptionHandler instanceof Handler) {
			logger.addHandler((Handler) exceptionHandler);
		}
		
		for (IExceptionHandler handler : oldHandlers) {
			if (handler instanceof Handler) {
				logger.removeHandler((Handler) handler);
			}
			
			handler.close();
			exceptionHandler.addLogs(handler.getUnsentLogs());
		}
	}
	
	private void enableMessage() {
		printInfo(ChatColor.GREEN + "Enabled.");
		printInfo(ChatColor.AQUA + "[Version " + getDescription().getVersion() + "] " + ChatColor.DARK_GREEN + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.GOLD + numTicks + " tick handlers " + ChatColor.BLUE + numMessages + " message handlers");
		printInfo("Attempting to load compatibility with Bukkit version " + getGameVersion());
	}
	private void disableMessage() {
		printInfo(ChatColor.RED + "Disabled");
	}
}
