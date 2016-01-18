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
import com.sensationcraft.sccore.utils.Utils;

/**
 * Created by Anml on 1/7/16.
 */
public class TempmuteCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private PunishmentManager punishmentManager;
	private Utils utils;

	public TempmuteCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
		this.utils = instance.getUtils();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.tempmute")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/tempmute <player> <length> <reason>";

		if (args.length < 3) {
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
			if (punishment.getType().equals(PunishmentType.MUTE) || punishment.getType().equals(PunishmentType.TEMPMUTE)) {
				if (!punishment.hasExpired()) {
					sender.sendMessage("§cThe target player is already muted.");
					return false;
				}
			}
		}

		long length = this.utils.longLength(args[1]);

		if (length == 0) {
			sender.sendMessage("§cYou must enter a correct length.");
			return false;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			if (i != args.length - 1)
				sb.append(args[i] + " ");
			else
				sb.append(args[i]);
		}

		String reason = sb.toString();
		UUID creator = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

		Punishment tempmute = new Punishment(PunishmentType.TEMPMUTE, offlinePlayer.getUniqueId(), creator, length, reason);
		this.punishmentManager.addPunishment(tempmute);

		String sName = creator == null ? "§6Console" : this.scPlayerManager.getSCPlayer(creator).getTag();
		this.scPlayerManager.staff("§9[STAFF] " + sName + " §7has temp-muted " + scPlayer.getTag() + " §7for §3" + this.utils.actualLength(args[1]) + " §7with reason: §a" + reason + "§7.");

		return true;
	}
}