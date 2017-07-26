package me.egg82.hme.exceptions;

import org.bukkit.command.CommandSender;

import ninja.egg82.plugin.exceptions.InvalidPermissionsException;

public class InvalidPermissionsHatTypeException extends InvalidPermissionsException {
	//vars
	public static final InvalidPermissionsHatTypeException EMPTY = new InvalidPermissionsHatTypeException(null, null);
	private static final long serialVersionUID = 400082895891889765L;

	//constructor
	public InvalidPermissionsHatTypeException(CommandSender sender, String permissionsType) {
		super(sender, permissionsType);
	}
	
	//public
	
	//private
	
}
