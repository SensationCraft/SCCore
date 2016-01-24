package com.sensationcraft.sccore.punishments.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.Utils;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.punishmentManager = instance.getPunishmentManager();
        this.scPlayerManager = instance.getSCPlayerManager();
        this.utils = instance.getUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.history")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/history full <player>\n         /history type <player> <type>\n" +
                "         /history count <player>";

        if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        OfflinePlayer player = this.instance.getServer().getOfflinePlayer(args[1]);

        if (player != null) {

            SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());
            List<Punishment> punishments = this.punishmentManager.getPunishments(player.getUniqueId());

            if (punishments.size() == 0) {
                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §ccurrently has no punishments at this time.", true);
                message.send(sender);
                return false;
            }

            if (args[0].equalsIgnoreCase("count")) {
                int ban = 0;
                int tempban = 0;
                int mute = 0;
                int tempmute = 0;
                int kick = 0;
                int warn = 0;

                synchronized (punishments) {
                    for (Punishment punishment : punishments) {
                        if (punishment.getType().equals(PunishmentType.BAN))
                            ban++;
                        else if (punishment.getType().equals(PunishmentType.TEMPBAN))
                            tempban++;
                        else if (punishment.getType().equals(PunishmentType.MUTE))
                            mute++;
                        else if (punishment.getType().equals(PunishmentType.TEMPMUTE))
                            tempmute++;
                        else if (punishment.getType().equals(PunishmentType.KICK))
                            kick++;
                        else {
                            warn++;
                        }
                    }
                }

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a's Punishment Count:", true);
                message.send(sender);
                sender.sendMessage("§f  - §aBan Count: §6" + ban);
                sender.sendMessage("§f  - §aTempban Count: §6" + tempban);
                sender.sendMessage("§f  - §aMute Count: §6" + mute);
                sender.sendMessage("§f  - §aTempmute Count: §6" + tempmute);
                sender.sendMessage("§f  - §aKick Count: §6" + kick);
                sender.sendMessage("§f  - §aWarning Count: §6" + warn);

                return true;
            } else if (args[0].equalsIgnoreCase("full")) {
                List<Long> createdTimes = new ArrayList<>();
                synchronized (punishments) {
                    for (Punishment punishment : punishments)
                        createdTimes.add(punishment.getCreated());
                }
                Collections.sort(createdTimes, Collections.reverseOrder());

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a's Punishments:", true);
                message.send(sender);
                synchronized (punishments) {
                    for (long l : createdTimes) {
                        for (Punishment punishment : punishments) {
                            if (punishment.getCreated() == l) {
                                String info = "§6[" + this.utils.getDateStamp(punishment.getCreated()) + "] §f" + punishment.getReason() + " §4§l[" + punishment.getType().name() + "]";
                                if (punishment.hasExpired() && !(punishment.getType().equals(PunishmentType.WARNING) || punishment.getType().equals(PunishmentType.KICK)))
                                    info += " §8[EXPIRED]";
                                FancyMessage msg = new FancyMessage("§f  - ").then(info).tooltip(punishment.getHoverText()).then("§7", true);
                                msg.send(sender);
                            }
                        }
                    }
                }
                return true;
            } else {
                PunishmentType type;

                try {
                    type = PunishmentType.valueOf(args[2].toUpperCase());
                } catch (Exception e) {
                    sender.sendMessage("§cYou have entered an invalid punishment type.");
                    return false;
                }

                List<Long> createdTimes = new ArrayList<>();
                synchronized (punishments) {
                    for (Punishment punishment : punishments)
                        if (punishment.getType().equals(type))
                            createdTimes.add(punishment.getCreated());
                }

                if (createdTimes.size() == 0) {
                    FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §ccurrently has no " + type.name().toLowerCase() + "punishments at this time.", true);
                    message.send(sender);
                    return false;
                }
                Collections.sort(createdTimes, Collections.reverseOrder());

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a's " + type.name() + " Punishments: ", true);
                message.send(sender);
                synchronized (punishments) {
                    for (long l : createdTimes) {
                        for (Punishment punishment : punishments) {
                            if (punishment.getType().equals(type))
                                if (punishment.getCreated() == l) {
                                    String info = "§6[" + this.utils.getDateStamp(punishment.getCreated()) + "] §f" + punishment.getReason() + " §4§l[" + punishment.getType().name() + "]";
                                    if (punishment.hasExpired() && !(punishment.getType().equals(PunishmentType.WARNING) || punishment.getType().equals(PunishmentType.KICK)))
                                        info += " §8[EXPIRED]";
                                    FancyMessage msg = new FancyMessage("§f  - ").then(info).tooltip(punishment.getHoverText()).then("§7", true);
                                    msg.send(sender);
                                }
                        }
                    }
                }
            }
        } else {
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
            case "count":
                return 2;
            default:
                return 100;
        }
    }

}
