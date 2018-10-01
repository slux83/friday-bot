package de.slux.line.friday.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author slux
 *
 */
public class StatsLogic {
	private static Logger LOG = LoggerFactory.getLogger(StatsLogic.class);
	private static final String CHAMPS_LIST_URL = "https://pastebin.com/raw/K8Xvdmtd";

	/** Key=champ_alias, Val=champ_description */
	private Map<String, String> champions;

	/**
	 * Ctor.
	 * <p>
	 * Fetch the champions list and create the internal data
	 * </p>
	 * 
	 * @throws Exception
	 */
	public StatsLogic() throws Exception {
		this.champions = new HashMap<>();
		fetchChamps();
	}

	private void fetchChamps() throws Exception {
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

}
