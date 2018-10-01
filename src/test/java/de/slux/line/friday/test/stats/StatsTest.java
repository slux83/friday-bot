package de.slux.line.friday.test.stats;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.junit.Test;

import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarHistoryDao;
import de.slux.line.friday.data.stats.HistoryStat;
import de.slux.line.friday.logic.StatsLogic;

/**
 * @author Slux
 */
public class StatsTest {

	@Test
	public void someTest() throws Exception {
		WarHistoryDao dao = new WarHistoryDao(DbConnectionPool.getConnection());

		List<HistoryStat> stats = dao.getStatsData();

		System.out.println(stats.size());

		StatsLogic logic = new StatsLogic();

		Map<String, String> champs = logic.getChampions();
		System.out.println(champs);
		int tot = 0;
		JaroWinklerDistance distance = new JaroWinklerDistance();
		for (HistoryStat stat : stats) {
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
			for (Map.Entry<String, String> ogChamp : champs.entrySet()) {
				double d = distance.apply(champ, ogChamp.getKey().toLowerCase());

				if (d > distanceValue) {
					distanceValue = d;
					recognizedAs = ogChamp;
				}
			}
			if (distanceValue >= 0.76d) {
				System.err.println("SELECTED FOR " + champ + " => " + recognizedAs + " " + distanceValue);
				tot++;
			}
			
		}
		
		System.out.println("TOT: " + tot);
	}

}
