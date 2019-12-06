/**
 *
 */
package de.slux.line.friday.dao;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DB Utility class
 *
 * @author slux
 */
public class DbConnectionPool {
    private static final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://";
    public static String DB_USER = System.getProperty("friday.db.user");
    public static String DB_PASSWORD = System.getProperty("friday.db.password");
    public static String DB_HOST = System.getProperty("friday.db.host");
    public static String DB_PORT = System.getProperty("friday.db.port");
    public static String DB_NAME = System.getProperty("friday.db.name");
    public static int CONN_POOL_SIZE = 10;
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
