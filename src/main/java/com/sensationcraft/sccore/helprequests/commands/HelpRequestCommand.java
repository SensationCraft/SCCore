package com.sensationcraft.sccore.helprequests.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.helprequests.HelpRequest;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 1/18/16.
 */
public class HelpRequestCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private HelpRequestManager helpRequestManager;

	public HelpRequestCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.helpRequestManager = instance.getHelpRequestManager();
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou must be a player in order to perform this command.");
			return false;
		}

		Player player = (Player) sender;
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		if (!sender.hasPermission("sccore.helprequest")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: " +
				"§c/helprequest <message>";

		if (sender.hasPermission("sccore.helprequest.admin")) {
			usage += "\n         /helperequest accept <player>\n" +
					"         /helprequest deny <player>\n" +
					"         /helprequest list";
		}

		if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
			sender.sendMessage(usage);
			return false;
		}

		if (sender.hasPermission("sccore.helprequest.admin")) {
			if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {

				Player target = Bukkit.getPlayer(args[1]);
				if (target == null) {
					sender.sendMessage("§cNo player online with the given name found.");
					return false;
				}
				SCPlayer scTarget = this.scPlayerManager.getSCPlayer(target.getUniqueId());

				HelpRequest request = this.helpRequestManager.getRequest(target.getUniqueId());
				FancyMessage message;
				if (request == null) {
					message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then(" has not sent a helprequest.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				message = new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText());
				FancyMessage toTarget = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText());
				if (args[0].equalsIgnoreCase("accept")) {
					message = message.then(" has accepted ").color(ChatColor.GRAY);
					toTarget = toTarget.then(" has accepted ").color(ChatColor.GREEN);

				} else {
					message = message.then(" has denied ").color(ChatColor.GRAY);
					toTarget = toTarget.then(" has denied ").color(ChatColor.RED);
				}

				message.then(scTarget.getTag()).tooltip(scTarget.getHoverText()).then("'s helprequest.").color(ChatColor.GRAY);
				toTarget.then("your helprequest.").color(ChatColor.GRAY);
				this.scPlayerManager.staff(message);
				toTarget.send(target);
				this.helpRequestManager.removeRequest(target.getUniqueId());
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {

				if (this.helpRequestManager.getRequests().size() == 0) {
					sender.sendMessage("§cNo helprequests were found at this current time.");
					return false;
				}

				sender.sendMessage("§aHelpRequests:");
				for (HelpRequest helpRequest : this.helpRequestManager.getRequests().values()) {

					Player target = Bukkit.getPlayer(helpRequest.getCreator());
					SCPlayer scTarget = this.scPlayerManager.getSCPlayer(target.getUniqueId());

					FancyMessage message = new FancyMessage("  * ").color(ChatColor.WHITE).style(ChatColor.BOLD).then(scTarget.getTag())
							.tooltip(scTarget.getHoverText()).then(": " + helpRequest.getMessage()).color(ChatColor.AQUA);
					message.send(sender);
				}
				return true;
			} else {
				sender.sendMessage(usage);
				return false;
			}
		}

		if (this.helpRequestManager.getRequest(player.getUniqueId()) != null) {
			sender.sendMessage("§cYou have already sent in a helprequest. Please wait for a staff member to respond.");
			return false;
		}

		StringBuilder sb = new StringBuilder();

		for (String arg : args) {
			sb.append(arg).append(" ");
		}

		this.helpRequestManager.addRequest(new HelpRequest(player.getUniqueId(), sb.toString()));
		sender.sendMessage("§aYour help request has been sent to the staff team. Please wait for a response.");

		FancyMessage message = new FancyMessage("[STAFF] ").color(ChatColor.BLUE).then("A helprequest has been receieved from ").color(ChatColor.GRAY)
				.then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(".").color(ChatColor.GRAY);
		this.scPlayerManager.staff(message);
		return true;
	}

	public int getMinArgs(String subcommand) {
		switch (subcommand.toLowerCase()) {
		case "accept":
			return 2;
		case "deny":
			return 2;
		case "list":
			return 1;
		default:
			return 1;
		}
	}
}
