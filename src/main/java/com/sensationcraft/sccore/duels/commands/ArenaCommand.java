package com.sensationcraft.sccore.duels.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.Main;
import com.sensationcraft.sccore.duels.Arena;
import com.sensationcraft.sccore.duels.ArenaLocationType;
import com.sensationcraft.sccore.duels.ArenaManager;

/**
 * Created by Anml on 1/3/16.
 */
public class ArenaCommand implements CommandExecutor {

	private Main instance;
	private ArenaManager arenaManager;
	private Arena arena;

	public ArenaCommand(Main instance) {
		this.instance = instance;
		this.arenaManager = instance.getArenaManager();
		this.arena = this.arenaManager.getArena();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {


		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou must be a player to execute this command.");
			return false;
		}

		Player player = (Player) sender;
		Location location = player.getLocation();

		if (!sender.hasPermission("sccore.arena")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: " +
				"§c/arena set <type>\n" +
				"         /arena forceend\n" +
				"         /arena tp <type>\n" +
				"         /arena loctypes";

		if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
			sender.sendMessage(usage);
			return false;
		}

		if (args[0].equalsIgnoreCase("set")) {

			ArenaLocationType type = null;

			for (ArenaLocationType t : ArenaLocationType.values()) {
				if (t.name().equalsIgnoreCase(args[1]))
					type = t;
			}

			if (type == null) {
				sender.sendMessage("§cYou must enter a valid location type.");
				return false;
			}

			if (this.arena.getWorld() != null) {
				if (!this.arena.getWorld().equals(location.getWorld())) {
					sender.sendMessage("§cYou must be in the same world as the previous locations.");
					return false;
				}
			}

			if (type.equals(ArenaLocationType.Spectate)) {
				sender.sendMessage("§aYou have set your location as the arena's §e" + type.name() + " §atype.");
				this.arena.setLocation(ArenaLocationType.Spectate, location);
				return true;
			}

			if (type != ArenaLocationType.PrimaryCorner && type != ArenaLocationType.SecondaryCorner) {
				if (this.arena.getLocation(ArenaLocationType.PrimaryCorner) == null || this.arena.getLocation(ArenaLocationType.SecondaryCorner) == null) {
					sender.sendMessage("§cYou must set the primary/secondary corners prior to setting other types.");
					return false;
				}

				boolean valid = this.arenaManager.insideBorders(location);

				if (!valid) {
					sender.sendMessage("§cYou must be in arena boundaries to set this location type.");
					return false;
				} else {
					this.arena.setLocation(type, location);
					sender.sendMessage("§aYou have set your location as the arena's §e" + type.name() + "§a location.");
					return true;
				}
			} else {
				this.arena.setLocation(type, location);
				sender.sendMessage("§aYou have set your location as the arena's §e" + type.name() + "§a location.");

				for (ArenaLocationType t : ArenaLocationType.values()) {
					if (t != ArenaLocationType.PrimaryCorner && t != ArenaLocationType.SecondaryCorner)
						if (this.arena.isValidLocation(t)) {
							boolean valid = this.arenaManager.insideBorders(this.arena.getLocation(t));
							if (!valid) {
								sender.sendMessage("§cThe location type §e" + t + " §chas been reset due to not being " +
										"within arena boundaries.");
								this.arena.setLocation(t, null);
							}
						}
				}

				return true;
			}
		} else if (args[0].equalsIgnoreCase("forceend")) {

			if (!this.arena.isRunning()) {
				sender.sendMessage("§cThere is no one currently dueling in the arena.");
				return false;
			}

			this.arena.forceEnd();
			sender.sendMessage("§aYou have force-ended the duel in the arena.");

		} else if (args[0].equalsIgnoreCase("tp")) {

			ArenaLocationType type = null;

			for (ArenaLocationType t : ArenaLocationType.values()) {
				if (t.name().equalsIgnoreCase(args[1]))
					type = t;
			}

			if (type == null) {
				sender.sendMessage("§cYou must enter a valid location type.");
				return false;
			}

			Location loc = this.arena.getLocation(type);
			if (loc == null) {
				sender.sendMessage("§cThere is no location set for the type §e" + type.name() + "§c.");
				return false;
			}

			player.teleport(loc);
			player.sendMessage("§aYou have been teleported to the location of the type §e" + type.name() + "§a.");
			return true;
		} else if (args[0].equalsIgnoreCase("loctypes")) {
			sender.sendMessage("§aArena Location Types:");
			for (ArenaLocationType type : ArenaLocationType.values()) {
				sender.sendMessage("§f   - §a" + type.name());
			}

			return true;
		} else {
			sender.sendMessage(usage);
			return false;
		}

		return false;
	}

	public int getMinArgs(String subcommand) {
		switch (subcommand) {
		case "set":
			return 2;
		case "forceend":
			return 1;
		case "tp":
			return 2;
		case "loctypes":
			return 1;
		default:
			return 100;
		}
	}

}
