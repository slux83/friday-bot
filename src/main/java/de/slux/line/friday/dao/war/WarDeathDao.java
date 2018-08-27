/**
 * 
 */
package de.slux.line.friday.dao.war;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.data.war.WarGroup;

/**
 * @author adfazio
 */
public class WarDeathDao {
	private static Logger LOG = LoggerFactory.getLogger(WarDeathDao.class);

	private Connection conn;

	private static final String ADD_DATA_STATEMENT = "INSERT INTO war_death (group_id, node, num_deaths, champion, player) VALUES(?, ?, ?, ?, ?)";

	private static final String RETRIEVE_DATA_STATEMENT = "SELECT node, num_deaths, FROM_BASE64(champion) AS champion, FROM_BASE64(player) AS player FROM war_death WHERE group_id = ?";

	private static final String DELETE_DATA_BY_GROUP_STATEMENT = "DELETE FROM war_death WHERE group_id = ?";

	private static final String GET_LAST_INSERT_STATEMENT = "SELECT MAX(id) AS latest FROM war_death WHERE group_id = ?";

	private static final String DELETE_DATA_BY_ID_STATEMENT = "DELETE FROM war_death WHERE id = ?";

	public WarDeathDao(Connection conn) {
		this.conn = conn;
	}

	/**
	 * Store a new death to the group
	 * 
	 * @param groupKey
	 * @param node
	 * @param deaths
	 * @param champName
	 * @param userName
	 * @throws SQLException
	 */
	public void storeData(int groupKey, int deaths, int node, String champName, String userName) throws SQLException {
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(ADD_DATA_STATEMENT);
			stmt.setInt(1, groupKey);
			stmt.setInt(2, node);
			stmt.setInt(3, deaths);
			stmt.setString(4, Base64.getEncoder().encodeToString(champName.getBytes()));
			stmt.setString(5, Base64.getEncoder().encodeToString(userName.getBytes()));
			stmt.execute();

		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
		}

		LOG.info("Data stored successfully: " + deaths + ", " + node + " (" + champName + ")");
	}

	/**
	 * get the war group model or null in case of error
	 * 
	 * @param groupKey
	 * @return the group model
	 * @throws SQLException
	 */
	public WarGroup retrieveData(int groupKey) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		WarGroup warGroupModel = null;
		try {

			stmt = conn.prepareStatement(RETRIEVE_DATA_STATEMENT);
			stmt.setInt(1, groupKey);
			rs = stmt.executeQuery();
			warGroupModel = new WarGroup();
			while (rs.next()) {
				int node = rs.getInt("node");
				int deaths = rs.getInt("num_deaths");
				String champion = rs.getString("champion");
				String player = rs.getString("player");

				warGroupModel.addDeath(deaths, node, champion, player);
			}

		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
		}

		return warGroupModel;
	}

	/**
	 * Delete the current report for the group
	 * 
	 * @param groupKey
	 *            the key of the group
	 * @throws SQLException
	 */
	public void clearData(int groupKey) throws SQLException {
		PreparedStatement stmt = null;
		// TODO: reset the summoners too
		
		try {
			stmt = conn.prepareStatement(DELETE_DATA_BY_GROUP_STATEMENT);
			stmt.setInt(1, groupKey);
			int deletedRows = stmt.executeUpdate();
			LOG.info("Deleted " + deletedRows + " report(s) for groupkey=" + groupKey);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
		}

	}

	/**
	 * Undo the last insert based on the oldest id for the given key
	 * 
	 * @param groupKey
	 * @throws SQLException
	 */
	public void undoLast(int groupKey) throws SQLException {
		PreparedStatement stmt = null;

		try {
			// First we get the last
			stmt = conn.prepareStatement(GET_LAST_INSERT_STATEMENT);
			stmt.setInt(1, groupKey);
			ResultSet rs = stmt.executeQuery();

			rs.next(); // We always have one row

			int latestId = rs.getInt("latest");

			if (rs.wasNull())
				return; // Nothing to undo

			stmt.close();

			// We delete the latest row
			stmt = conn.prepareStatement(DELETE_DATA_BY_ID_STATEMENT);
			stmt.setInt(1, latestId);
			int deletedRows = stmt.executeUpdate();
			LOG.info("Delete latest inserted row of groupKey=" + groupKey + ", id: " + latestId + ". Effected rows: "
			        + deletedRows);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Unexpected error " + e, e);
			}
		}
	}
}
