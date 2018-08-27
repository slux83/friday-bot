/**
 * 
 */
package de.slux.line.friday.dao.war;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.data.war.WarGroup.GroupStatus;

/**
 * @author slux
 */
public class WarGroupDao {
	private static Logger LOG = LoggerFactory.getLogger(WarGroupDao.class);

	private Connection conn;

	private static final String ADD_DATA_STATEMENT = "INSERT INTO war_group (group_id, group_name) VALUES(?, ?)";

	private static final String RETRIEVE_DATA_STATEMENT = "SELECT id, group_name FROM war_group WHERE group_id = ? AND group_status = ?";

	private static final String UPDATE_NAME_DATA_STATEMENT = "UPDATE war_group SET group_name = ? WHERE group_id = ?";
	
	private static final String UPDATE_STATUS_DATA_STATEMENT = "UPDATE war_group SET group_status = ? WHERE group_id = ?";

	private static final String RETRIEVE_ALL_DATA_STATEMENT = "SELECT group_id, group_name FROM war_group";
	
	public WarGroupDao(Connection conn) {
		this.conn = conn;
	}

	/**
	 * Store a new group, updating the name if already exists
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws SQLException
	 */
	public void storeData(String groupId, String groupName) throws SQLException {
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(ADD_DATA_STATEMENT);
			stmt.setString(1, groupId);
			stmt.setString(2, Base64.getEncoder().encodeToString(groupName.getBytes()));
			stmt.execute();

		} catch (SQLIntegrityConstraintViolationException ex) {
			// The group already exists
			LOG.info("WarGroupDao.storeData() name needs to be updated: " + ex);
			updateGroupName(groupId, groupName);
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

		LOG.info("Data stored successfully: " + groupId + " (" + groupName + ")");
	}

	/**
	 * Update the name of the group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws SQLException
	 */
	private void updateGroupName(String groupId, String groupName) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_NAME_DATA_STATEMENT);
			stmt.setString(1, Base64.getEncoder().encodeToString(groupName.getBytes()));
			stmt.setString(2, groupId);
			stmt.executeUpdate();

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

		LOG.info("Data updated successfully: " + groupId + " (" + groupName + ")");
	}
	
	/**
	 * Update the name of the group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws SQLException
	 */
	public void updateGroupStatus(String groupId, GroupStatus groupStatus) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_STATUS_DATA_STATEMENT);
			stmt.setInt(1, groupStatus.getValue());
			stmt.setString(2, groupId);
			stmt.executeUpdate();

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

		LOG.info("Data updated successfully: " + groupId + " (newStatus=" + groupStatus + ")");
	}

	/**
	 * get the key of the table by groupId
	 * 
	 * @param groupId
	 * @return the key or -1 if none
	 * @throws SQLException
	 */
	public int getKeyById(String groupId, GroupStatus groupStatus) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int key = -1;
		try {

			stmt = conn.prepareStatement(RETRIEVE_DATA_STATEMENT);
			stmt.setString(1, groupId);
			stmt.setInt(2, groupStatus.getValue());
			rs = stmt.executeQuery();

			while (rs.next()) {
				key = rs.getInt("id");
				String groupName = rs.getString("group_name");
				groupName = new String(Base64.getDecoder().decode(groupName));
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

		return key;
	}

	/**
	 * get all the uuid of the active groups
	 * 
	 * @return the map of groups (with key=guid and value=name)
	 * @throws SQLException
	 */
	public Map<String, String> getAll() throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, String> groups = new HashMap<>();
		try {

			stmt = conn.prepareStatement(RETRIEVE_ALL_DATA_STATEMENT);
			rs = stmt.executeQuery();

			while (rs.next()) {
				String guid = rs.getString("group_id");
				String groupName = rs.getString("group_name");
				groupName = new String(Base64.getDecoder().decode(groupName));

				groups.put(guid, groupName);
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

		return groups;
	}
}
