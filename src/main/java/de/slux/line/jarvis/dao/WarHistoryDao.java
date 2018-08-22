/**
 * 
 */
package de.slux.line.jarvis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import de.slux.line.jarvis.war.WarGroup;

/**
 * @author adfazio
 */
public class WarHistoryDao {
	private Connection conn;

	private static final String ADD_DATA_STATEMENT = "INSERT INTO war_history (group_id, node, num_deaths, champion, player, opponent_tag, war_date) "
			+ "(SELECT group_id, node, num_deaths, champion, player, TO_BASE64(?), ? FROM war_death where group_id = ?)";

	private static final String GET_ALL_DATA = "SELECT war_date, FROM_BASE64(opponent_tag) AS ally_tag FROM war_history "
			+ "WHERE group_id = ? GROUP BY war_date, ally_tag ORDER BY war_date";

	private static final String GET_DAILY_DATA = "SELECT FROM_BASE64(opponent_tag) AS ally_tag, node, num_deaths, "
			+ "FROM_BASE64(champion) AS champ, FROM_BASE64(player) AS player_name "
			+ "FROM war_history WHERE group_id = ? AND war_date = ?";
	
	private static final String DELETE_DATA = "DELETE FROM war_history WHERE group_id = ? AND war_date = ? AND opponent_tag = TO_BASE64(?)";

	public WarHistoryDao(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * Remove data from the history
	 * 
	 * @param groupKey
	 * @param allianceTag
	 * @param date
	 * @return true if data has been found, false otherwise
	 * @throws SQLException
	 */
	public boolean deleteData(int groupKey, String allianceTag, Date date) throws SQLException {
		PreparedStatement stmt = null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Timestamp timestamp = new Timestamp(c.getTimeInMillis());
		int deletedEntries = 0;
		try {
			stmt = conn.prepareStatement(DELETE_DATA);
			stmt.setInt(1, groupKey);
			stmt.setTimestamp(2, timestamp);
			stmt.setString(3, allianceTag);
			deletedEntries = stmt.executeUpdate();
			
			System.out.println("Deleted " + deletedEntries + " in history for " + allianceTag + " on " + timestamp);

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

		return (deletedEntries > 0);
	}
	
	/**
	 * get all the history for a particular day
	 * 
	 * @param groupKey
	 * @param warDate
	 * @return map where key=ally_tag 
	 * @throws SQLException
	 */
	public Map<String, WarGroup> getAllData(int groupKey, Timestamp warDate) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, WarGroup> outcome = new TreeMap<>();
		try {

			stmt = conn.prepareStatement(GET_DAILY_DATA);
			stmt.setInt(1, groupKey);
			stmt.setTimestamp(2, warDate);
			rs = stmt.executeQuery();

			while (rs.next()) {
				String allianceName = rs.getString("ally_tag");
				int node = rs.getInt("node");
				int numDeaths = rs.getInt("num_deaths");
				String champ = rs.getString("champ");
				String player = rs.getString("player_name");
				
				if (!outcome.containsKey(allianceName)) {
					WarGroup warGroup = new WarGroup();
					outcome.put(allianceName, warGroup);
				}
				
				outcome.get(allianceName).addDeath(numDeaths, node, champ, player);
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

		return outcome;
	}

	/**
	 * get all the history organized by date and alliance
	 * 
	 * @param groupKey
	 * @return the history
	 * @throws SQLException
	 */
	public Map<Timestamp, String> getAllData(int groupKey) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<Timestamp, String> outcome = new TreeMap<>();
		try {

			stmt = conn.prepareStatement(GET_ALL_DATA);
			stmt.setInt(1, groupKey);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Timestamp warDate = rs.getTimestamp("war_date");
				String allianceName = rs.getString("ally_tag");

				if (outcome.containsKey(warDate)) {
					String ally = outcome.get(warDate);
					ally += ", " + allianceName;
					outcome.put(warDate, ally);
				} else {
					outcome.put(warDate, allianceName);
				}
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

		return outcome;
	}

	/**
	 * Store the current content of the death into the history
	 * 
	 * @param groupKey
	 * @param allianceTag
	 * @throws SQLException
	 */
	public void storeData(int groupKey, String allianceTag) throws SQLException {
		PreparedStatement stmt = null;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Timestamp timestamp = new Timestamp(c.getTimeInMillis());

		try {
			stmt = conn.prepareStatement(ADD_DATA_STATEMENT);
			stmt.setString(1, allianceTag);
			stmt.setTimestamp(2, timestamp);
			stmt.setInt(3, groupKey);
			stmt.execute();

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

		System.out.println("Data stored successfully: " + allianceTag + ", "
				+ timestamp);
	}

}
