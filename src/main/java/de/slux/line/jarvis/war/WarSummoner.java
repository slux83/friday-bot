package de.slux.line.jarvis.war;

import java.util.ArrayList;
import java.util.List;

/**
 * Summoner pojo
 * 
 * @author slux
 */
public class WarSummoner {
	private Integer id;
	private String name;
	private List<WarSummonerPlacement> placements;

	/**
	 * @param id
	 * @param name
	 */
	public WarSummoner(Integer id, String name) {
		super();
		this.placements = new ArrayList<>();
		this.id = id;
		this.name = name;
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
	public List<WarSummonerPlacement> getPlacements() {
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

}
