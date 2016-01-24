package com.sensationcraft.sccore.duels;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Anml on 1/3/16.
 */
public class DuelListeners implements Listener {

	private final Set<EntityDamageByEntityEvent> interceptedDamage = new HashSet<>();
	private final Set<PotionSplashEvent> interceptedPotions = new HashSet<>();
	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private ArenaManager arenaManager;
	private Arena arena;

	public DuelListeners(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.arenaManager = instance.getArenaManager();
		this.arena = this.arenaManager.getArena();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(final PlayerTeleportEvent e) {

		if (e.getPlayer().hasPermission("sccore.arena"))
			return;

		if (this.arenaManager.insideBorders(e.getTo()) && !this.arena.getArenaPlayers().contains(e.getPlayer())) {
			e.getPlayer().sendMessage("§cYou are not permitted to teleport into the duel arena.");
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent e) {
		if (this.arena.isRunning() && this.arena.getArenaPlayers().contains(e.getPlayer())) {
			this.arena.forceEnd();
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());
			if (scPlayer.getDuelRequests().containsKey(e.getPlayer())) {
				scPlayer.removeDuelRequest(e.getPlayer().getUniqueId());
			}
		}
	}

	/*
		@EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
            if (e.getEntity() instanceof Player == false || e.getDamager() instanceof Player == false) return;

            if (!this.arena.isDuel(e.getEntity(), e.getDamager()))
                e.setCancelled(true);
        }
    */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(final PlayerDeathEvent e) {
		if (this.arena.getArenaPlayers().contains(e.getEntity())) {
			this.arena.endMatch(e.getEntity());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPre(final PlayerCommandPreprocessEvent e) {

		if (this.arena.getArenaPlayers().contains(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cYou are not permitted to execute commands while dueling.");
		}
	}
/*
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerDamageByEntity(final EntityDamageByEntityEvent e)
	{
		if (this.isEvent(e.getEntity(), e.getDamager(), false))
		{
			e.setCancelled(true);
			return;
		}

		if (!e.getEntity().getWorld().getName().equalsIgnoreCase("Spawn"))
			return;
		if ((e.getEntity() instanceof Player) == false)
			return;
		if (!this.instance.getEssentials().getUser((Player) e.getEntity()).isGodModeEnabled())
			this.interceptedDamage.add(e);
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerDamageByEntityLate(final EntityDamageByEntityEvent e)
	{
		if (!this.interceptedDamage.remove(e))
			return;

		if (this.isEvent(e.getEntity(), e.getDamager(), false))
		{
			e.setCancelled(false);
			return;
		}
		if (this.arena.isDuel(e.getEntity(), e.getDamager()))
			e.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPotionSplashEvent(final PotionSplashEvent e)
	{
		for (final LivingEntity le : e.getAffectedEntities())
			if (this.isEvent(le, e.getEntity(), true))
			{
				e.setCancelled(true);
				return;
			}

		if (!e.getEntity().getWorld().getName().equalsIgnoreCase("Spawn"))
			return;
		if ((e.getEntity().getShooter() instanceof Player) == false)
			return;
		if (!this.instance.getEssentials().getUser((Player)e.getEntity().getShooter()).isGodModeEnabled())
			this.interceptedPotions.add(e);
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPotionSplashEventLate(final PotionSplashEvent e)
	{
		if(!this.interceptedPotions.remove(e))
			return;
		final Player shooter = (Player) (e.getPotion()).getShooter();
		if (this.arena.isDuel(shooter))
		{
			for (final LivingEntity affected : e.getAffectedEntities())
				if (affected instanceof Player)
					if (!this.isEvent(affected, shooter, true))
						e.setIntensity(affected, 0.0D);
			if (!e.getAffectedEntities().isEmpty())
				e.setCancelled(false);
			return;
		}

		for (final LivingEntity affected : e.getAffectedEntities())
			if (affected instanceof Player)
				if (!this.arena.isDuel(affected, shooter))
					e.setIntensity(affected, 0.0D);
		if (!e.getAffectedEntities().isEmpty())
			e.setCancelled(false);
	}

	private boolean isEvent(final Entity defender, Entity attacker, final boolean pot)
	{
		if ((defender instanceof Player) == false)
			return false;
		if ((attacker instanceof Player) == false)
			if (attacker instanceof Projectile)
			{
				attacker = (Entity) ((Projectile) attacker).getShooter();
				if ((attacker instanceof Player) == false)
					return false;
			} else
				return false;

		boolean b = true;
		//So damage pots don't affect same team, uncomment when we have teams
		if (!pot)
			b = !this.ea.sameTeam((Player) attacker, (Player) defender);

		return this.arena.isDuel(attacker, defender)
				&& b;
	}
   */
}
