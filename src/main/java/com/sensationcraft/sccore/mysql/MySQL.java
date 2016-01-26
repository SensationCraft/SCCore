package com.sensationcraft.sccore.mysql;

import com.google.common.collect.Maps;
import com.sensationcraft.sccore.SCCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Map;

public class MySQL {

    public static final String PUNISHMENT_INSERT = "INSERT INTO `SCPunishments` (`Type`, `Target`, `Punisher`, `Created`, `Expires`, `Reason`) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    public static final String PUNISHMENT_UPDATE = "UPDATE SCPunishments SET Expires=? WHERE Target=? AND Created=?";
    public static final String PUNISHMENT_SELECT = "SELECT * FROM SCPunishments WHERE Target=?";
    public static final String RANK_UPDATE = "UPDATE SCPlayerInfo SET Rank=? WHERE UUID=?";
    public static final String RANK_INSERT = "INSERT INTO `SCPlayerInfo`(`UUID`, `Rank`) VALUES (?, ?)";
    public static final String RANK_SELECT = "SELECT Rank FROM SCPlayerInfo WHERE UUID=?";
    public static final String STAT_SELECT = "SELECT %s FROM SCPlayerInfo WHERE UUID=?";
    public static final String STAT_UPDATE = "UPDATE SCPlayerInfo SET %s=? WHERE UUID=?";
    private final Map<String, PreparedStatement> statements = Maps.newConcurrentMap();
    private Connection connection;
    private SCCore instance;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public MySQL(SCCore instance) {
        this.instance = instance;

        this.host = instance.getConfig().getString("MySQL.Host");
        this.port = instance.getConfig().getString("MySQL.Port");
        this.database = instance.getConfig().getString("MySQL.Database");
        this.username = instance.getConfig().getString("MySQL.Username");
        this.password = instance.getConfig().getString("MySQL.Password");

        try {
            if (this.connection != null)
                return;

            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ResultSet getResultSet(String qry, Object... args) throws SQLException {

        if (!this.statements.containsKey(qry))
            this.statements.put(qry, this.connection.prepareStatement(qry));

        PreparedStatement statement = this.statements.get(qry);
        synchronized (statement) {
            if (args.length > 0)
                for (int i = 0; i < args.length; i++)
                    statement.setObject(i, args[i]);
            return statement.executeQuery();
        }
    }

    /**
     * Execute an update asynchronously.
     *
     * @param qry
     * @param args
     * @throws SQLException
     */
    public void executeUpdate(String qry, Object... args) throws SQLException {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    if (!MySQL.this.statements.containsKey(qry))
                        MySQL.this.statements.put(qry, MySQL.this.connection.prepareStatement(qry));

                    PreparedStatement statement = MySQL.this.statements.get(qry);
                    synchronized (statement) {
                        if (args.length > 0)
                            for (int i = 0; i < args.length; i++)
                                statement.setObject(i, args[i]);
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    MySQL.this.instance.getLogger().severe("Failed to exectute SQL update! " + qry);
                    e.printStackTrace();
                }
            }

        }.runTaskAsynchronously(this.instance);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            this.instance.getLogger().severe("Failed to close database connection!");
            e.printStackTrace();
        }
    }
}
