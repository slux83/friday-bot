package de.slux.line.friday.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
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

import de.slux.line.friday.FridayBotApplication;
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
	public static final double CHAMP_MATCHING_THRESHOLD = 0.76d;

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

		FridayBotApplication.getInstance().setChampionsData(this.champions);
	}

	/**
	 * @return the champions
	 */
	public Map<String, String> getChampions() {
		return champions;
	}

	/**
	 * Update all the statistics
	 * 
	 * @return the updated description of the stats (some info)
	 * @throws SQLException
	 */
	public String updateStatistics() throws SQLException {
		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);
		List<HistoryStats> rawStats = dao.getStatsData();

		Map<Integer, List<HistoryStats>> nodeStats = updateNodeStats(rawStats);
		Map<String, List<HistoryStats>> champStats = updateChampStats(rawStats);

		Integer totalNodeElements = nodeStats.values().stream().mapToInt(List::size).sum();
		Integer totalChampElements = champStats.values().stream().mapToInt(List::size).sum();

		StringBuilder sb = new StringBuilder();
		sb.append("NODES: ");
		sb.append("Total nodes: ");
		sb.append(nodeStats.size());
		sb.append(". Total elements: ");
		sb.append(totalNodeElements);
		sb.append("\n");
		sb.append("CHAMPS: ");
		sb.append("Total nodes: ");
		sb.append(champStats.size());
		sb.append(". Total elements: ");
		sb.append(totalChampElements);
		sb.append("\n");

		FridayBotApplication.getInstance().setWarNodeStatistics(nodeStats);
		FridayBotApplication.getInstance().setWarChampStatistics(champStats);

		return sb.toString();
	}

	/**
	 * Updates the node statistics
	 * 
	 * @param rawStats
	 *            from DB
	 * @return the stats organized by node
	 */
	private Map<Integer, List<HistoryStats>> updateNodeStats(List<HistoryStats> rawStats) {
		Map<Integer, List<HistoryStats>> nodeStats = new HashMap<>();

		for (HistoryStats stat : rawStats) {
			String champ = stat.getChamp();

			Entry<Double, String> guess = guessChampion(champ, this.champions);

			if (guess.getKey() >= CHAMP_MATCHING_THRESHOLD) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Statistic selected for " + champ + " => " + guess.getValue() + " " + guess.getKey());
				}

				if (!nodeStats.containsKey(stat.getNode())) {
					nodeStats.put(stat.getNode(), new ArrayList<>());
				}

				List<HistoryStats> nodeStat = nodeStats.get(stat.getNode());
				nodeStat.add(new HistoryStats(guess.getValue(), stat.getNode(), stat.getDeaths()));
			}

		}

		return nodeStats;

	}

	/**
	 * Updates the champion statistics
	 * 
	 * @param rawStats
	 *            from DB
	 * @return the stats organized by champions
	 */
	private Map<String, List<HistoryStats>> updateChampStats(List<HistoryStats> rawStats) {
		Map<String, List<HistoryStats>> nodeStats = new HashMap<>();

		for (HistoryStats stat : rawStats) {
			String champ = stat.getChamp();
			Entry<Double, String> guess = guessChampion(champ, this.champions);

			if (guess.getKey() >= CHAMP_MATCHING_THRESHOLD) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Statistic selected for " + champ + " => " + guess.getValue() + " " + guess.getKey());
				}

				if (!nodeStats.containsKey(guess.getValue())) {
					nodeStats.put(guess.getValue(), new ArrayList<>());
				}

				List<HistoryStats> nodeStat = nodeStats.get(guess.getValue());
				nodeStat.add(new HistoryStats(guess.getValue(), stat.getNode(), stat.getDeaths()));
			}

		}

		return nodeStats;
	}

	/**
	 * Get the top-5 entries for a node
	 * 
	 * @param champStats
	 * @param champ
	 * @return the stats
	 */
	public String getChampionStats(Map<String, List<HistoryStats>> champStats, String champ) {
		Map<String, String> championsList = FridayBotApplication.getInstance().getChampionsData();
		if (championsList == null) {
			return "Champions data is not yet available. Please try later";
		}

		Entry<Double, String> guess = guessChampion(champ, championsList);

		// We pump a little the matching threshold
		if (guess.getKey() < CHAMP_MATCHING_THRESHOLD + 0.1) {
			return "The provided champion '" + champ
			        + "' cannot be properly matched, please try to be more specific. The closest guess was: "
			        + guess.getValue() + ".\nYou can always take a look at the full champions list at "
			        + CHAMPS_LIST_URL;
		}

		List<HistoryStats> champData = champStats.get(guess.getValue());
		if (champData == null) {
			return "Cannot find any War Statistics for champion " + champ;
		}
		Map<Integer, NodeStats> nodeAggr = new HashMap<>();

		for (HistoryStats s : champData) {
			int totDeaths = s.getDeaths() < 0 ? 0 : s.getDeaths();
			int deathItems = s.getDeaths() < 0 ? 0 : 1;
			if (!nodeAggr.containsKey(s.getNode())) {
				nodeAggr.put(s.getNode(), new NodeStats(s.getNode(), 1, totDeaths, deathItems));
			} else {
				NodeStats ns = nodeAggr.get(s.getNode());
				ns.setOccurrences(ns.getOccurrences() + 1);
				ns.setTotalDeaths(ns.getTotalDeaths() + totDeaths);
				ns.setDeathItems(ns.getDeathItems() + deathItems);
			}
		}

		List<Entry<Integer, NodeStats>> topNodes = nodeAggr.entrySet().stream()
		        .sorted(Map.Entry.comparingByValue(Comparator.comparing(NodeStats::getOccurrences).reversed())).limit(5)
		        .collect(Collectors.toList());

		StringBuilder sb = new StringBuilder("WAR Stats for ");
		sb.append(guess.getValue());
		for (Entry<Integer, NodeStats> agg : topNodes) {
			NodeStats ns = agg.getValue();
			double deathPercentage = (ns.getTotalDeaths() * 100.0) / ns.getDeathItems();
			sb.append("\n\n * NODE ");
			sb.append(agg.getKey());
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

	/**
	 * Guess champ based on the champions list
	 * 
	 * @param champ
	 * @param champions
	 * @return a single entry map with the best match
	 */
	public static Entry<Double, String> guessChampion(String champ, Map<String, String> champions) {
		JaroWinklerDistance distance = new JaroWinklerDistance();

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
		for (Map.Entry<String, String> ogChamp : champions.entrySet()) {
			double d = distance.apply(champ, ogChamp.getKey().toLowerCase());

			if (d > distanceValue) {
				distanceValue = d;
				recognizedAs = ogChamp;
			}
		}

		return new AbstractMap.SimpleEntry<Double, String>(distanceValue, recognizedAs.getValue());
	}

}
