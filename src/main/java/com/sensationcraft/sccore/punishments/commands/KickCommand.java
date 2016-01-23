package com.sensationcraft.sccore.punishments.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by Anml on 1/7/16.
 */
public class KickCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private RankManager rankManager;
	private PunishmentManager punishmentManager;

	public KickCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.rankManager = instance.getRankManager();
		this.punishmentManager = instance.getPunishmentManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.kick")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/kick <player> <reason>";

		if (args.length < 2) {
			sender.sendMessage(usage);
			return false;
		}

		Player player = Bukkit.getPlayer(args[0]);

		if (player == null) {
			sender.sendMessage("§cNo player online with the given name found.");
			return false;
		}

		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		List<Punishment> punishments = this.punishmentManager.getPunishments(player.getUniqueId());

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			if (i != args.length - 1)
				sb.append(args[i] + " ");
			else
				sb.append(args[i]);
		}

		String reason = sb.toString();
		UUID creator = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

		if(creator != null) {
			if (rankManager.getRank(creator).getId() <= rankManager.getRank(player.getUniqueId()).getId()) {
				sender.sendMessage("§cYou are not permitted to kick a player that possesses the " + rankManager.getRank(player.getUniqueId()).getName() + " §crank.");
				return false;
			}
		}

		Punishment kick = new Punishment(PunishmentType.KICK, player.getUniqueId(), creator, 0, reason);
		this.punishmentManager.addPunishment(kick);

		boolean hover = sender instanceof Player ? true : false;
		FancyMessage message = new FancyMessage("§9[STAFF] ");

		if (hover) {
			SCPlayer senderSCPlayer = this.scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
			message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then(" §7has kicked ", true).then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §7with reason: §a" + reason + "§7.", true);
		} else {
			message = message.then("§6Console §7has kicked ", true).then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §7with reason: §a" + reason + "§7.", true);
		}

		this.scPlayerManager.staff(message);

		player.kickPlayer(kick.getMessage());

		return true;
	}
}
