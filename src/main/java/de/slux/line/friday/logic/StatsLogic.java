package de.slux.line.friday.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarHistoryDao;
import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.data.stats.NodeStats;

/**
 * @author slux
 *
 */
public class StatsLogic {
	private static Logger LOG = LoggerFactory.getLogger(StatsLogic.class);
	private static final String CHAMPS_LIST_URL = "https://pastebin.com/raw/K8Xvdmtd";
	private static final double CHAMP_MATCHING_THRESHOLD = 0.76d;

	/** Key=champ_alias, Val=champ_description */
	private Map<String, String> champions;

	/**
	 * Ctor.
	 * <p>
	 * Fetch the champions list and create the internal data
	 * </p>
	 * 
	 * @param fetch
	 *            fetchs the champs if true
	 * @throws IOException
	 * @throws Exception
	 */
	public StatsLogic(boolean fetch) throws IOException {
		this.champions = new HashMap<>();
		if (fetch)
			fetchChamps();
	}

	private void fetchChamps() throws IOException {
		LOG.info("Fetching champions from PasteBin");

		URL url = new URL(CHAMPS_LIST_URL);

		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

		String line = null;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.indexOf('=') == -1) {
				this.champions.put(line, line);
			} else {
				String champName = line.substring(0, line.indexOf('='));
				String aliasesChunk = line.substring(line.indexOf('=') + 1);
				String[] aliases = aliasesChunk.split(",");
				this.champions.put(champName, champName);
				for (String alias : aliases) {
					this.champions.put(alias.trim(), champName);
				}

			}
		}

		LOG.info("Fetched " + this.champions.size() + " champion(s)");
	}

	/**
	 * @return the champions
	 */
	public Map<String, String> getChampions() {
		return champions;
	}

	/**
	 * Updates the node statistics
	 * 
	 * @return
	 * 
	 * @throws SQLException
	 */
	public Map<Integer, List<HistoryStats>> updateNodeStats() throws SQLException {
		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);
		List<HistoryStats> stats = dao.getStatsData();
		Map<Integer, List<HistoryStats>> nodeStats = new HashMap<>();

		JaroWinklerDistance distance = new JaroWinklerDistance();
		for (HistoryStats stat : stats) {
			String champ = stat.getChamp();
			champ = champ.toLowerCase();
			champ = champ.replaceAll("[1-6]star", "");
			champ = champ.replaceAll("[1-6]\\*", "");
			champ = champ.replaceAll("duped", "");
			champ = champ.replaceAll("unduped", "");
			champ = champ.replaceAll("undupe", "");
			champ = champ.replaceAll("dupe", "");
			champ = champ.replaceAll("r[0-9]/[0-9][0-9]", "");
			champ = champ.replaceAll("r[0-9]", "");
			champ = champ.replaceAll("mini", "");
			champ = champ.replaceAll("miniboss", "");
			champ = champ.replaceAll("boss", "");
			champ = champ.replaceAll("rank\\s?[0-9]", "");
			champ = champ.replaceAll("mutant", "");
			champ = champ.replaceAll("skill", "");
			champ = champ.replaceAll("tech", "");
			champ = champ.replaceAll("mystic", "");
			champ = champ.replaceAll("cosmic", "");
			champ = champ.replaceAll("empty", "");
			champ = champ.replaceAll("open", "");
			champ = champ.replaceAll("hidden", "");
			champ = champ.replaceAll("none", "");
			champ = champ.trim();

			double distanceValue = -1.0;
			Entry<String, String> recognizedAs = null;
			for (Map.Entry<String, String> ogChamp : this.champions.entrySet()) {
				double d = distance.apply(champ, ogChamp.getKey().toLowerCase());

				if (d > distanceValue) {
					distanceValue = d;
					recognizedAs = ogChamp;
				}
			}
			if (distanceValue >= CHAMP_MATCHING_THRESHOLD) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Statistic selected for " + champ + " => " + recognizedAs + " " + distanceValue);
				}

				if (!nodeStats.containsKey(stat.getNode())) {
					nodeStats.put(stat.getNode(), new ArrayList<>());
				}

				List<HistoryStats> nodeStat = nodeStats.get(stat.getNode());
				nodeStat.add(new HistoryStats(recognizedAs.getValue(), stat.getNode(), stat.getDeaths()));
			}

		}

		return nodeStats;

	}

	/**
	 * Get the top-5 entries for a node
	 * 
	 * @param nodeStats
	 * @param node
	 * @return the stats
	 */
	public String getNodeStats(Map<Integer, List<HistoryStats>> nodeStats, int node) {
		List<HistoryStats> nodeData = nodeStats.get(node);
		if (nodeData == null) {
			return "Cannot find any War Statistics for node " + node;
		}

		Map<String, NodeStats> nodeAggr = new HashMap<>();

		for (HistoryStats s : nodeData) {
			int totDeaths = s.getDeaths() < 0 ? 0 : s.getDeaths();
			int deathItems = s.getDeaths() < 0 ? 0 : 1;
			if (!nodeAggr.containsKey(s.getChamp())) {
				nodeAggr.put(s.getChamp(), new NodeStats(s.getChamp(), 1, totDeaths, deathItems));
			} else {
				NodeStats ns = nodeAggr.get(s.getChamp());
				ns.setOccurrences(ns.getOccurrences() + 1);
				ns.setTotalDeaths(ns.getTotalDeaths() + totDeaths);
				ns.setDeathItems(ns.getDeathItems() + deathItems);
			}
		}

		List<Entry<String, NodeStats>> topNodes = nodeAggr.entrySet().stream()
		        .sorted(Map.Entry.comparingByValue(Comparator.comparing(NodeStats::getOccurrences).reversed())).limit(5)
		        .collect(Collectors.toList());

		StringBuilder sb = new StringBuilder("WAR Node ");
		sb.append(node);
		sb.append(" Stats:");
		for (Entry<String, NodeStats> agg : topNodes) {
			NodeStats ns = agg.getValue();
			double deathPercentage = (ns.getTotalDeaths() * 100.0) / ns.getDeathItems();
			sb.append("\n\n * ");
			sb.append(ns.getChamp());
			sb.append(" *\n");
			sb.append("Placed ");
			sb.append(ns.getOccurrences());
			sb.append(" time(s)\n");
			sb.append(ns.getTotalDeaths());
			sb.append(" total deaths\n");
			sb.append("Mortality ");
			if (!Double.isNaN(deathPercentage)) {
				sb.append(String.format("%.1f", deathPercentage));
				sb.append("%");
			} else {
				sb.append("---");
			}
		}

		return sb.toString();
	}

}
