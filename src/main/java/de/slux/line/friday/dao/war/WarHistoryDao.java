/**
 * 
 */
package de.slux.line.friday.dao.war;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.HistoryType;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.data.war.WarSummonerPlacement;

/**
 * @author slux
 */
public class WarHistoryDao {
	private static Logger LOG = LoggerFactory.getLogger(WarHistoryDao.class);

	private Connection conn;

	private static final String ADD_DATA_DEATHS_STATEMENT = "INSERT INTO war_history (group_id, node, num_deaths, champion, player, opponent_tag, war_date) "
	        + "(SELECT group_id, node, num_deaths, champion, player, TO_BASE64(?), ? FROM war_death where group_id = ?)";

	private static final String GET_ALL_DATA = "SELECT war_date, FROM_BASE64(opponent_tag) AS ally_tag FROM war_history "
	        + "WHERE group_id = ? GROUP BY war_date, ally_tag ORDER BY war_date";

	private static final String GET_DAILY_DATA = "SELECT FROM_BASE64(opponent_tag) AS ally_tag, node, num_deaths, "
	        + "FROM_BASE64(champion) AS champ, FROM_BASE64(player) AS player_name "
	        + "FROM war_history WHERE group_id = ? AND war_date = ? AND history_type = ? ORDER BY id";

	private static final String GET_STATS_DATA = "SELECT node, num_deaths, CAST(FROM_BASE64(champion) AS CHAR) AS champ FROM war_history WHERE node > 0 AND node <= 55";

	private static final String DELETE_DATA = "DELETE FROM war_history WHERE group_id = ? AND war_date = ? AND opponent_tag = TO_BASE64(?)";

	/* @formatter:off */
	private static final String ADD_DATA_PLACEMENT_STATEMENT = 
			"INSERT INTO war_history (group_id, node, num_deaths, champion, player, opponent_tag, war_date, history_type) " + 
			"(SELECT WS.group_id, WP.node, -1, IFNULL(WP.champ, ''), " + 
			"		WS.name, TO_BASE64(?), ?, ? " + 
			"FROM war_summoner AS WS " + 
			"	JOIN war_placement AS WP ON (WS.id = WP.summoner_id) " + 
			"WHERE WS.group_id = ? " + 
			"ORDER BY WS.id, WP.id)";
	/* @formatter:on */

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

			LOG.info("Deleted " + deletedEntries + " in history for " + allianceTag + " on " + timestamp);

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

		return (deletedEntries > 0);
	}

	/**
	 * get all the history for a particular day (death reports)
	 * 
	 * @param groupKey
	 * @param warDate
	 * @return map where key=ally_tag
	 * @throws SQLException
	 */
	public Map<String, WarGroup> getAllDataForDeaths(int groupKey, Timestamp warDate) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, WarGroup> outcome = new TreeMap<>();
		try {

			stmt = conn.prepareStatement(GET_DAILY_DATA);
			stmt.setInt(1, groupKey);
			stmt.setTimestamp(2, warDate);
			stmt.setInt(3, HistoryType.HistoryTypeDeathReport.getValue());
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

		return outcome;
	}

	/**
	 * get all the history for a particular day (death reports)
	 * 
	 * @param groupKey
	 * @param warDate
	 * @return map where key=ally_tag
	 * @throws SQLException
	 */
	public Map<String, Map<Integer, WarSummoner>> getAllDataForReports(int groupKey, Timestamp warDate)
	        throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, Map<Integer, WarSummoner>> outcome = new HashMap<>();

		try {

			stmt = conn.prepareStatement(GET_DAILY_DATA);
			stmt.setInt(1, groupKey);
			stmt.setTimestamp(2, warDate);
			stmt.setInt(3, HistoryType.HistoryTypePlacementReport.getValue());
			rs = stmt.executeQuery();

			char placementPos = 'A';
			int summonerPos = 0;
			String lastSummonerName = UUID.randomUUID().toString();
			while (rs.next()) {
				String summonerName = rs.getString("player_name");
				String allianceName = rs.getString("ally_tag");
				int placementNode = rs.getInt("node");
				String placementChamp = rs.getString("champ");
				if (placementChamp.isEmpty())
					placementChamp = null;

				if (!lastSummonerName.equals(summonerName)) {
					lastSummonerName = summonerName;
					summonerPos++;
					placementPos = 'A';
				}

				if (!outcome.containsKey(allianceName)) {
					Map<Integer, WarSummoner> summoners = new TreeMap<>();
					outcome.put(allianceName, summoners);
				}

				Map<Integer, WarSummoner> summoners = outcome.get(allianceName);

				WarSummoner ws = summoners.get(summonerPos);
				if (ws == null) {
					ws = new WarSummoner(summonerName);
					summoners.put(summonerPos, ws);
				}

				ws.getPlacements().put(Character.valueOf(placementPos++),
				        new WarSummonerPlacement(placementNode, placementChamp));
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

		return outcome;
	}

	/**
	 * Retrieve all the history data. This can be a onerous operation
	 * 
	 * @return the stats
	 * @throws SQLException
	 */
	public List<HistoryStats> getStatsData() throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<HistoryStats> outcome = new ArrayList<>();
		try {

			stmt = conn.prepareStatement(GET_STATS_DATA);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Integer node = rs.getInt("node");
				Integer numDeaths = rs.getInt("num_deaths");
				String champ = rs.getString("champ");

				outcome.add(new HistoryStats(champ, node, numDeaths));
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
		// TODO: delete wars saved older than 24 (absolute number of saved wars)
		PreparedStatement stmt = null;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Timestamp timestamp = new Timestamp(c.getTimeInMillis());

		try {
			// Store the deaths if any
			stmt = conn.prepareStatement(ADD_DATA_DEATHS_STATEMENT);
			stmt.setString(1, allianceTag);
			stmt.setTimestamp(2, timestamp);
			stmt.setInt(3, groupKey);
			stmt.execute();
			stmt.close();

			// Store the placements if any
			stmt = conn.prepareStatement(ADD_DATA_PLACEMENT_STATEMENT);
			stmt.setString(1, allianceTag);
			stmt.setTimestamp(2, timestamp);
			stmt.setInt(3, HistoryType.HistoryTypePlacementReport.getValue());
			stmt.setInt(4, groupKey);
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

		LOG.info("Data stored successfully: " + allianceTag + ", " + timestamp);
	}

}
