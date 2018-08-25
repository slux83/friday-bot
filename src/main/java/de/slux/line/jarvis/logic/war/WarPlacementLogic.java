/**
 * 
 */
package de.slux.line.jarvis.logic.war;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.jarvis.dao.DbConnectionPool;
import de.slux.line.jarvis.dao.war.WarSummonerDao;
import de.slux.line.jarvis.data.war.WarSummoner;
import de.slux.line.jarvis.data.war.WarSummonerPlacement;

/**
 * War placement logic to save the various nodes during war
 * 
 * @author slux
 */
public class WarPlacementLogic {
	public static final int MAX_SUMMONERS = 10;
	private static Logger LOG = LoggerFactory.getLogger(WarPlacementLogic.class);

	/**
	 * Ctor
	 */
	public WarPlacementLogic() {
	}

	/**
	 * Add a list of summoners in the current placement schema
	 * 
	 * @param groupId
	 * @param summoners
	 * @throws Exception
	 */
	public void addSummoners(String groupId, List<String> summoners) throws Exception {
		int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarSummonerDao dao = new WarSummonerDao(conn);

		dao.storeData(groupKey, summoners);
	}

	/**
	 * Get all summoners
	 * 
	 * @param groupId
	 * @return the map of summoners
	 * @throws Exception
	 */
	public Map<Integer, WarSummoner> getSummoners(String groupId) throws Exception {
		int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarSummonerDao dao = new WarSummonerDao(conn);

		return dao.getAll(groupKey);
	}

	/**
	 * Edit a placement
	 * 
	 * @param groupId
	 * @param summonerPos
	 * @param placementPos
	 * @param node
	 * @param champ
	 * @throws Exception
	 */
	public void editPlacement(String groupId, Integer summonerPos, Character placementPos, Integer node, String champ)
	        throws Exception {
		int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarSummonerDao dao = new WarSummonerDao(conn);

		dao.editPlacement(groupKey, summonerPos, placementPos, node, champ);
	}

	/**
	 * Stringify the map of summoners for printing/posting
	 * 
	 * @param summoners
	 * @return the string for the map
	 */
	public String getSummonersText(Map<Integer, WarSummoner> summoners) {
		StringBuilder sb = new StringBuilder("*** CURRENT WAR PLACEMENTS ***\n\n");
		for (Entry<Integer, WarSummoner> entry : summoners.entrySet()) {
			sb.append(entry.getKey().toString());
			sb.append(". ");
			sb.append(entry.getValue().getName());
			sb.append("\n");

			for (Entry<Character, WarSummonerPlacement> placement : entry.getValue().getPlacements().entrySet()) {
				sb.append("  ");
				sb.append(placement.getKey().toString());
				sb.append(". ");
				if (placement.getValue().getChampion() != null) {
					sb.append(placement.getValue().getChampion());
					sb.append(" (");
					sb.append(placement.getValue().getNode().toString());
					sb.append(")\n");
				} else {
					sb.append("\n");
				}
			}

			sb.append("\n");
		}
		return sb.toString();
	}
}
