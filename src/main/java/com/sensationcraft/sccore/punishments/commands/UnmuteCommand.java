package com.sensationcraft.sccore.punishments.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Anml on 1/7/16.
 */
public class UnmuteCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private PunishmentManager punishmentManager;

	public UnmuteCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
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

		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(offlinePlayer.getUniqueId());

		List<Punishment> punishments = this.punishmentManager.getPunishments(offlinePlayer.getUniqueId());

		synchronized (punishments) {
			for (Punishment punishment : punishments) {
				if (punishment.getType().equals(PunishmentType.MUTE)
						|| punishment.getType().equals(PunishmentType.TEMPMUTE)) {
					if (!punishment.hasExpired()) {
						punishment.setExpires(0L);
						punishment.execute();

						boolean hover = sender instanceof Player ? true : false;
						FancyMessage message = new FancyMessage("§9[STAFF] ");

						if (hover) {
							SCPlayer senderSCPlayer = this.scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
							message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then(" has unmuted ").color(ChatColor.GRAY)
									.then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(".").color(ChatColor.GRAY);
						} else {
							message = message.then("Console").color(ChatColor.GOLD).then(" has unmuted ").color(ChatColor.GRAY).then(scPlayer.getTag())
									.tooltip(scPlayer.getHoverText()).then(".").color(ChatColor.GRAY);
						}

						this.scPlayerManager.staff(message);
						return true;
					}
				}
			}
		}
		sender.sendMessage("§cThe target player is currently not muted.");
		return false;
	}
}
