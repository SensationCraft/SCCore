package com.sensationcraft.sccore.punishments.commands;

import java.util.List;

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
public class UnmuteCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager iPlayerManager;
	private PunishmentManager punishmentManager;

	public UnmuteCommand(SCCore instance) {
		this.instance = instance;
		this.iPlayerManager = instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.unmute")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/unmute <player>";

		if (args.length < 1) {
			sender.sendMessage(usage);
			return false;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

		if (offlinePlayer == null) {
			sender.sendMessage("§cNo player with the given name found.");
			return false;
		}

		SCPlayer scPlayer = this.iPlayerManager.getSCPlayer(offlinePlayer.getUniqueId());

		List<Punishment> punishments = this.punishmentManager.getPunishments(offlinePlayer.getUniqueId());

		for (Punishment punishment : punishments) {
			if (punishment.getType().equals(PunishmentType.MUTE) || punishment.getType().equals(PunishmentType.TEMPMUTE)) {
				if (!punishment.hasExpired()) {
					punishment.setExpires(0L);
					punishment.execute();

					String sName = !(sender instanceof Player) ? "§6Console" : this.iPlayerManager.getSCPlayer(((Player) sender).getUniqueId()).getTag();
					this.iPlayerManager.staff("§9[STAFF] " + sName + " §7has unmuted " + scPlayer.getTag() + "§7.");
					return true;
				}
			}
		}

		sender.sendMessage("§cThe target player is currently not muted.");
		return false;
	}
}