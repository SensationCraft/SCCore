package com.sensationcraft.sccore.ranks;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.mysql.MySQL;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Anml on 1/12/16.
 */

public class RankManager {

	private SCCore instance;
	private MySQL mySQL;
	private Map<UUID, Rank> players;

	public RankManager(SCCore instance) {
		this.instance = instance;
		this.mySQL = instance.getMySQL();
		this.players = new HashMap<>();
	}

	public Rank getRankById(int id) {
		for (Rank rank : Rank.values()) {
			if (rank.getId() == id)
				return rank;
		}
		return Rank.DEFAULT;
	}

	public Rank getRank(UUID uuid) {
		if (this.players.containsKey(uuid))
			return this.players.get(uuid);

		try {
			ResultSet resultSet = this.mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
			if (resultSet.next()) {
				return Rank.valueOf(resultSet.getString("Rank"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Rank.DEFAULT;
	}


	public void setRank(UUID uuid, Rank rank) {
		if (this.players.containsKey(uuid)) {
			this.players.replace(uuid, rank);
			return;
		}

		if (Bukkit.getPlayer(uuid) != null) {
			this.players.put(uuid, rank);
			return;
		}

		this.setSQLRank(uuid, rank);
	}

	public void setSQLRank(UUID uuid, Rank rank) {

		if (!this.hasRank(uuid)) {
			try {
				this.mySQL.executeUpdate("INSERT INTO `SCPlayerInfo`(`UUID`, `Rank`) VALUES ('" + uuid + "','" + rank.name() + "')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.mySQL.executeUpdate("UPDATE SCPlayerInfo SET Rank='" + rank.name() + "' WHERE UUID='" + uuid + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasRank(UUID uuid) {
		try {
			ResultSet rs = this.mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public Map<UUID, Rank> getPlayers() {
		return this.players;
	}
}
