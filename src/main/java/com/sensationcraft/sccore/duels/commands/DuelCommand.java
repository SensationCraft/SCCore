package com.sensationcraft.sccore.duels.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayerColl;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.duels.Arena;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 1/3/16.
 */
public class DuelCommand implements CommandExecutor {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private Arena arena;

	public DuelCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.arena = instance.getArenaManager().getArena();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {


		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou must be a player to execute this command.");
			return false;
		}

		Player player = (Player) sender;
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		if (!sender.hasPermission("sccore.duel")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}


		String usage = "§4Usage: " +
				"§c/duel request <player>\n" +
				"         §c/duel accept <player>\n" +
				"         §c/duel deny <player>\n" +
				"         §c/duel cancel <player>\n" +
				"         §c/duel requests";

		if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
			sender.sendMessage(usage);
			return false;
		}

		if (args[0].equalsIgnoreCase("request") || args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase
				("deny") || args[0].equalsIgnoreCase("cancel")) {

			Player target = Bukkit.getPlayer(args[1]);

			if (target == null) {
				sender.sendMessage("§cYou must enter a valid online player name.");
				return false;
			}

			SCPlayer scTarget = this.scPlayerManager.getSCPlayer(target.getUniqueId());

			if (args[0].equalsIgnoreCase("request")) {

				if (player == target) {
					sender.sendMessage("§cYou are not permitted to duel yourself.");
					return false;
				}

				if (scTarget.getDuelRequests().containsKey(player)) {
					FancyMessage message = new FancyMessage("You have already sent a duel request to ").color(ChatColor.RED).then
							(scTarget.getTag()).tooltip(scTarget.getHoverText()).then(".").color(ChatColor.RED);
					message.send(player);
					return false;
				}

				scTarget.addDuelRequest(player.getUniqueId());

				FancyMessage message = new FancyMessage("You have sent a duel request to ").color(ChatColor.GREEN).then(scTarget.getTag()
						).tooltip(scTarget.getHoverText()).then(" which will expire in 5 minutes.").color(ChatColor.GREEN);
				message.send(sender);

				message = new FancyMessage("You have received a duel request from ").color(ChatColor.GREEN).then(scPlayer.getTag())
						.tooltip(scPlayer.getHoverText()).then(". You have 5 minutes to respond to this request before it is cancelled.").color(ChatColor.GREEN);
				message.send(target);
				return true;
			} else if (args[0].equalsIgnoreCase("accept")) {
				if (!scPlayer.getDuelRequests().containsKey(target.getUniqueId())) {
					FancyMessage message = new FancyMessage("You have not received a duel request from ").color(ChatColor.RED).then
							(scTarget.getTag()).tooltip(scTarget.getHoverText()).then(".").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				if (this.arena.isRunning()) {
					sender.sendMessage("§cYou are not permitted to accept a duel request while another duel is taking place.");
					return false;
				}

				if (!this.arena.allValidLocations()) {
					sender.sendMessage("§cDuels will not take place without all location types being set.");
					return false;
				}

				if (scTarget.isCombatTagged()) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" is currently in combat which prohibits you from dueling each other.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				final Faction pFaction = MPlayerColl.get().get(player).getFaction();
				final Faction tFaction = MPlayerColl.get().get(target).getFaction();

				if (pFaction.getFlag(MFlag.ID_PEACEFUL)) {
					sender.sendMessage("§cYou are in a peaceful faction which prohibits you from dueling.");
					return false;
				}
				if (tFaction.getFlag(MFlag.ID_PEACEFUL)) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" is in a peaceful faction prohibiting you from dueling each other.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}
				if (pFaction.getRelationTo(tFaction) == Rel.MEMBER && !pFaction.isNone()) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" is a member of your faction.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}
				if (pFaction.getRelationTo(tFaction) == Rel.ALLY) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" is s a member of an allied faction.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				if (this.instance.getEssentials().getUser(target).isGodModeEnabled()) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" has god-mode enabled which prohibits you from dueling each other.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				if (this.instance.getEssentials().getUser(player).isGodModeEnabled()) {
					sender.sendMessage("§cYou must disable god-mode prior to accepting a duel request.");
					return false;
				}

				if (target.getGameMode().equals(GameMode.CREATIVE)) {
					FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText())
							.then(" is currently in creative which prohibits you from dueling each other.").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				if (player.getGameMode().equals(GameMode.CREATIVE)) {
					sender.sendMessage("§cYou are not allowed to be in creative prior to accepting a duel request.");
					return false;
				}

				scPlayer.removeDuelRequest(target.getUniqueId());
				this.arena.startMatch(player, target);

				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" " +
						"has accepted your duel request.").color(ChatColor.GREEN);
				message.send(target);

				message = new FancyMessage("You have accepted ").color(ChatColor.GREEN).then(scTarget.getTag()).tooltip(scTarget
						.getHoverText()).then("'s duel request.").color(ChatColor.GREEN);
				message.send(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("deny")) {
				if (!scPlayer.getDuelRequests().containsKey(target.getUniqueId())) {
					FancyMessage message = new FancyMessage("You have not received a duel request from ").color(ChatColor.RED)
							.then(scTarget.getTag()).tooltip(scTarget.getHoverText()).then(".").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				scPlayer.removeDuelRequest(target.getUniqueId());

				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText())
						.then(" has declined your duel request.").color(ChatColor.RED);
				message.send(target);

				message = new FancyMessage("§cYou have declined ").then(scTarget.getTag()).tooltip(scTarget.getHoverText())
						.then("§c's duel request.").color(ChatColor.RED);
				message.send(sender);
				return true;
			} else {
				if (!scTarget.getDuelRequests().containsKey(player.getUniqueId())) {
					FancyMessage message = new FancyMessage("You have not sent a duel request to ").color(ChatColor.RED).then(scTarget.getTag())
							.tooltip(scTarget.getHoverText()).then(".").color(ChatColor.RED);
					message.send(sender);
					return false;
				}

				scTarget.removeDuelRequest(player.getUniqueId());

				FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" " +
						"has cancelled his duel request.").color(ChatColor.GREEN);
				message.send(target);

				message = new FancyMessage("§cYou have cancelled your duel request to ").then(scTarget.getTag())
						.tooltip(scTarget.getHoverText()).then(".").color(ChatColor.RED);
				message.send(sender);
				return true;
			}
		} else if (args[0].equalsIgnoreCase("requests")) {
			if (scPlayer.getDuelRequests().size() != 0) {
				sender.sendMessage("§aDuel Requests:");
				for (UUID p : scPlayer.getDuelRequests().keySet()) {
					SCPlayer scP = this.scPlayerManager.getSCPlayer(p);
					FancyMessage message = new FancyMessage("   - ").color(ChatColor.WHITE).then(scP.getTag()).tooltip(scP.getHoverText());
					message.send(sender);
				}
			} else {
				sender.sendMessage("§cYou currently do not have any duel requests.");
			}
			return true;
		} else {
			sender.sendMessage(usage);
			return false;
		}
	}

	public int getMinArgs(String subcommand) {
		switch (subcommand) {
		case "request":
			return 2;
		case "deny":
			return 2;
		case "accept":
			return 2;
		case "cancel":
			return 2;
		case "requests":
			return 1;
		default:
			return 100;
		}
	}
}
