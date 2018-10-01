package de.slux.line.friday.data.stats;

/**
 * @author slux
 *
 */
public class HistoryStats {

	private String champ;
	private int node;
	private int deaths;

	/**
	 * Ctor
	 */
	public HistoryStats(String champ, int node, int deaths) {
		this.champ = champ;
		this.node = node;
		this.deaths = deaths;
	}

	/**
	 * @return the champ
	 */
	public String getChamp() {
		return champ;
	}

	/**
	 * @param champ
	 *            the champ to set
	 */
	public void setChamp(String champ) {
		this.champ = champ;
	}

	/**
	 * @return the node
	 */
	public int getNode() {
		return node;
	}

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(int node) {
		this.node = node;
	}

	/**
	 * @return the deaths
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * @param deaths
	 *            the deaths to set
	 */
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

}
