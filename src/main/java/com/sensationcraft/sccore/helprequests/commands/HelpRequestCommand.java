package com.sensationcraft.sccore.helprequests.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.helprequests.HelpRequest;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());

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
                SCPlayer scTarget = scPlayerManager.getSCPlayer(target.getUniqueId());

                HelpRequest request = helpRequestManager.getRequest(target.getUniqueId());
                FancyMessage message;
                if (request == null) {
                    message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then(" §chas not sent a helprequest.", true);
                    message.send(sender);
                    return false;
                }

                message = new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText());
                FancyMessage toTarget = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText());
                if (args[0].equalsIgnoreCase("accept")) {
                    message = message.then(" §7has accepted ", true);
                    toTarget = toTarget.then(" §ahas accepted ", true);

                } else {
                    message = message.then(" §7has denied ", true);
                    toTarget = toTarget.then(" §chas denied ", true);
                }

                message.then(scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§7's helprequest.", true);
                toTarget.then("your helprequest.", true);
                scPlayerManager.staff(message);
                toTarget.send(target);
                helpRequestManager.removeRequest(target.getUniqueId());
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {

                if (helpRequestManager.getRequests().size() == 0) {
                    sender.sendMessage("§cNo helprequests were found at this current time.");
                    return false;
                }

                sender.sendMessage("§aHelpRequests:");
                for (HelpRequest helpRequest : helpRequestManager.getRequests().values()) {

                    Player target = Bukkit.getPlayer(helpRequest.getCreator());
                    SCPlayer scTarget = scPlayerManager.getSCPlayer(target.getUniqueId());

                    FancyMessage message = new FancyMessage("  §f§l* ").then(scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§b: " + helpRequest.getMessage(), true);
                    message.send(sender);
                }
                return true;
            } else {
                sender.sendMessage(usage);
                return false;
            }
        }

        if (helpRequestManager.getRequest(player.getUniqueId()) != null) {
            sender.sendMessage("§cYou have already sent in a helprequest. Please wait for a staff member to respond.");
            return false;
        }

        StringBuilder sb = new StringBuilder();

        for (String arg : args) {
            sb.append(arg).append(" ");
        }

        helpRequestManager.addRequest(new HelpRequest(player.getUniqueId(), sb.toString()));
        sender.sendMessage("§aYour help request has been sent to the staff team. Please wait for a response.");

        FancyMessage message = new FancyMessage("§9[STAFF] §7A helprequest has been receieved from ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§7.", true);
        scPlayerManager.staff(message);
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
