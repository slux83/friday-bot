/**
 * 
 */
package de.slux.line.jarvis.dao.war;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.jarvis.dao.DbConnectionPool;
import de.slux.line.jarvis.dao.exception.GenericDaoException;
import de.slux.line.jarvis.dao.exception.SummonerNotFoundException;
import de.slux.line.jarvis.dao.exception.SummonerNumberExceededException;
import de.slux.line.jarvis.data.war.WarSummoner;
import de.slux.line.jarvis.data.war.WarSummonerPlacement;

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
	private static final String UPDATE_PLACEMENT = "UPDATE war_placement SET node = ?, champ = ? WHERE id = ?";
	private static final String RENAME_SUMMONER = "UPDATE war_summoner SET name = ? WHERE id = ?";
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
	 * @throws SummonerNumberExceededException
	 *             if you add more then {@link WarSummonerDao#MAX_SUMMONERS}
	 * @throws SQLException
	 * @throws GenericDaoException
	 * @throws Exception
	 */
	public void storeData(int groupId, List<String> summoners)
	        throws SummonerNumberExceededException, SQLException, GenericDaoException {
		PreparedStatement stmt = null;
		try {
			Map<Integer, WarSummoner> existingSummoners = getAll(groupId);
			if (existingSummoners.size() + summoners.size() > MAX_SUMMONERS) {
				throw new SummonerNumberExceededException("You can add a maximum of " + MAX_SUMMONERS
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
					throw new GenericDaoException("Something wrong with the insertion of the summoners: " + summoners
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
		Map<Integer, WarSummoner> summoners = new TreeMap<>();
		try {

			stmt = conn.prepareStatement(GET_DATA);
			stmt.setInt(1, groupId);
			rs = stmt.executeQuery();

			char placementPos = 'A';
			int summonerPos = 0;
			int lastSummonerId = -1;
			while (rs.next()) {
				int summonerId = rs.getInt("ws_id");

				if (lastSummonerId != summonerId) {
					lastSummonerId = summonerId;
					summonerPos++;
					placementPos = 'A';
				}

				String summonerName = rs.getString("ws_name");
				summonerName = new String(Base64.getDecoder().decode(summonerName.getBytes()));
				int placementId = rs.getInt("wp_id");
				int placementNode = rs.getInt("wp_node");
				String placementChamp = rs.getString("wp_champ");
				if (placementChamp != null)
					placementChamp = new String(Base64.getDecoder().decode(placementChamp.getBytes()));

				WarSummoner ws = summoners.get(summonerPos);
				if (ws == null) {
					ws = new WarSummoner(summonerId, summonerName);
					summoners.put(summonerPos, ws);
				}

				ws.getPlacements().put(Character.valueOf(placementPos++),
				        new WarSummonerPlacement(placementId, placementNode, placementChamp));

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

	/**
	 * Rename the name of a summoner in the current list
	 * 
	 * @param groupId
	 * @param summonerPos
	 *            [1-10]
	 * @param name
	 * @throws SQLException
	 * @throws SummonerNotFoundException
	 */
	public void renameSummoner(Integer groupId, Integer summonerPos, String name)
	        throws SQLException, SummonerNotFoundException {
		Map<Integer, WarSummoner> summoners = getAll(groupId);
		WarSummoner summoner = summoners.get(summonerPos);
		if (summoner == null) {
			throw new SummonerNotFoundException("Sorry, cannot find the specified summoner at position " + summonerPos);
		}

		int summonerDbKey = summoner.getId();

		// getAll() will close it
		if (this.conn.isClosed()) {
			this.conn = DbConnectionPool.getConnection();
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(RENAME_SUMMONER);
			stmt.setString(1, Base64.getEncoder().encodeToString(name.getBytes()));
			stmt.setInt(2, summonerDbKey);
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
	}

	/**
	 * edit a placement for a given summoner and group
	 * 
	 * @param groupId
	 * @param summonerPos
	 *            [1-10]
	 * @param placementPos
	 *            [A-E]
	 * @param node
	 * @param champ
	 * @throws SQLException
	 * @throws SummonerNotFoundException
	 * @throws GenericDaoException
	 */
	public void editPlacement(Integer groupId, Integer summonerPos, Character placementPos, Integer node, String champ)
	        throws SQLException, SummonerNotFoundException, GenericDaoException {
		Map<Integer, WarSummoner> summoners = getAll(groupId);
		WarSummoner summoner = summoners.get(summonerPos);
		if (summoner == null) {
			throw new SummonerNotFoundException("Sorry, cannot find the specified summoner at position " + summonerPos);
		}

		WarSummonerPlacement placement = summoner.getPlacements().get(placementPos);

		if (placement == null) {
			// If this happens, it's clearly a bug
			throw new GenericDaoException("Cannot find position '" + placementPos + "' for summoner "
			        + summoner.getName() + " (position=" + summonerPos + ")");
		}

		int placementDbKey = placement.getId();

		// getAll() will close it
		if (this.conn.isClosed()) {
			this.conn = DbConnectionPool.getConnection();
		}

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(UPDATE_PLACEMENT);
			stmt.setInt(1, node);
			stmt.setString(2, Base64.getEncoder().encodeToString(champ.getBytes()));
			stmt.setInt(3, placementDbKey);
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
	}
}
