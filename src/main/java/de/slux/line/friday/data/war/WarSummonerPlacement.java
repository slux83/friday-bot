/**
 * 
 */
package de.slux.line.friday.data.war;

/**
 * Summoner placement pojo
 * 
 * @author slux
 */
public class WarSummonerPlacement {
	private Integer id;
	private Integer node;
	private String champion;

	/**
	 * @param id
	 * @param node
	 * @param champion
	 */
	public WarSummonerPlacement(Integer id, Integer node, String champion) {
		super();
		this.id = id;
		this.node = node;
		this.champion = champion;
	}

	/**
	 * @param node
	 * @param champion
	 */
	public WarSummonerPlacement(Integer node, String champion) {
		this(-1, node, champion);
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the node
	 */
	public Integer getNode() {
		return node;
	}

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(Integer node) {
		this.node = node;
	}

	/**
	 * @return the champion
	 */
	public String getChampion() {
		return champion;
	}

	/**
	 * @param champion
	 *            the champion to set
	 */
	public void setChampion(String champion) {
		this.champion = champion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WarSummonerPlacement [id=" + id + ", node=" + node + ", champion=" + champion + "]";
	}

}
