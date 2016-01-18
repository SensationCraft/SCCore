package com.sensationcraft.sccore.scplayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.stats.Stat;
import com.sensationcraft.sccore.stats.StatsManager;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class SCPlayer {

	private SCCore instance;
	private UUID uuid;
	private SCPlayerManager scPlayerManager;
	private RankManager rankManager;
	private StatsManager statsManager;
	private Map<UUID, BukkitRunnable> duelRequests;
	private boolean combatTagged;
	private BukkitTask combatTask;

	public SCPlayer(SCCore instance, UUID uuid) {
		this.instance = instance;
		this.uuid = uuid;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.rankManager = instance.getRankManager();
		this.statsManager = instance.getStatsManager();
		this.duelRequests = new HashMap<>();
		this.combatTagged = false;
	}

	public boolean isShoutCooldowned() {
		return this.scPlayerManager.getShoutCooldowns().contains(this.uuid);
	}

	public void shoutCooldown() {
		if (!this.isShoutCooldowned()) {
			this.scPlayerManager.getShoutCooldowns().add(this.uuid);

			this.instance.getServer().getScheduler().scheduleSyncDelayedTask(this.instance, () -> {
				if (SCPlayer.this.isShoutCooldowned())
					SCPlayer.this.scPlayerManager.getShoutCooldowns().remove(SCPlayer.this.uuid);
			}, 300L);
		}
	}

	public String getTag() {
		return this.rankManager.getRank(this.uuid).getTag().replace("%s", Bukkit.getOfflinePlayer(this.uuid).getName());
	}

	public List<String> getHoverText() {
		return Arrays.asList(
				"§bStats:",
				"   §aKills: §f" + this.statsManager.getIntegerStat(this.uuid, Stat.KILLS),
				"   §aDeaths: §f" + this.statsManager.getIntegerStat(this.uuid, Stat.DEATHS),
				"   §aK/D: §f" + +this.statsManager.getKD(this.uuid),
				"§bInfo:",
				"   §aFaction: §f",
				"   §aPower: §f",
				"§bDuels:",
				"   §aWins: §f" + this.statsManager.getIntegerStat(this.uuid, Stat.WINS),
				"   §aLosses: §f" + this.statsManager.getIntegerStat(this.uuid, Stat.LOSSES),
				"   §aW/L: §f" + this.statsManager.getWL(this.uuid));
	}


	public boolean isLockpicking() {
		return this.scPlayerManager.getLockpicking().containsKey(this.uuid);
	}

	public boolean lockpickAttempt() {

		int random = (int) (Math.random() * 100) + 1;
		return random <= this.rankManager.getRank(this.uuid).getLockpickChance();
	}

	public Map<UUID, BukkitRunnable> getDuelRequests() {
		return this.duelRequests;
	}

	public void addDuelRequest(UUID target) {

		BukkitRunnable request = new BukkitRunnable() {
			@Override
			public void run() {
				SCPlayer.this.duelRequests.remove(target);
			}
		};

		this.duelRequests.put(target, request);
		request.runTaskLater(this.instance, 6000L);
	}

	public void removeDuelRequest(UUID target) {
		if (!this.duelRequests.containsKey(target))
			return;

		BukkitRunnable task = this.duelRequests.get(target);
		if (task != null)
			task.cancel();

		this.duelRequests.remove(target);
	}

	public boolean isCombatTagged() {
		return this.combatTagged;
	}

	public void combatTag() {
		if (Bukkit.getPlayer(this.uuid) == null)
			return;

		Player player = Bukkit.getPlayer(this.uuid);

		if (this.combatTask != null)
			this.combatTask.cancel();

		if (!this.combatTagged) {
			this.combatTagged = true;
			player.sendMessage("§eYou are now in combat.");
		}

		this.combatTask = new BukkitRunnable() {

			@Override
			public void run() {
				SCPlayer.this.removeCombatTag();
			}

		}.runTaskLater(this.instance, 160L);

	}

	public void removeCombatTag() {
		this.combatTagged = false;

		if (this.combatTask != null && Bukkit.getPlayer(this.uuid) != null) {
			this.combatTask.cancel();
			this.combatTask = null;
			Bukkit.getPlayer(this.uuid).sendMessage("§eYou have left combat.");
		}
	}

}

