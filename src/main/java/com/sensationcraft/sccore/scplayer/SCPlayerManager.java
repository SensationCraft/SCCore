package com.sensationcraft.sccore.scplayer;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.lockpicks.LockpickRunnable;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Created by Kishan on 12/2/15.
 */
public class SCPlayerManager implements Listener {

	private SCCore instance;
	private RankManager rankManager;
	private PermissionsManager permissionsManager;
	private HelpRequestManager helpRequestManager;
	private StatsManager statsManager;
	@Getter
	private List<UUID> shoutCooldowns;
	@Getter
	private Map<UUID, LockpickRunnable> lockpicking;
	private Map<UUID, SCPlayer> scPlayers;

	public SCPlayerManager(SCCore instance) {
		this.instance = instance;
		this.rankManager = instance.getRankManager();
		this.permissionsManager = instance.getPermissionsManager();
		this.helpRequestManager = instance.getHelpRequestManager();
		this.statsManager = instance.getStatsManager();
		this.shoutCooldowns = new ArrayList<>();
		this.scPlayers = new HashMap<>();
		this.lockpicking = new HashMap<>();
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
		if(helpRequestManager != null && helpRequestManager.getRequests().containsKey(uuid)) {
			helpRequestManager.removeRequest(uuid);
		}

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