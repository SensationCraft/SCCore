package com.sensationcraft.sccore.punishments;

import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sensationcraft.sccore.Main;

/**
 * Created by Anml on 1/8/16.
 */
public class PunishmentListeners implements Listener {

	Main instance;
	PunishmentManager punishmentManager;

	public PunishmentListeners(Main instance) {
		this.instance = instance;
		this.punishmentManager = instance.getPunishmentManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerLogin(AsyncPlayerPreLoginEvent e) {
		List<Punishment> punishments = this.punishmentManager.getPunishments(e.getUniqueId());

		for (Punishment punishment : punishments) {
			if (punishment.getType().equals(PunishmentType.BAN) || punishment.getType().equals(PunishmentType.TEMPBAN)) {
				if (!punishment.hasExpired()) {
					e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, punishment.getMessage());
					return;
				}
			}
		}

		this.punishmentManager.getCachedPunishments().put(e.getUniqueId(), punishments);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerQuit(PlayerQuitEvent e) {
		UUID id = e.getPlayer().getUniqueId();

		if (this.punishmentManager.getCachedPunishments().containsKey(id)) {
			for (Punishment p : this.punishmentManager.getCachedPunishments().get(id)) {
				p.execute();
			}
			this.punishmentManager.getCachedPunishments().remove(id);
		}
	}
}