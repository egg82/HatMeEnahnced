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
import ninja.egg82.plugin.utils.SpigotReflectUtil;
import ninja.egg82.utils.ReflectUtil;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.util.LightAPIHelper;
import me.egg82.hme.util.NullLightHelper;
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
			@SuppressWarnings("unused")
			Metrics m = new Metrics(this);
		} catch (Exception ex) {
			
		}
		
		numCommands = SpigotReflectUtil.addCommandsFromPackage(commandHandler, "me.egg82.hme.commands");
		numEvents = SpigotReflectUtil.addEventsFromPackage(eventListener, "me.egg82.hme.events");
		numPermissions = SpigotReflectUtil.addPermissionsFromClass(permissionsManager, PermissionsType.class);
		numTicks = SpigotReflectUtil.addTicksFromPackage(tickHandler, "me.egg82.hme.ticks");
		
		Object[] enums = ReflectUtil.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			if (m != null) {
				permissionsManager.addPermission("hme.hat." + Integer.toString(m.getId()));
				permissionsManager.addPermission("hme.hat." + m.toString().toLowerCase());
			}
		}
		
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
			int[] latest = parseVersion(updater.getLatestName());
			int[] current = parseVersion(getDescription().getVersion());
			
			for (int i = 0; i < Math.min(latest.length, current.length); i++) {
				if (latest[i] < current[i]) {
					return;
				}
			}
			
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "--== " + ChatColor.YELLOW + "HatMeEnhanced UPDATE AVAILABLE (Latest: " + versionToString(latest) + " Current: " + versionToString(current) + ") " + ChatColor.GREEN + " ==--");
		}
	}
	private int[] parseVersion(String name) {
		int versionIndex = Math.max(0, name.lastIndexOf('v') + 1);
		String versionString = name.substring(versionIndex);
		
		int firstDot = versionString.indexOf('.');
		int middleDot = versionString.indexOf('.', firstDot + 1);
		int lastDot = versionString.lastIndexOf('.');
		
		int first = Integer.parseInt(versionString.substring(0, firstDot));
		int middle = Integer.parseInt(versionString.substring(firstDot + 1, middleDot));
		int last = Integer.parseInt(versionString.substring(lastDot + 1));
		
		return new int[] {first, middle, last};
	}
	private String versionToString(int[] version) {
		String retVal = "";
		for (int i = 0; i < version.length; i++) {
			retVal += version[i] + ".";
		}
		retVal = retVal.substring(0, retVal.length() - 1);
		
		return retVal;
	}
	
	private void enableMessage(ConsoleCommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + " _   _       _  ___  ___     _____      _                              _ ");
		sender.sendMessage(ChatColor.YELLOW + "| | | |     | | |  \\/  |    |  ___|    | |                            | |");
		sender.sendMessage(ChatColor.YELLOW + "| |_| | __ _| |_| .  . | ___| |__ _ __ | |__   __ _ _ __   ___ ___  __| |");
		sender.sendMessage(ChatColor.YELLOW + "|  _  |/ _` | __| |\\/| |/ _ |  __| '_ \\| '_ \\ / _` | '_ \\ / __/ _ \\/ _` |");
		sender.sendMessage(ChatColor.YELLOW + "| | | | (_| | |_| |  | |  __| |__| | | | | | | (_| | | | | (_|  __| (_| |");
		sender.sendMessage(ChatColor.YELLOW + "\\_| |_/\\__,_|\\__\\_|  |_/\\___\\____|_| |_|_| |_|\\__,_|_| |_|\\___\\___|\\__,_|");
		sender.sendMessage(ChatColor.GREEN + "[Version " + getDescription().getVersion() + "] " + ChatColor.RED + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.WHITE + numPermissions + " permissions " + ChatColor.YELLOW + numTicks + " tick handlers");
		sender.sendMessage(ChatColor.WHITE + "[HatMeEnhanced] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + initReg.getRegister("game.version"));
	}
	private void disableMessage(ConsoleCommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
	}
}
