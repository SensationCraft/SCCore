package org.sensationcraft.sccore.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sensationcraft.sccore.SCCore;
import org.sensationcraft.sccore.scplayer.SCPlayer;
import org.sensationcraft.sccore.scplayer.SCPlayerManager;
import org.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 12/28/15.
 */
public class ShoutCommand implements CommandExecutor {

	SCCore instance;
	SCPlayerManager scPlayerManager;

	public ShoutCommand(SCCore instance) {
		this.instance = instance;
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

			if (scPlayer.isShoutCooldowned()) {
				sender.sendMessage("§cYou must wait a minimum of 15 seconds between shouts.");
				return false;
			}

			if (!sender.hasPermission("sccore.shout.bypasscooldown")) {
				scPlayer.shoutCooldown();
			}

			FancyMessage message = new FancyMessage("§c[S] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()
					).then("§f§l: " + msg);

			for (Player player : Bukkit.getOnlinePlayers()) {
				message.send(player);
			}
			return true;
		} else {
			Bukkit.broadcastMessage("§c[S] §6Console§f§l: " + msg);
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
