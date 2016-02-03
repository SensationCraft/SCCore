package com.sensationcraft.sccore.help.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.help.TutorialManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Anml on 2/2/16.
 */
public class HelpCommand implements CommandExecutor {

    private SCCore instance;
    private TutorialManager tutorialManager;

    public HelpCommand(SCCore instance) {
        this.instance = instance;
        tutorialManager = instance.getTutorialManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only accessible to players.");
            return false;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("sccore.help")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        if (args.length == 0) {
            player.openInventory(tutorialManager.getInventory());
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != args.length - 1)
                sb.append(args[i] + " ");
            else
                sb.append(args[i]);
        }

        player.performCommand("essentials:help " + sb.toString());
        return true;
    }
}
