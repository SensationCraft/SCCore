package com.sensationcraft.sccore.xrayspy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.xrayspy.XrayManager;

/**
 * Created by Anml on 1/24/16.
 */
public class XraySpyCommand implements CommandExecutor {

	private SCCore instance;
	private XrayManager xrayManager;

	public XraySpyCommand(SCCore instance) {
		this.instance = instance;
		this.xrayManager = instance.getXrayManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command is only accessible to players.");
		}

		Player player = (Player) sender;

		if (!sender.hasPermission("sccore.xrayspy")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		if (this.xrayManager.getXraySpyers().contains(player)) {
			this.xrayManager.getXraySpyers().remove(player);
			sender.sendMessage("§cYou have disabled xray spy.");
			return false;
		} else {
			this.xrayManager.getXraySpyers().add(player);
			sender.sendMessage("§aYou have enabled xray spy.");
			return true;
		}
	}
}
