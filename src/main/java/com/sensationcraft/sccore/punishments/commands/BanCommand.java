package com.sensationcraft.sccore.punishments.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;

/**
 * Created by Anml on 1/7/16.
 */
public class BanCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private PunishmentManager punishmentManager;

	public BanCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.ban")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/ban <player> <reason>";

		if (args.length < 2) {
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

		synchronized (punishments) {
			for (Punishment punishment : punishments) {
				if (punishment.getType().equals(PunishmentType.BAN)
						|| punishment.getType().equals(PunishmentType.TEMPBAN)) {
					if (!punishment.hasExpired()) {
						sender.sendMessage("§cThe target player is already banned.");
						return false;
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			if (i != args.length - 1)
				sb.append(args[i] + " ");
			else
				sb.append(args[i]);
		}

		String reason = sb.toString();
		UUID creator = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

		Punishment ban = new Punishment(PunishmentType.BAN, offlinePlayer.getUniqueId(), creator, -1, reason);
		this.punishmentManager.addPunishment(ban);

		String sName = creator == null ? "§6Console" : this.scPlayerManager.getSCPlayer(creator).getTag();
		this.scPlayerManager.staff("§9[STAFF] " + sName + " §7has banned " + scPlayer.getTag() + " §7with reason: §a" + reason + "§7.");

		if (offlinePlayer.isOnline()) {
			offlinePlayer.getPlayer().kickPlayer(ban.getMessage());
		}

		return true;
	}
}
