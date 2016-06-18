package me.egg82.hme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Timer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mcstats.Metrics;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.BasePlugin;
import ninja.egg82.registry.Registry;
import ninja.egg82.utils.Util;

import me.egg82.hme.commands.HatCommand;
import me.egg82.hme.commands.UnhatCommand;
import me.egg82.hme.enums.PermissionsType;
import me.egg82.hme.enums.PluginServiceType;
import me.egg82.hme.events.PlayerKickEventCommand;
import me.egg82.hme.events.PlayerMoveEventCommand;
import me.egg82.hme.events.PlayerQuitEventCommand;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HatMeEnhanced extends BasePlugin {
	//vars
	private Timer updateTimer = null;
	
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
		
		updateTimer = new Timer(24 * 60 * 60 * 1000, onUpdateTimer);
	}
	@SuppressWarnings("deprecation")
	public void onEnable() {
		super.onEnable();
		
		try {
			Metrics m = new Metrics(this);
			m.start();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		checkUpdate();
		updateTimer.setRepeats(true);
		updateTimer.start();
		
		commandHandler.addCommand("hat", HatCommand.class);
		commandHandler.addCommand("unhat", UnhatCommand.class);
		
		eventListener.addEvent(PlayerQuitEvent.class, PlayerQuitEventCommand.class);
		eventListener.addEvent(PlayerKickEvent.class, PlayerKickEventCommand.class);
		eventListener.addEvent(PlayerMoveEvent.class, PlayerMoveEventCommand.class);
		
		Object[] enums = Util.getStaticFields(PermissionsType.class);
		String[] permissions = Arrays.copyOf(enums, enums.length, String[].class);
		for (String p : permissions) {
			permissionsManager.addPermission(p);
		}
		
		enums = Util.getStaticFields(Material.class);
		Material[] materials = Arrays.copyOf(enums, enums.length, Material[].class);
		for (Material m : materials) {
			if (m != null) {
				permissionsManager.addPermission("hme.hat." + Integer.toString(m.getId()));
				permissionsManager.addPermission("hme.hat." + m.toString().toLowerCase());
			}
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Enabled" + ChatColor.GREEN + " ==--");
	}
	public void onDisable() {
		commandHandler.clearCommands();
		eventListener.clearEvents();
		permissionsManager.clearPermissions();
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "HatMeEnhanced Disabled" + ChatColor.GREEN + " ==--");
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
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "--== " + ChatColor.GREEN + "HatMeEnhanced UPDATE AVAILABLE" + ChatColor.YELLOW + " ==--");
		}
	}
}
