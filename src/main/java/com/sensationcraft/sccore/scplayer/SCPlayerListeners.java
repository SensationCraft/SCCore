package com.sensationcraft.sccore.scplayer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Created by Anml on 1/25/16.
 */
public class SCPlayerListeners implements Listener {

	SCCore instance;
	SCPlayerManager scPlayerManager;
	Essentials essentials;
	RankManager rankManager;
	PermissionsManager permissionsManager;
	ArenaManager arenaManager;

	public SCPlayerListeners(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.essentials = instance.getEssentials();
		this.rankManager = instance.getRankManager();
		this.permissionsManager = instance.getPermissionsManager();
		this.arenaManager = instance.getArenaManager();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
		this.scPlayerManager.addSCPlayer(e.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(final PlayerJoinEvent e) {

		Player player = e.getPlayer();

		this.permissionsManager.setAttachment(player);

		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		if (this.rankManager.getRank(player.getUniqueId()).getId() >= Rank.MOD.getId())
			this.scPlayerManager.staff(new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §econnected."));

		e.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		if (scPlayer.isCombatTagged()) {
			e.setCancelled(true);
			player.sendMessage("§cYou are not permitted to teleport while in combat.");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPre(final PlayerCommandPreprocessEvent e) {
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(e.getPlayer().getUniqueId());
		if (scPlayer.isCombatTagged()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cYou are not permitted to execute commands while in combat.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(final PlayerQuitEvent e) {
		Player player = e.getPlayer();
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

		e.setQuitMessage(null);

		if (scPlayer.isCombatTagged()) {
			player.setHealth(0);
			scPlayer.removeCombatTag();
			this.scPlayerManager.broadcast(new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §dhas logged off while in combat!"));
		}

		if (this.rankManager.getRank(player.getUniqueId()).getId() >= Rank.MOD.getId())
			this.scPlayerManager.staff(new FancyMessage("§9[STAFF] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §edisconnected."));

		this.scPlayerManager.removeSCPlayer(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Location from = e.getFrom();

		Faction faction = Board.getInstance().getFactionAt(new FLocation(from));

		if (this.arenaManager.getArena().isRunning() && this.arenaManager.getArena().getArenaPlayers().contains(player)) {
			if (player.getWalkSpeed() != .2F)
				player.setWalkSpeed(.2F);
			return;
		}

		if (!faction.isSafeZone()) {
			if (player.getWalkSpeed() == .4F) {
				player.setWalkSpeed(.2F);
			}
			return;
		}

		if (player.getWalkSpeed() != .4F) {
			player.setWalkSpeed(.4F);
		}

	}

	@EventHandler
	public void onPlayerFire(final EntityCombustEvent e) {
		if (e.getEntity() instanceof Player == false)
			return;
		Faction faction = Board.getInstance().getFactionAt(new FLocation(e.getEntity().getLocation()));
		if (faction != null && faction.isSafeZone() && !this.arenaManager.getArena().isDuel(e.getEntity()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player == false)
			return;
		Faction faction = Board.getInstance().getFactionAt(new FLocation(e.getEntity().getLocation()));
		if (faction != null && faction.isSafeZone() && !this.arenaManager.getArena().isDuel(e.getEntity())) {
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

		SCPlayer scp = this.scPlayerManager.getSCPlayer(player.getUniqueId());
		SCPlayer sct = this.scPlayerManager.getSCPlayer(target.getUniqueId());
		Faction faction = Board.getInstance().getFactionAt(new FLocation(target.getLocation()));


		if (faction.isSafeZone() && !(this.arenaManager.getArena().getArenaPlayers().contains(player) || this.arenaManager.getArena().getArenaPlayers().contains(target))) {
			return;
		}

		final Faction pFaction = FPlayers.getInstance().getByPlayer(player).getFaction();
		final Faction tFaction = FPlayers.getInstance().getByPlayer(target).getFaction();

		if (pFaction.getRelationTo(tFaction) == Relation.MEMBER && !pFaction.isNone()) {
			return;
		}

		if (pFaction.getRelationTo(tFaction) == Relation.ALLY || pFaction.getRelationTo(tFaction) == Relation.TRUCE) {
			return;
		}

		Faction standingOn = Board.getInstance().getFactionAt(new FLocation(target.getLocation()));

		if (pFaction.getRelationTo(tFaction) == Relation.NEUTRAL && tFaction.equals(standingOn))
			return;

		User pUser = this.essentials.getUser(player);
		User tUser = this.essentials.getUser(target);

		if (pUser.isGodModeEnabled() || tUser.isGodModeEnabled())
			return;

		if (pUser.isVanished() || tUser.isVanished())
			return;

		if (!player.getGameMode().equals(GameMode.SURVIVAL) || !target.getGameMode().equals(GameMode.SURVIVAL))
			return;


		scp.combatTag();
		sct.combatTag();

	}
}
