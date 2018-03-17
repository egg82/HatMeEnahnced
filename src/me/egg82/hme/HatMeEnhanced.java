package me.egg82.hme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.Timer;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import ninja.egg82.exceptionHandlers.GameAnalyticsExceptionHandler;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.exceptionHandlers.RollbarExceptionHandler;
import ninja.egg82.exceptionHandlers.builders.GameAnalyticsBuilder;
import ninja.egg82.exceptionHandlers.builders.RollbarBuilder;
import ninja.egg82.patterns.IRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.BasePlugin;
import ninja.egg82.plugin.enums.BukkitInitType;
import ninja.egg82.plugin.handlers.CommandHandler;
import ninja.egg82.plugin.handlers.EventListener;
import ninja.egg82.plugin.handlers.IMessageHandler;
import ninja.egg82.plugin.handlers.PermissionsManager;
import ninja.egg82.plugin.handlers.TickHandler;
import ninja.egg82.plugin.services.LanguageRegistry;
import ninja.egg82.plugin.utils.BukkitReflectUtil;
import ninja.egg82.plugin.utils.VersionUtil;
import ninja.egg82.startup.InitRegistry;
import ninja.egg82.utils.FileUtil;
import ninja.egg82.utils.ReflectUtil;
import me.egg82.hme.enums.LanguageType;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.reflection.light.FAWELightHelper;
import me.egg82.hme.reflection.light.ILightHelper;
import me.egg82.hme.reflection.light.LightAPIHelper;
import me.egg82.hme.reflection.light.NullLightHelper;
import me.egg82.hme.services.GlowMaterialRegistry;
import me.egg82.hme.services.MaterialRegistry;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HatMeEnhanced extends BasePlugin {
	//vars
	private Metrics metrics = null;
	
	private Timer updateTimer = null;
	private Timer exceptionHandlerTimer = null;
	
	private int numMessages = 0;
	private int numCommands = 0;
	private int numEvents = 0;
	private int numPermissions = 0;
	private int numTicks = 0;
	
	private IExceptionHandler exceptionHandler = null;
	private String version = getDescription().getVersion();
	private String userId = Bukkit.getServerId().trim();
	
	//constructor
	public HatMeEnhanced() {
		super();
		
		if (userId.isEmpty() || userId.equalsIgnoreCase("unnamed") || userId.equalsIgnoreCase("unknown") || userId.equalsIgnoreCase("default")) {
			userId = UUID.randomUUID().toString();
			writeProperties();
		}
		
		getLogger().setLevel(Level.WARNING);
		IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		ServiceLocator.removeServices(IExceptionHandler.class);
		
		ServiceLocator.provideService(RollbarExceptionHandler.class, false);
		exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		oldExceptionHandler.disconnect();
		exceptionHandler.connect(new RollbarBuilder("455ef15583a54d6995c5dc1a64861a6c", "production", version, userId));
		exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
		exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
	}
	
	//public
	public void onLoad() {
		super.onLoad();
		
		BukkitReflectUtil.addServicesFromPackage("me.egg82.hme.services");
		
		PluginManager manager = getServer().getPluginManager();
		
		if (manager.getPlugin("FastAsyncWorldEdit") != null) {
			printInfo(ChatColor.GREEN + "[HatMeEnhanced] Enabling support for FAWE.");
			ServiceLocator.provideService(FAWELightHelper.class);
		} else if (manager.getPlugin("LightAPI") != null) {
			printInfo(ChatColor.GREEN + "[HatMeEnhanced] Enabling support for LightAPI.");
			ServiceLocator.provideService(LightAPIHelper.class);
		} else {
			printWarning(ChatColor.RED + "[HatMeEnhanced] LightAPI was not found. Lights won't appear with blocks that light up.");
			ServiceLocator.provideService(NullLightHelper.class);
		}
		
		populateLanguage();
		
		updateTimer = new Timer(24 * 60 * 60 * 1000, onUpdateTimer);
		exceptionHandlerTimer = new Timer(60 * 60 * 1000, onExceptionHandlerTimer);
	}
	@SuppressWarnings("deprecation")
	public void onEnable() {
		super.onEnable();
		
		try {
			metrics = new Metrics(this);
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
				return numHats;
			}));
			metrics.addCustomChart(new Metrics.SingleLineChart("glowing_hats",  () -> {
				IRegistry<String> glowMaterialRegistry = ServiceLocator.getService(GlowMaterialRegistry.class);
				int numHats = 0;
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					ItemStack head = p.getInventory().getHelmet();
					if (head != null && head.getAmount() > 0 && glowMaterialRegistry.hasRegister(head.getType().name())) {
						numHats++;
					}
				}
				return numHats;
			}));
		}
		
		numMessages = ServiceLocator.getService(IMessageHandler.class).addMessagesFromPackage("me.egg82.hme.messages");
		numCommands = ServiceLocator.getService(CommandHandler.class).addCommandsFromPackage("me.egg82.hme.commands", BukkitReflectUtil.getCommandMapFromPackage("me.egg82.hme.commands", false, null, "Command"), false);
		numEvents = ServiceLocator.getService(EventListener.class).addEventsFromPackage("me.egg82.hme.events");
		numPermissions = ServiceLocator.getService(PermissionsManager.class).addPermissionsFromClass(PermissionsType.class);
		numTicks = ServiceLocator.getService(TickHandler.class).addTicksFromPackage("me.egg82.hme.ticks");
		
		PermissionsManager permissionsManager = ServiceLocator.getService(PermissionsManager.class);
		IRegistry<String> materialRegistry = ServiceLocator.getService(MaterialRegistry.class);
		
		Object[] enums = ReflectUtil.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			if (m != null) {
				permissionsManager.addPermission("hme.hat." + Integer.toString(m.getId()));
				permissionsManager.addPermission("hme.hat." + m.name().toLowerCase());
				
				materialRegistry.setRegister(m.name(), m);
				materialRegistry.setRegister(Integer.toString(m.getId()), m);
			}
		}
		
		enums = ReflectUtil.getStaticFields(EntityType.class);
		EntityType[] entityTypes = Arrays.copyOf(enums, enums.length, EntityType[].class);
		for (EntityType e : entityTypes) {
			if (e != null && e.isAlive()) {
				permissionsManager.addPermission("hme.mob." + e.name().toLowerCase());
			}
		}
		
		enableMessage();
		checkUpdate();
		updateTimer.setRepeats(true);
		updateTimer.start();
		checkExceptionLimitReached();
		exceptionHandlerTimer.setRepeats(true);
		exceptionHandlerTimer.start();
	}
	public void onDisable() {
		super.onDisable();
		
		ILightHelper lightHelper = (ILightHelper) ServiceLocator.getService(ILightHelper.class);
		lightHelper.removeAllLights();
		
		BukkitReflectUtil.clearAll();
		disableMessage();
	}
	
	//private
	private ActionListener onUpdateTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			exceptionHandler.addThread(Thread.currentThread());
			checkUpdate();
			exceptionHandler.removeThread(Thread.currentThread());
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
			
			printWarning(ChatColor.GREEN + "--== " + ChatColor.YELLOW + "HatMeEnhanced UPDATE AVAILABLE (Latest: " + latestVersion + " Current: " + currentVersion + ") " + ChatColor.GREEN + " ==--");
		}
	}
	
	private ActionListener onExceptionHandlerTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			checkExceptionLimitReached();
		}
	};
	private void checkExceptionLimitReached() {
		if (exceptionHandler.isLimitReached()) {
			IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
			ServiceLocator.removeServices(IExceptionHandler.class);
			
			ServiceLocator.provideService(GameAnalyticsExceptionHandler.class, false);
			exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
			oldExceptionHandler.disconnect();
			exceptionHandler.connect(new GameAnalyticsBuilder("ffe3c97598f4a511d29e5aa4a086d99b", "118e0c9a4258a412624ad0f9104cadf6216c595a", version, userId));
			exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
			exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
		}
	}
	
	private void enableMessage() {
		printInfo(ChatColor.YELLOW + " _   _       _  ___  ___     _____      _                              _ ");
		printInfo(ChatColor.YELLOW + "| | | |     | | |  \\/  |    |  ___|    | |                            | |");
		printInfo(ChatColor.YELLOW + "| |_| | __ _| |_| .  . | ___| |__ _ __ | |__   __ _ _ __   ___ ___  __| |");
		printInfo(ChatColor.YELLOW + "|  _  |/ _` | __| |\\/| |/ _ |  __| '_ \\| '_ \\ / _` | '_ \\ / __/ _ \\/ _` |");
		printInfo(ChatColor.YELLOW + "| | | | (_| | |_| |  | |  __| |__| | | | | | | (_| | | | | (_|  __| (_| |");
		printInfo(ChatColor.YELLOW + "\\_| |_/\\__,_|\\__\\_|  |_/\\___\\____|_| |_|_| |_|\\__,_|_| |_|\\___\\___|\\__,_|");
		printInfo(ChatColor.GREEN + "[Version " + getDescription().getVersion() + "] " + ChatColor.RED + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.WHITE + numPermissions + " permissions " + ChatColor.YELLOW + numTicks + " tick handlers " + ChatColor.BLUE + numMessages + " message handlers");
		printInfo(ChatColor.WHITE + "[HatMeEnhanced] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + ((InitRegistry) ServiceLocator.getService(InitRegistry.class)).getRegister(BukkitInitType.GAME_VERSION));
	}
	private void disableMessage() {
		printInfo(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
	}
	
	private void populateLanguage() {
		IRegistry<String> languageRegistry = ServiceLocator.getService(LanguageRegistry.class);
		
		languageRegistry.setRegister(LanguageType.HAT_CANCEL, ChatColor.YELLOW + "Mob/Player hat canceled. Please use /hat with an open hand to start again.");
		languageRegistry.setRegister(LanguageType.INVALID_PERMISSIONS_HAT_TYPE, ChatColor.RED + "You don't have permissions to use that type of hat!");
		languageRegistry.setRegister(LanguageType.INVENTORY_FULL, ChatColor.RED + "You don't have any space left in your inventory!");
		languageRegistry.setRegister(LanguageType.MOB_OWNED, ChatColor.RED + "That mob/player is already someone else's hat!");
		languageRegistry.setRegister(LanguageType.PLAYER_IMMUNE, ChatColor.RED + "Player is immune.");
		languageRegistry.setRegister(LanguageType.PLAYER_OFFLINE, ChatColor.RED + "Player is no longer online.");
	}
	
	private void writeProperties() {
		File propertiesFile = new File(Bukkit.getWorldContainer(), "server.properties");
		String path = propertiesFile.getAbsolutePath();
		
		if (!FileUtil.pathExists(path) || !FileUtil.pathIsFile(path)) {
			return;
		}
		
		try {
			FileUtil.open(path);
			
			String[] lines = toString(FileUtil.read(path, 0L), Charset.forName("UTF-8")).replaceAll("\r", "").split("\n");
			boolean found = false;
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].trim().startsWith("server-id=")) {
					found = true;
					lines[i] = "server-id=" + userId;
				}
			}
			if (!found) {
				ArrayList<String> temp = new ArrayList<String>(Arrays.asList(lines));
				temp.add("server-id=" + userId);
				lines = temp.toArray(new String[0]);
			}
			
			FileUtil.erase(path);
			FileUtil.write(path, toBytes(String.join(FileUtil.LINE_SEPARATOR, lines), Charset.forName("UTF-8")), 0L);
			FileUtil.close(path);
		} catch (Exception ex) {
			
		}
	}
	
	private byte[] toBytes(String input, Charset enc) {
		return input.getBytes(enc);
	}
	private String toString(byte[] input, Charset enc) {
		return new String(input, enc);
	}
}
