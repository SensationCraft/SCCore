package com.sensationcraft.sccore.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.stats.Stat;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

/**
 * Created by Anml on 12/31/15.
 */
public class Arena {

	private SCCore instance;
	private FileConfiguration config;
	private SCPlayerManager scPlayerManager;
	private StatsManager statsManager;
	private Player primaryPlayer, secondaryPlayer;
	private Location primaryPlayerLocation, secondaryPlayerLocation;
	private boolean running;
	private BukkitTask task;


	public Arena(SCCore instance) {
		this.instance = instance;
		this.config = instance.getConfig();
		this.scPlayerManager = instance.getSCPlayerManager();
		this.statsManager = instance.getStatsManager();
		this.running = false;
	}

	public Location getLocation(ArenaLocationType type) {
		String path = "Duels.Arena." + type.name();
		if (!this.config.contains(path)) {
			return null;
		} else {
			String[] loc = this.config.getString(path).split(",");
			try {
				World w = Bukkit.getWorld(loc[0]);
				Double x = Double.parseDouble(loc[1]);
				Double y = Double.parseDouble(loc[2]);
				Double z = Double.parseDouble(loc[3]);
				float yaw = Float.parseFloat(loc[4]);
				float pitch = Float.parseFloat(loc[5]);
				Location location = new Location(w, x, y, z, yaw, pitch);
				return location;
			} catch (Exception e) {
				return null;
			}
		}
	}

	public boolean allValidLocations() {
		for (ArenaLocationType type : ArenaLocationType.values()) {
			if (!this.isValidLocation(type)) return false;
		}
		return true;
	}

	public boolean isValidLocation(ArenaLocationType type) {
		return this.getLocation(type) != null;
	}

	public World getWorld() {
		for (ArenaLocationType type : ArenaLocationType.values()) {
			if (this.getLocation(type) != null) return this.getLocation(type).getWorld();
		}
		return null;
	}

	public void setLocation(ArenaLocationType type, Location loc) {
		String path = "Duels.Arena." + type.name();

		if (loc == null) {
			this.config.set(path, null);
			this.instance.saveConfig();
			return;
		}

		String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
		this.config.set(path, location);
		this.instance.saveConfig();
	}

	public boolean isRunning() {
		return this.running;
	}

	public void startMatch(Player primaryPlayer, Player secondaryPlayer) {

		this.primaryPlayer = primaryPlayer;
		this.primaryPlayerLocation = primaryPlayer.getLocation();
		this.secondaryPlayer = secondaryPlayer;
		this.secondaryPlayerLocation = secondaryPlayer.getLocation();

		if (!primaryPlayer.teleport(this.getLocation(ArenaLocationType.PrimarySpawn), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
			this.forceEnd();
			return;
		}
		if (!secondaryPlayer.teleport(this.getLocation(ArenaLocationType.SecondarySpawn), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
			this.forceEnd();
			return;
		}

		SCPlayer primary = this.scPlayerManager.getSCPlayer(primaryPlayer.getUniqueId());
		SCPlayer secondary = this.scPlayerManager.getSCPlayer(secondaryPlayer.getUniqueId());

		this.running = true;

		FancyMessage message = new FancyMessage(primary.getTag()).tooltip(primary.getHoverText()).then(" §6and ")
				.then(secondary.getTag()).tooltip(secondary.getHoverText()).then(" §6are now dueling! '/spectate' to " +
						"spectate the battle!");
		this.scPlayerManager.broadcast(message);

		this.task = new BukkitRunnable() {
			@Override
			public void run() {
				Arena.this.forceEnd();
			}
		}.runTaskLater(this.instance, 6000L);
	}

	protected void endMatch(final Player loser) {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}

		SCPlayer scLoser = this.scPlayerManager.getSCPlayer(loser.getUniqueId());
		Player winner = loser.equals(this.primaryPlayer) ? this.secondaryPlayer : this.primaryPlayer;
		SCPlayer scWinner = this.scPlayerManager.getSCPlayer(winner.getUniqueId());

		this.statsManager.setIntegerStat(winner.getUniqueId(), Stat.WINS, this.statsManager.getIntegerStat(winner.getUniqueId(), Stat.WINS) + 1);
		this.statsManager.setIntegerStat(loser.getUniqueId(), Stat.KILLS, this.statsManager.getIntegerStat(loser.getUniqueId(), Stat.LOSSES) + 1);

		FancyMessage message = new FancyMessage(scWinner.getTag()).tooltip(scWinner.getHoverText()).then(" §6has " +
				"beaten ").then(scLoser.getTag()).tooltip(scLoser.getHoverText()).then(" §6in the duel arena!");
		this.scPlayerManager.broadcast(message);
		scWinner.removeCombatTag();
		scLoser.removeCombatTag();
		Location location = winner.getName().equalsIgnoreCase(Arena.this.primaryPlayer.getName()) ? Arena.this.primaryPlayerLocation : Arena.this.secondaryPlayerLocation;

		if (this.primaryPlayer.getUniqueId().equals(loser.getUniqueId())) this.primaryPlayer = null;
		if (this.secondaryPlayer.getUniqueId().equals(loser.getUniqueId())) this.secondaryPlayer = null;
		winner.sendMessage("§aYou have 10 seconds to collect the dropped items.");

		new BukkitRunnable() {
			@Override
			public void run() {
				winner.teleport(location);

				Arena.this.reset();
			}
		}.runTaskLater(this.instance, 200L);
	}

	public void forceEnd() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}

		SCPlayer primary = this.scPlayerManager.getSCPlayer(this.primaryPlayer.getUniqueId());
		SCPlayer secondary = this.scPlayerManager.getSCPlayer(this.secondaryPlayer.getUniqueId());

		FancyMessage message = new FancyMessage("§6The duel between ").then(primary.getTag()).tooltip(primary
				.getHoverText()).then(" and ").then(secondary.getTag()).tooltip(secondary.getHoverText()).then(
						"§6ended in a draw!");
		this.scPlayerManager.broadcast(message);
		this.primaryPlayer.teleport(this.primaryPlayerLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
		this.secondaryPlayer.teleport(this.secondaryPlayerLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

		this.reset();
	}

	public List<Player> getArenaPlayers() {
		List<Player> players = new ArrayList<>();
		if (this.primaryPlayer != null)
			players.add(this.primaryPlayer);
		if (this.secondaryPlayer != null)
			players.add(this.secondaryPlayer);

		return players;
	}

	public boolean isDuel(Entity... entities) {
		List<Player> arenaPlayers = this.getArenaPlayers();
		for (Entity entity : entities) {
			Player player = null;
			if (entity instanceof Player)
				player = (Player) entity;
			else if (entity instanceof Projectile) {
				ProjectileSource source = ((Projectile) entity).getShooter();
				if (source instanceof Player)
					player = (Player) source;
			}
			if (player != null && !arenaPlayers.contains(player))
				return false;
		}
		return true;
	}

	public void reset() {
		this.primaryPlayer = null;
		this.secondaryPlayer = null;
		this.primaryPlayerLocation = null;
		this.secondaryPlayerLocation = null;

		this.running = false;
	}


}
