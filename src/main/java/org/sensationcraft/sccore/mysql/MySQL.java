package org.sensationcraft.sccore.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sensationcraft.sccore.SCCore;

import lombok.Cleanup;
import lombok.Getter;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class MySQL {

	@Getter
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

	public ResultSet getResultSet(String qry) throws SQLException {

		PreparedStatement statement = this.connection.prepareStatement(qry);
		return statement.executeQuery();
	}

	public void executeUpdate(String qry) throws SQLException {

		@Cleanup
		PreparedStatement statement = this.connection.prepareStatement(qry);
		statement.executeUpdate();
	}

	public void executeUpdate(PreparedStatement qry) throws SQLException {
		try {
			qry.execute();
		}finally{
			qry.close();
		}
	}
}
