package com.sensationcraft.sccore.scplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.lockpicks.LockpickRunnable;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

import lombok.Getter;

/**
 * Created by Kishan on 12/2/15.
 */

@Getter
public class SCPlayerManager implements Listener {

	private SCCore instance;
	private RankManager rankManager;
	private PermissionsManager permissionsManager;
	private HelpRequestManager helpRequestManager;
	private ArenaManager arenaManager;
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
		this.arenaManager = instance.getArenaManager();
		this.statsManager = instance.getStatsManager();
		this.shoutCooldowns = new ArrayList<>();
		this.scPlayers = new HashMap<>();
		this.lockpicking = new HashMap<>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent e){
		this.addSCPlayer(e.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(final PlayerJoinEvent e) {

		Player player = e.getPlayer();

		this.permissionsManager.setAttachment(player);

		SCPlayer scPlayer = this.getSCPlayer(player.getUniqueId());

		if (this.rankManager.getRank(player.getUniqueId()).getId() >= Rank.MOD.getId())
			this.staff(new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §econnected."));

		e.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e){
		Player player = e.getPlayer();
		SCPlayer scPlayer = this.getSCPlayer(player.getUniqueId());

		if(scPlayer.isCombatTagged()){
			e.setCancelled(true);
			player.sendMessage(ChatColor.RED+"You can't teleport while in combat!");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPre(final PlayerCommandPreprocessEvent e) {

		SCPlayer scPlayer = this.getSCPlayer(e.getPlayer().getUniqueId());

		if (scPlayer.isCombatTagged()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cYou are not permitted to execute commands while in combat.");
		}
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

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Location from = e.getFrom();

		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(from));

		if (this.arenaManager != null) {
			if (this.arenaManager.getArena().isRunning() && this.arenaManager.getArena().getArenaPlayers().contains(player))
				return;
		}

		if (!faction.getId().equals(Factions.ID_SAFEZONE)) {
			if (player.getWalkSpeed() == .4F)
				player.setWalkSpeed(.2F);
			return;
		}

		if (player.getWalkSpeed() != .4F)
			player.setWalkSpeed(.4F);

	}

	@EventHandler
	public void onPlayerFire(final EntityCombustEvent e){
		if(e.getEntity() instanceof Player == false)
			return;
		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(e.getEntity().getLocation()));
		if(faction != null && faction.getId().equals(Factions.ID_SAFEZONE) && !this.arenaManager.getArena().isDuel(e.getEntity()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e){
		if(e.getEntity() instanceof Player == false)
			return;
		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(e.getEntity().getLocation()));
		if(faction != null && faction.getId().equals(Factions.ID_SAFEZONE) && !this.arenaManager.getArena().isDuel(e.getEntity())){
			e.setCancelled(true);
			e.getEntity().setFireTicks(0);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
		Player player = null;
		if (e.getEntity() instanceof Player)
			player = (Player) e.getEntity();
		else if (e.getEntity() instanceof Projectile) {
			ProjectileSource source = ((Projectile) e.getEntity()).getShooter();
			if (source instanceof Player)
				player = (Player) source;
		}

		Player target = null;
		if (e.getDamager() instanceof Player)
			target = (Player) e.getDamager();
		else if (e.getDamager() instanceof Projectile) {
			ProjectileSource source = ((Projectile) e.getDamager()).getShooter();
			if (source instanceof Player)
				target = (Player) source;
		}

		if (player == null || target == null) return;

		SCPlayer scp = this.getSCPlayer(player.getUniqueId());
		SCPlayer sct = this.getSCPlayer(target.getUniqueId());
		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(target.getLocation().getChunk()));


		if (faction.getId().equals(Factions.ID_SAFEZONE) && !(this.arenaManager.getArena().getArenaPlayers().contains(player) || this.arenaManager.getArena().getArenaPlayers().contains(target))) {
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
		this.statsManager.loadStats(uuid);

	}

	public void removeSCPlayer(UUID uuid) {

		if (this.scPlayers.containsKey(uuid))
			this.scPlayers.remove(uuid);

		this.rankManager.setSQLRank(uuid, this.rankManager.getRank(uuid));
		this.permissionsManager.removeAttachment(uuid);
		this.statsManager.unloadStats(uuid);
		if (this.helpRequestManager != null && this.helpRequestManager.getRequests().containsKey(uuid)) {
			this.helpRequestManager.removeRequest(uuid);
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