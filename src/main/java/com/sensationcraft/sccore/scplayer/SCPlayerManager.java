package com.sensationcraft.sccore.scplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import com.sensationcraft.sccore.Main;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.lockpicks.LockpickRunnable;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.Utils;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Kishan on 12/2/15.
 */
public class SCPlayerManager implements Listener {

	private Main instance;
	private RankManager rankManager;
	private PermissionsManager permissionsManager;
	private PunishmentManager punishmentManager;
	private ArenaManager arenaManager;
	private StatsManager statsManager;
	private Utils utils;
	private List<UUID> shoutCooldowns;
	private Map<UUID, LockpickRunnable> lockpicking;
	private Map<UUID, SCPlayer> scPlayers;

	public SCPlayerManager(Main instance) {
		this.instance = instance;
		this.rankManager = instance.getRankManager();
		this.permissionsManager = instance.getPermissionsManager();
		this.statsManager = instance.getStatsManager();
		this.punishmentManager = instance.getPunishmentManager();
		this.arenaManager = instance.getArenaManager();
		this.utils = instance.getUtils();
		this.shoutCooldowns = new ArrayList<>();
		this.scPlayers = new HashMap<>();
		this.lockpicking = new HashMap<>();
	}

	public List<UUID> getShoutCooldowns() {
		return this.shoutCooldowns;
	}

	public Map<UUID, LockpickRunnable> getLockpicking() {
		return this.lockpicking;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent e) {
		Player player = e.getPlayer();
		this.addSCPlayer(player.getUniqueId());
		SCPlayer scPlayer = this.getSCPlayer(player.getUniqueId());

		if (this.rankManager.getRank(player.getUniqueId()).getId() >= Rank.MOD.getId())
			this.staff(new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §econnected."));

		e.setJoinMessage(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(final PlayerQuitEvent e) {
		Player player = e.getPlayer();
		SCPlayer scPlayer = this.getSCPlayer(player.getUniqueId());

		e.setQuitMessage(null);

		if (scPlayer.isCombatTagged()) {
			player.setHealth(0);
			scPlayer.removeCombatTag();
			this.broadcast(new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §5has logged off while in combat!"));
		}

		if (this.rankManager.getRank(player.getUniqueId()).getId() >= Rank.MOD.getId())
			this.staff(new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §edisconnected."));

		this.removeSCPlayer(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player == false || e.getDamager() instanceof Player == false) return;

		Player player = (Player) e.getEntity();
		Player target = (Player) e.getDamager();
		SCPlayer scp = this.getSCPlayer(player.getUniqueId());
		SCPlayer sct = this.getSCPlayer(target.getUniqueId());
		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(target.getLocation().getChunk()));


		if (faction.getName().equalsIgnoreCase("Safezone")) {
			return;
		}

		final Faction pFaction = MPlayerColl.get().get(player).getFaction();
		final Faction tFaction = MPlayerColl.get().get(target).getFaction();

		if (pFaction.getRelationTo(tFaction) == Rel.MEMBER && !pFaction.isNone()) {
			return;
		}

		if (pFaction.getRelationTo(tFaction) == Rel.ALLY) {
			return;
		}

		scp.combatTag();
		sct.combatTag();

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(final AsyncPlayerChatEvent e) {
		List<Punishment> punishments = this.punishmentManager.getPunishments(e.getPlayer().getUniqueId());

		for (Punishment punishment : punishments) {
			if (punishment.getType().equals(PunishmentType.MUTE)) {
				if (!punishment.hasExpired()) {
					e.getPlayer().sendMessage("§cYou are permanently muted.");
					e.setCancelled(true);
					return;
				}
			}

			if (punishment.getType().equals(PunishmentType.TEMPMUTE)) {
				if (!punishment.hasExpired()) {
					e.getPlayer().sendMessage("§cYou are temporarily muted until §3" + punishment.getEndTimestamp() + " §c.");
					e.setCancelled(true);
					return;
				}
			}
		}

		e.setCancelled(true);

		Player player = e.getPlayer();
		SCPlayer scPlayer = this.getSCPlayer(player.getUniqueId());

		if (player.isOp()) e.setMessage(e.getMessage().replace('&', ChatColor.COLOR_CHAR));

		FancyMessage message = new FancyMessage(" §7- ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText())
				.then("§8: §7" + e.getMessage());

		for (final Player other : e.getRecipients()) {
			if (other.getWorld() != player.getWorld())
				continue;
			if (other.getLocation().distanceSquared(player.getLocation()) <= 900)
				message.send(other);
		}
	}

	public SCPlayer getSCPlayer(UUID uuid) {
		if (this.scPlayers.containsKey(uuid))
			return this.scPlayers.get(uuid);

		return new SCPlayer(this.instance, uuid);
	}

	public void addSCPlayer(UUID uuid) {
		if (this.scPlayers.containsKey(uuid))
			return;

		this.scPlayers.put(uuid, new SCPlayer(this.instance, uuid));
		this.rankManager.setRank(uuid, this.rankManager.getRank(uuid));
		this.permissionsManager.setAttachment(Bukkit.getPlayer(uuid));
		this.statsManager.loadStats(uuid);

	}

	public void removeSCPlayer(UUID uuid) {

		if (this.scPlayers.containsKey(uuid))
			this.scPlayers.remove(uuid);

		this.rankManager.setSQLRank(uuid, this.rankManager.getRank(uuid));
		this.permissionsManager.removeAttachment(uuid);
		this.statsManager.unloadStats(uuid);
	}

	public void loadSCPlayers() {

		for (UUID uuid : this.scPlayers.keySet()) {
			this.removeSCPlayer(uuid);
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.addSCPlayer(player.getUniqueId());
		}
	}

	public void staff(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("sccore.staff")) {
				p.sendMessage(message);
			}
		}
	}

	public void staff(FancyMessage message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("sccore.staff")) {
				message.send(p);
			}
		}
	}

	public void broadcast(FancyMessage message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			message.send(p);
		}
	}

	public void broadcast(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(message);
		}
	}

}