package org.sensationcraft.sccore.mysql;

import java.sql.*;

import org.sensationcraft.sccore.SCCore;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class MySQL {

    private Connection connection;
    private SCCore instance;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public MySQL(SCCore instance) {
        this.instance = instance;

        host = instance.getConfig().getString("MySQL.Host");
        port = instance.getConfig().getString("MySQL.Port");
        database = instance.getConfig().getString("MySQL.Database");
        username = instance.getConfig().getString("MySQL.Username");
        password = instance.getConfig().getString("MySQL.Password");

        try {
            if (connection != null)
                return;

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet(String qry) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(qry);
        return statement.executeQuery();
    }

    public void executeUpdate(String qry) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(qry);
        statement.executeUpdate();
        statement.close();
    }

    public void executeUpdate(PreparedStatement qry) throws SQLException {
        qry.execute();
        qry.close();
    }
}
