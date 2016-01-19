package com.sensationcraft.sccore.chat.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 12/28/15.
 */
public class ShoutCommand implements CommandExecutor {

	SCCore instance;
	SCPlayerManager scPlayerManager;
	PunishmentManager punishmentManager;

	public ShoutCommand(SCCore instance) {
		this.instance = instance;
		this.punishmentManager = instance.getPunishmentManager();
		this.scPlayerManager = instance.getSCPlayerManager();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

		if (!sender.hasPermission("sccore.shout")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/shout <message>";

		if (args.length == 0) {
			sender.sendMessage(usage);
			return false;
		}

		String msg = sender.isOp() ? this.getMessage(args).replace('&', ChatColor.COLOR_CHAR) : this.getMessage(args);

		if (sender instanceof Player) {
			SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());

			List<Punishment> punishments = this.punishmentManager.getPunishments(((Player) sender).getUniqueId());

			synchronized (punishments) {
				for (Punishment punishment : punishments) {
					if (punishment.getType().equals(PunishmentType.MUTE)) {
						if (!punishment.hasExpired()) {
							sender.sendMessage("§cYou are permanently muted.");
							return false;
						}
					}

					if (punishment.getType().equals(PunishmentType.TEMPMUTE)) {
						if (!punishment.hasExpired()) {
							sender.sendMessage(
									"§cYou are temporarily muted until §3" + punishment.getEndTimestamp() + " §c.");
							return false;
						}
					}
				}
			}
			if (scPlayer.isShoutCooldowned()) {
				sender.sendMessage("§cYou must wait a minimum of 15 seconds between shouts.");
				return false;
			}

			if (!sender.hasPermission("sccore.shout.bypasscooldown")) {
				scPlayer.shoutCooldown();
			}

			FancyMessage message = new FancyMessage("§c[S] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()
					).then("§f: §l" + msg, true);
			//this.instance.getLogger().info(message.toJSONString());
			this.scPlayerManager.broadcast(message);
			return true;
		} else {
			Bukkit.broadcastMessage("§c[S] §6Console§f: §l" + msg);
			return true;
		}
	}

	public String getMessage(String[] args) {

		StringBuilder sb = new StringBuilder();

		for (String arg : args) {
			sb.append(arg).append(" ");
		}
		return sb.toString();
	}
}
