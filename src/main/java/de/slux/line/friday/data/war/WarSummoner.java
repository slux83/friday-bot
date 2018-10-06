package de.slux.line.friday.data.war;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Summoner pojo
 * 
 * @author slux
 */
public class WarSummoner {
	private Integer id;
	private String name;
	private Map<Character, WarSummonerPlacement> placements;

	/**
	 * @param id
	 * @param name
	 */
	public WarSummoner(Integer id, String name) {
		super();
		this.placements = new TreeMap<>();
		this.id = id;
		this.name = name;
	}

	/**
	 * @param name
	 */
	public WarSummoner(String name) {
		this(-1, name);
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the placements
	 */
	public Map<Character, WarSummonerPlacement> getPlacements() {
		return placements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WarSummoner [id=" + id + ", name=" + name + ", placements=" + placements + "]";
	}

	/**
	 * Checks if the node already exists
	 * 
	 * @param node
	 * @param placementPos
	 * @param summonerPos
	 * @return
	 */
	public boolean nodeExists(int node, Integer summonerPos, Character placementPos) {
		if (node <= 0)
			return false;

		for (Entry<Character, WarSummonerPlacement> entries : this.placements.entrySet()) {
			if (this.id != summonerPos && entries.getKey() != placementPos && entries.getValue().getNode() == node)
				return true;
		}

		return false;
	}

}
