package com.sensationcraft.sccore.chat.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.help.TutorialManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Anml on 1/7/16.
 */
public class StaffCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private TutorialManager tutorialManager;

	public StaffCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.tutorialManager = instance.getTutorialManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!sender.hasPermission("sccore.staff")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		if (sender instanceof Player && tutorialManager.getTutorialedPlayers().containsKey(((Player) sender).getUniqueId())) {
			sender.sendMessage("§cYou are not permitted to talk in staff chat while in tutorial mode");
			return false;
		}

		String usage = "§4Usage: §c/staff <message>";

		if (args.length == 0) {
			sender.sendMessage(usage);
			return false;
		}


		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i != args.length - 1)
				sb.append(args[i] + " ");
			else
				sb.append(args[i]);
		}

		boolean hover = sender instanceof Player ? true : false;
		FancyMessage message = new FancyMessage("[STAFF] ").color(ChatColor.BLUE);

		if (hover) {
			SCPlayer senderSCPlayer = this.scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
			message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then(": ").color(ChatColor.WHITE)
					.then(sb.toString()).color(ChatColor.YELLOW).style(ChatColor.BOLD);
		} else {
			message = message.then("Console").color(ChatColor.GOLD).then(": ").color(ChatColor.WHITE).then(sb.toString()).color(ChatColor.YELLOW).style(ChatColor.BOLD);
		}

		this.scPlayerManager.staff(message);

		return true;
	}
}
