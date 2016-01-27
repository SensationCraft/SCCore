package com.sensationcraft.sccore.scplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.lockpicks.LockpickRunnable;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

import lombok.Getter;

/**
 * Created by Kishan on 12/2/15.
 */

@Getter
public class SCPlayerManager {

	private SCCore instance;
	private RankManager rankManager;
	private PermissionsManager permissionsManager;
	private HelpRequestManager helpRequestManager;
	private StatsManager statsManager;
	private List<UUID> shoutCooldowns;
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