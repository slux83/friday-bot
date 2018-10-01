package de.slux.line.friday.test.stats;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.data.stats.NodeStats;
import de.slux.line.friday.logic.StatsLogic;

/**
 * @author Slux
 */
public class StatsTest {

	@Test
	public void someTest() throws Exception {
		StatsLogic logic = new StatsLogic();
		Map<Integer, List<HistoryStats>> nodeStats = logic.updateNodeStats();

		System.out.println(nodeStats.keySet());

		List<HistoryStats> node = nodeStats.get(29);

		Map<String, NodeStats> nodeAggr = new HashMap<>();

		for (HistoryStats s : node) {
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

		List<Entry<String, NodeStats>> newMap = nodeAggr.entrySet().stream()
		        .sorted(Map.Entry.comparingByValue(Comparator.comparing(NodeStats::getOccurrences).reversed())).limit(5)
		        .collect(Collectors.toList());

		for (Entry<String, NodeStats> agg : newMap) {
			NodeStats ns = agg.getValue();
			double deathPercentage = (ns.getTotalDeaths() * 100.0) / ns.getDeathItems();
			System.out.println(ns + " mortality: " + String.format("%.1f", deathPercentage) + "%");
		}
	}

}
