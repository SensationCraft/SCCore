package com.sensationcraft.sccore.punishments.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.Utils;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 1/19/16.
 */
public class HistoryCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private PunishmentManager punishmentManager;
	private Utils utils;

	public HistoryCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
		this.utils = instance.getUtils();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.history")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/history full <player>\n         /history type <player> <type>";

		if (args.length < this.getMinArgs(args[0])) {
			sender.sendMessage(usage);
			return false;
		}

		OfflinePlayer player = this.instance.getServer().getOfflinePlayer(args[1]);

		if (player != null) {

			SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());
			List<Punishment> punishments = this.punishmentManager.getPunishments(player.getUniqueId());

			if(punishments.size() == 0) {
				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §ccurrently has no punishments at this time.", true);
				message.send(sender);
				return false;
			}

			if(args[0].equalsIgnoreCase("full")) {
				List<Long> createdTimes = new ArrayList<>();
				synchronized (punishments) {
					for (Punishment punishment : punishments)
						createdTimes.add(punishment.getCreated());
				}
				Collections.sort(createdTimes, Collections.reverseOrder());

				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a's Punishments:", true);
				synchronized (punishments) {
					for (long l : createdTimes) {
						for (Punishment punishment : punishments) {
							if (punishment.getCreated() == l) {
								String info = "§3[" + this.utils.getDateStamp(punishment.getCreated()) + "]§7: §a" + punishment.getReason() + " §4[" + punishment.getType().name() + "]";
								if (punishment.hasExpired() && !(punishment.getType().equals(PunishmentType.WARNING) || punishment.getType().equals(PunishmentType.KICK)))
									info += " §8[EXPIRED]";
								FancyMessage msg = new FancyMessage(" §f- ").then(info).tooltip(punishment.getHoverText()).then("", true);
								msg.send(sender);
							}
						}
					}
				}
				return true;
			} else {
				PunishmentType type = PunishmentType.valueOf(args[2].toUpperCase());

				if(type == null) {
					sender.sendMessage("§cYou have entered an invalid punishment type.");
					return false;
				}

				List<Long> createdTimes = new ArrayList<>();
				synchronized (punishments) {
					for (Punishment punishment : punishments)
						if(punishment.getType().equals(type))
							createdTimes.add(punishment.getCreated());
				}
				Collections.sort(createdTimes, Collections.reverseOrder());

				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a's " + type.name() + " Punishments: " , true);
				synchronized (punishments) {
					for (long l : createdTimes) {
						for (Punishment punishment : punishments) {
							if(punishment.getType().equals(type))
								if (punishment.getCreated() == l) {
									String info = "§3[" + this.utils.getDateStamp(punishment.getCreated()) + "]§7: §a" + punishment.getReason() + " §4[" + punishment.getType().name() + "]";
									if (punishment.hasExpired() && !(punishment.getType().equals(PunishmentType.WARNING) || punishment.getType().equals(PunishmentType.KICK)))
										info += " §8[EXPIRED]";
									FancyMessage msg = new FancyMessage(" §f- ").then(info).tooltip(punishment.getHoverText()).then("", true);
									msg.send(sender);
								}
						}
					}
				}
			}
		}  else {
			sender.sendMessage("§cNo player with the given name found.");
			return false;
		}
		return false;
	}

	public int getMinArgs(String subcommand) {
		switch (subcommand.toLowerCase()) {
		case "full":
			return 2;
		case "type":
			return 3;
		default:
			return 100;
		}
	}
}
