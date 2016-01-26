package com.sensationcraft.sccore.punishments;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Anml on 1/7/16.
 */
public class PunishmentManager {

    private SCCore instance;
    private MySQL mySQL;
    private Map<UUID, List<Punishment>> cachedPunishments;

    public PunishmentManager(SCCore instance) {
        this.instance = instance;
        this.mySQL = instance.getMySQL();
        this.cachedPunishments = new ConcurrentHashMap<UUID, List<Punishment>>();
    }

    public Map<UUID, List<Punishment>> getCachedPunishments() {
        return this.cachedPunishments;
    }

    public List<Punishment> getPunishments(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        if (offlinePlayer == null)
            return null;

        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            if (player == null) {
                return null;
            }

            UUID id = player.getUniqueId();
            if (this.cachedPunishments.containsKey(id)) {
                return this.cachedPunishments.get(id);
            }
        }

        List<Punishment> list = Collections.synchronizedList(new ArrayList<Punishment>());

        try {
            ResultSet resultSet = this.mySQL.getResultSet("SELECT * FROM SCPunishments WHERE Target='" + offlinePlayer.getUniqueId() + "'");

            while (resultSet.next()) {
                PunishmentType type = PunishmentType.valueOf(resultSet.getString("Type"));
                UUID target = UUID.fromString(resultSet.getString("Target"));
                UUID punisher = !resultSet.getString("Punisher").equals("Console") ? UUID.fromString(resultSet.getString("Punisher")) : null;
                long created = resultSet.getLong("Created");
                long expires = resultSet.getLong("Expires");
                String reason = resultSet.getString("Reason");

                Punishment entry = new Punishment(type, target, punisher, expires, reason);
                entry.setCreated(created);
                entry.setExecuted(true);
                list.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.cachedPunishments.put(uuid, list);
        return list;
    }

    public void addPunishment(Punishment punishment) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(punishment.getTarget());

        if (offlinePlayer == null)
            return;

        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            if (player == null) {
                return;
            }

            UUID id = player.getUniqueId();
            if (this.cachedPunishments.containsKey(id)) {
                this.cachedPunishments.get(id).add(punishment);
                return;
            }
        }

        punishment.execute();
    }
}
