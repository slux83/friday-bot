/**
 * 
 */
package de.slux.line.jarvis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;

/**
 * @author adfazio
 */
public class WarGroupDao {
	private Connection conn;

	private static final String ADD_DATA_STATEMENT = "INSERT INTO war_group (group_id, group_name) VALUES(?, ?)";

	private static final String RETRIEVE_DATA_STATEMENT = "SELECT id, group_name FROM war_group WHERE group_id = ?";

	private static final String UPDATE_DATA_STATEMENT = "UPDATE war_group SET group_name = ? WHERE group_id = ?";

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
			System.out.println("WarGroupDao.storeData() name needs to be updated: " + ex);
			updateGroup(groupId, groupName);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Data stored successfully: " + groupId + " (" + groupName + ")");
	}

	/**
	 * Update the name of the group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws SQLException
	 */
	private void updateGroup(String groupId, String groupName) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_DATA_STATEMENT);
			stmt.setString(1, Base64.getEncoder().encodeToString(groupName.getBytes()));
			stmt.setString(2, groupId);
			stmt.executeUpdate();

		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Data updated successfully: " + groupId + " (" + groupName + ")");
	}

	/**
	 * get the key of the table by groupId
	 * 
	 * @param groupId
	 * @return the key or -1 if none
	 * @throws SQLException
	 */
	public int getKeyById(String groupId) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int key = -1;
		try {

			stmt = conn.prepareStatement(RETRIEVE_DATA_STATEMENT);
			stmt.setString(1, groupId);
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
				e.printStackTrace();
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return key;
	}
}
