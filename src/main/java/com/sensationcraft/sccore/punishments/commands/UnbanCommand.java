package com.sensationcraft.sccore.punishments.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.Main;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;

/**
 * Created by Anml on 1/7/16.
 */
public class UnbanCommand implements CommandExecutor {

	private Main instance;
	private SCPlayerManager scPlayerManager;
	private PunishmentManager punishmentManager;

	public UnbanCommand(Main instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.unban")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/unban <player>";

		if (args.length < 1) {
			sender.sendMessage(usage);
			return false;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

		if (offlinePlayer == null) {
			sender.sendMessage("§cNo player with the given name found.");
			return false;
		}

		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(offlinePlayer.getUniqueId());

		List<Punishment> punishments = this.punishmentManager.getPunishments(offlinePlayer.getUniqueId());

		for (Punishment punishment : punishments) {
			if (punishment.getType().equals(PunishmentType.BAN) || punishment.getType().equals(PunishmentType.TEMPBAN)) {
				if (!punishment.hasExpired()) {
					punishment.setExpires(0L);
					punishment.execute();

					String sName = !(sender instanceof Player) ? "§6Console" : this.scPlayerManager.getSCPlayer(((Player) sender).getUniqueId()).getTag();
					this.scPlayerManager.staff("§9[STAFF] " + sName + " §7has unbanned " + scPlayer.getTag() + "§7.");
					return true;
				}
			}
		}

		sender.sendMessage("§cThe target player is currently not banned.");
		return false;
	}
}
