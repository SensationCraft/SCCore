package com.sensationcraft.sccore.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.mysql.MySQL;

/**
 * Created by Anml on 1/12/16.
 */
public class StatsManager {

	private SCCore instance;
	private MySQL mySQL;
	private Map<UUID, Integer> kills;
	private Map<UUID, Integer> deaths;
	private Map<UUID, Integer> wins;
	private Map<UUID, Integer> losses;

	public StatsManager(SCCore instance) {
		this.instance = instance;
		this.mySQL = instance.getMySQL();
		this.kills = new HashMap<>();
		this.deaths = new HashMap<>();
		this.wins = new HashMap<>();
		this.losses = new HashMap<>();
	}

	public int getIntegerStat(UUID uuid, Stat stat) {

		if (stat.equals(Stat.KILLS)) {
			if (this.kills.containsKey(uuid)) {
				return this.kills.get(uuid);
			}
		} else if (stat.equals(Stat.DEATHS)) {
			if (this.deaths.containsKey(uuid)) {
				return this.deaths.get(uuid);
			}
		} else if (stat.equals(Stat.WINS)) {
			if (this.wins.containsKey(uuid)) {
				return this.wins.get(uuid);
			}
		} else if (stat.equals(Stat.LOSSES)) {
			if (this.losses.containsKey(uuid)) {
				return this.losses.get(uuid);
			}
		}

		try {
			ResultSet resultSet = this.mySQL.getResultSet("SELECT " + stat.getName() + " FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
			if (resultSet.next()) {
				return resultSet.getInt(stat.getName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}


	public void setIntegerStat(UUID uuid, Stat stat, int value) {

		if (stat.equals(Stat.KILLS)) {
			if (this.kills.containsKey(uuid)) {
				this.kills.replace(uuid, value);
				return;
			}
			if (Bukkit.getPlayer(uuid) != null) {
				this.kills.put(uuid, value);
				return;
			}
		} else if (stat.equals(Stat.DEATHS)) {
			if (this.deaths.containsKey(uuid)) {
				this.deaths.replace(uuid, value);
				return;
			}
			if (Bukkit.getPlayer(uuid) != null) {
				this.deaths.put(uuid, value);
				return;
			}
		} else if (stat.equals(Stat.WINS)) {
			if (this.wins.containsKey(uuid)) {
				this.wins.replace(uuid, value);
				return;
			}
			if (Bukkit.getPlayer(uuid) != null) {
				this.wins.put(uuid, value);
				return;
			}
		} else if (stat.equals(Stat.LOSSES)) {
			if (this.losses.containsKey(uuid)) {
				this.losses.replace(uuid, value);
				return;
			}
			if (Bukkit.getPlayer(uuid) != null) {
				this.losses.put(uuid, value);
				return;
			}
		}


		this.setSQLIntegerStatistic(uuid, stat, value);
	}

	private void setSQLIntegerStatistic(UUID uuid, Stat stat, int value) {
		try {
			this.mySQL.executeUpdate("UPDATE SCPlayerInfo SET " + stat.getName() + "='" + value + "' WHERE UUID='" + uuid + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadStats(UUID uuid) {
		if (!Bukkit.getOfflinePlayer(uuid).isOnline())
			return;

		if (this.kills.containsKey(uuid))
			this.kills.remove(uuid);
		if (this.deaths.containsKey(uuid))
			this.deaths.remove(uuid);
		if (this.wins.containsKey(uuid))
			this.wins.remove(uuid);
		if (this.losses.containsKey(uuid))
			this.losses.remove(uuid);

		this.kills.put(uuid, this.getIntegerStat(uuid, Stat.KILLS));
		this.deaths.put(uuid, this.getIntegerStat(uuid, Stat.DEATHS));
		this.wins.put(uuid, this.getIntegerStat(uuid, Stat.WINS));
		this.losses.put(uuid, this.getIntegerStat(uuid, Stat.LOSSES));

	}

	public void unloadStats(UUID uuid) {
		if (this.kills.containsKey(uuid)) {
			this.setSQLIntegerStatistic(uuid, Stat.KILLS, this.kills.get(uuid));
			this.kills.remove(uuid);
		}
		if (this.deaths.containsKey(uuid)) {
			this.setSQLIntegerStatistic(uuid, Stat.DEATHS, this.deaths.get(uuid));
			this.deaths.remove(uuid);
		}
		if (this.wins.containsKey(uuid)) {
			this.setSQLIntegerStatistic(uuid, Stat.WINS, this.wins.get(uuid));
			this.wins.remove(uuid);
		}
		if (this.losses.containsKey(uuid)) {
			this.setSQLIntegerStatistic(uuid, Stat.LOSSES, this.losses.get(uuid));
			this.losses.remove(uuid);
		}

	}

	public double getKD(UUID uuid) {
		if (this.getIntegerStat(uuid, Stat.DEATHS) == 0) {
			return this.getIntegerStat(uuid, Stat.KILLS);
		}

		DecimalFormat df = new DecimalFormat("#.##");
		double ratio = ((double) this.getIntegerStat(uuid, Stat.KILLS)) / ((double) this.getIntegerStat(uuid, Stat.DEATHS));
		return Double.valueOf(df.format(ratio));
	}

	public double getWL(UUID uuid) {
		if (this.getIntegerStat(uuid, Stat.LOSSES) == 0) {
			return this.getIntegerStat(uuid, Stat.WINS);
		}

		DecimalFormat df = new DecimalFormat("#.##");
		double ratio = ((double) this.getIntegerStat(uuid, Stat.WINS)) / ((double) this.getIntegerStat(uuid, Stat.LOSSES));
		return Double.valueOf(df.format(ratio));
	}
}
