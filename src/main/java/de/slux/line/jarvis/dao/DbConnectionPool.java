/**
 * 
 */
package de.slux.line.jarvis.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * DB Utility class
 * 
 * @author adfazio
 */
public class DbConnectionPool {
	private static final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://";
	private static final String DB_USER = System.getProperty("jarvis.db.user");
	private static final String DB_PASSWORD = System.getProperty("jarvis.db.password");
	private static final String DB_HOST = System.getProperty("jarvis.db.host");
	private static final String DB_PORT = System.getProperty("jarvis.db.port");
	private static final String DB_NAME = System.getProperty("jarvis.db.name");
	private static final int CONN_POOL_SIZE = 10;
	private static DbConnectionPool INSTANCE = null;

	private BasicDataSource bds;

	/**
	 * Ctor
	 */
	private DbConnectionPool() {
		this.bds = new BasicDataSource();
		this.bds.setDriverClassName(DRIVER_CLASS_NAME);
		this.bds.setUrl(DB_URL + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
		this.bds.setUsername(DB_USER);
		this.bds.setPassword(DB_PASSWORD);
		this.bds.setInitialSize(CONN_POOL_SIZE);
	}

	/**
	 * Get a new connection
	 * 
	 * @return {@link Connection}
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		if (INSTANCE == null)
			INSTANCE = new DbConnectionPool();

		return INSTANCE.getBds().getConnection();
	}

	/**
	 * @return the bds
	 */
	public BasicDataSource getBds() {
		return bds;
	}

}
