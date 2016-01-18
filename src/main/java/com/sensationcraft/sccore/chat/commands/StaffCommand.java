package com.sensationcraft.sccore.chat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.Main;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 1/7/16.
 */
public class StaffCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;
    private PunishmentManager punishmentManager;

    public StaffCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        punishmentManager = instance.getPunishmentManager();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.staff")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
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
        FancyMessage message = new FancyMessage("§9[STAFF] ");

        if (hover) {
            SCPlayer senderSCPlayer = scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
            message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then("§f: §e§l" + sb);
        } else {
            message = message.then("§6Console").then("§f: §e§l" + sb);
        }

        scPlayerManager.staff(message);

        return true;
    }
}
