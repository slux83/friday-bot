/**
 * 
 */
package de.slux.line.jarvis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.jarvis.war.WarSummoner;
import de.slux.line.jarvis.war.WarSummonerPlacement;

/**
 * @author slux
 */
public class WarSummonerDao {
	private static final int MAX_SUMMONERS = 10;
	private static Logger LOG = LoggerFactory.getLogger(WarSummonerDao.class);

	/* @formatter:off */
	private static final String ADD_DATA_STATEMENT = "INSERT INTO war_summoner (group_id, name) VALUES (?, ?)";
	private static final String GET_DATA = 
		"SELECT WS.id AS ws_id, WS.group_id AS ws_gid, WS.name AS ws_name, " + 
				"WP.id AS wp_id, WP.node AS wp_node, WP.champ AS wp_champ " +  
		"FROM war_summoner AS WS " +
			"JOIN war_placement AS WP ON (WS.id = WP.summoner_id) " + 
		"WHERE WS.group_id = ? " +
		"ORDER BY WS.id, WP.id";
	private static final String ADD_PLACEMENTS = "INSERT INTO war_placement (summoner_id) VALUES (?), (?), (?), (?), (?)";
	/* @formatter:on */

	private Connection conn;

	public WarSummonerDao(Connection conn) {
		this.conn = conn;
	}

	/**
	 * Store a new list of summoners, max 10
	 * 
	 * @param groupId
	 * @param summoners
	 * @throws Exception
	 */
	public void storeData(int groupId, List<String> summoners) throws Exception {
		PreparedStatement stmt = null;
		try {
			Map<Integer, WarSummoner> existingSummoners = getAll(groupId);
			if (existingSummoners.size() + summoners.size() > MAX_SUMMONERS) {
				throw new Exception("You can add a maximum of " + MAX_SUMMONERS
				        + " summoners, and you have already added " + existingSummoners.size());
			}

			// getAll() will close it
			if (this.conn.isClosed()) {
				this.conn = DbConnectionPool.getConnection();
			}

			for (String summoner : summoners) {
				stmt = conn.prepareStatement(ADD_DATA_STATEMENT, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, groupId);
				stmt.setString(2, Base64.getEncoder().encodeToString(summoner.getBytes()));
				stmt.executeUpdate();

				int insertedKey = -1;

				ResultSet rs = stmt.getGeneratedKeys();

				if (rs.next()) {
					insertedKey = rs.getInt(1);
				} else {
					throw new Exception("Something wrong with the insertion of the summoners: " + summoners
					        + ". Cannot retrieve the inserted key");
				}

				stmt.close();

				addEmptyPlacement(insertedKey);
			}
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

		LOG.info("Data stored successfully: " + groupId + " (" + summoners + ")");
	}

	/**
	 * Add 5 default placements for a given summoner key
	 * <p>
	 * <strong>NOTE:</strong> This does not close the connection
	 * </p>
	 * 
	 * @param summonerKey
	 * @throws SQLException
	 */
	private void addEmptyPlacement(int summonerKey) throws SQLException {
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(ADD_PLACEMENTS);
			stmt.setInt(1, summonerKey);
			stmt.setInt(2, summonerKey);
			stmt.setInt(3, summonerKey);
			stmt.setInt(4, summonerKey);
			stmt.setInt(5, summonerKey);
			stmt.execute();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				// FIXME: replace all the e.printStackTrace()
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get all the summoners for a group
	 * 
	 * @throws SQLException
	 */
	public Map<Integer, WarSummoner> getAll(int groupId) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<Integer, WarSummoner> summoners = new HashMap<>();
		try {

			stmt = conn.prepareStatement(GET_DATA);
			stmt.setInt(1, groupId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				int summonerId = rs.getInt("ws_id");
				String summonerName = rs.getString("ws_name");
				summonerName = new String(Base64.getDecoder().decode(summonerName.getBytes()));
				int placementId = rs.getInt("wp_id");
				int placementNode = rs.getInt("wp_node");
				String placementChamp = rs.getString("wp_champ");
				if (placementChamp != null)
					placementChamp = new String(Base64.getDecoder().decode(placementChamp.getBytes()));

				WarSummoner ws = summoners.get(summonerId);
				if (ws == null) {
					ws = new WarSummoner(summonerId, summonerName);
					summoners.put(summonerId, ws);
				}

				ws.getPlacements().add(new WarSummonerPlacement(placementId, placementNode, placementChamp));

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

		return summoners;
	}
}
