package org.sensationcraft.sccore.ranks.commands;

import com.earth2me.essentials.User;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sensationcraft.sccore.SCCore;
import org.sensationcraft.sccore.ranks.PermissionsManager;
import org.sensationcraft.sccore.ranks.Rank;
import org.sensationcraft.sccore.ranks.RankManager;
import org.sensationcraft.sccore.scplayer.SCPlayer;
import org.sensationcraft.sccore.scplayer.SCPlayerManager;
import org.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 12/28/15.
 */
public class RankCommand implements CommandExecutor {

    SCCore instance;
    SCPlayerManager scPlayerManager;
    RankManager rankManager;
    PermissionsManager permissionsManager;

    public RankCommand(SCCore instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        rankManager = instance.getRankManager();
        permissionsManager = instance.getPermissionsManager();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (!sender.hasPermission("sccore.rank")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/rank set <player> <rank>\n         /rank get <player>";

        if (args.length == 0 || args.length < getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        OfflinePlayer player = instance.getServer().getOfflinePlayer(args[1]);

        if (player != null) {
            User user = instance.getEssentials().getOfflineUser(args[1]);

            if (user == null) {
                sender.sendMessage("§cNo player with the given name found.");
                return false;
            }

            SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());

            if (args[0].equalsIgnoreCase("set")) {

                Rank rank = translateRank(args[2]);

                if (rank == null) {
                    sender.sendMessage("§cNo rank with the given name found.");
                    return false;
                }

                boolean hover = sender instanceof Player ? true : false;
                FancyMessage message = new FancyMessage("§9[STAFF] ");

                if (hover) {
                    SCPlayer senderSCPlayer = scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
                    message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then(" §7has " +
                            "set ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§7's rank to " + rank
                            .getName() + "§7.");
                } else {
                    message = message.then("§6Console §7has set ").then(scPlayer.getTag()).tooltip(scPlayer
                            .getHoverText()).then("§7's rank to " + rank
                            .getName() + "§7.");
                }

                scPlayerManager.staff(message);
                rankManager.setRank(player.getUniqueId(), rank);
                instance.getLogger().info(sender.getName() + " has set " + player.getName() + "'s rank to " + rank.name());
                permissionsManager.updateAttachment(player.getUniqueId());
                return true;
            } else if (args[0].equalsIgnoreCase("get")) {
                FancyMessage message = new FancyMessage("§aThe player ").then("§a" + player.getName()).tooltip(scPlayer
                        .getHoverText()).then("§a's rank is " + rankManager.getRank(player.getUniqueId()).getName() + "§a.");
                message.send(sender);
                return true;
            } else {
                sender.sendMessage(usage);
                return false;
            }
        } else {
            sender.sendMessage("§cNo player with the given name found.");
            return false;
        }
    }

    public Rank translateRank(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.name().equalsIgnoreCase(name) || rank.getAlias().equalsIgnoreCase(name)) {
                return rank;
            }
        }

        return null;
    }

    public int getMinArgs(String subcommand) {
        switch (subcommand.toLowerCase()) {
            case "set":
                return 3;
            case "get":
                return 2;
            default:
                return 100;
        }
    }
}