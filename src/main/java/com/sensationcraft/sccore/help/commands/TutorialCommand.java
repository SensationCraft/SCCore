package com.sensationcraft.sccore.help.commands;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.help.TutorialManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Created by Anml on 2/1/16.
 */
public class TutorialCommand implements CommandExecutor {

    private SCCore instance;
    private TutorialManager tutorialManager;
    private SCPlayerManager scPlayerManager;

    public TutorialCommand(SCCore instance) {
        this.instance = instance;
        tutorialManager = instance.getTutorialManager();
        scPlayerManager = instance.getSCPlayerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {

        if (!sender.hasPermission("sccore.tutorial")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/tutorial setspawn\n" +
                "         /tutorial spawn\n" +
                "         /tutorial start <player>\n" +
                "         /tutorial remove <player>\n" +
                "         /tutorial listplayers";

        if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("spawn")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command is only accessible to players.");
                return false;
            }

            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("setspawn")) {
                tutorialManager.setSpawnLocation(player.getLocation());
                sender.sendMessage("§aYou have set your current location as the tutorial spawnpoint.");
                return true;
            }

            Location location = tutorialManager.getSpawnLocation();
            if (location == null) {
                sender.sendMessage("§cThe spawn location for the tutorial has not been set yet.");
                return false;
            }

            player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            sender.sendMessage("§cYou have been teleported to the spawn lcoation for the tutorial.");
            return true;
        }

        if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("remove")) {

            Player player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                sender.sendMessage("§cYou have entered an invalid online player name.");
                return false;
            }

            SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());

            if (args[0].equalsIgnoreCase("start")) {

                if (tutorialManager.getTutorialedPlayers().containsKey(player.getUniqueId())) {
                    FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" is already viewing the tutorial.").color(ChatColor.RED);
                    message.send(sender);
                    return false;
                }

                tutorialManager.start(player);
                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" is now viewing the tutorial.").color(ChatColor.GREEN);
                message.send(sender);
                return true;
            }

            if (!tutorialManager.getTutorialedPlayers().containsKey(player.getUniqueId())) {
                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" is not currently viewing the tutorial.").color(ChatColor.RED);
                message.send(sender);
                return false;
            }

            tutorialManager.remove(player);
            FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" has been removed from viewing the tutorial.").color(ChatColor.GREEN);
            message.send(sender);
            return true;
        }

        if (tutorialManager.getTutorialedPlayers().keySet().size() == 0) {
            sender.sendMessage("§cThere are currently no tutorial viewers.");
            return false;
        }

        sender.sendMessage("§aCurrent Viewers of the Tutorial: ");

        for (UUID uuid : tutorialManager.getTutorialedPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());
                FancyMessage message = new FancyMessage(" - ").color(ChatColor.GRAY).then(scPlayer.getTag()).tooltip(scPlayer.getHoverText());
                message.send(sender);
            }
        }
        return true;
    }

    public int getMinArgs(String subcommand) {
        switch (subcommand.toLowerCase()) {
            case "setspawn":
                return 1;
            case "spawn":
                return 1;
            case "start":
                return 2;
            case "remove":
                return 2;
            case "listplayers":
                return 1;
            default:
                return 100;
        }
    }
}
