/**
 * 
 */
package de.slux.line.friday.data.war;

/**
 * Diversity bean for war
 * 
 * @author Slux
 */
public class WarDiversity {

	public static final String UNKNOWN_CHAMP = "unknown";

	private Integer node;
	private String rawInputData;
	private String normalizedChampionName;

	/**
	 * Ctor.
	 * 
	 * @param node
	 * @param rawInputData
	 * @param normalizedChampionName
	 */
	public WarDiversity(Integer node, String rawInputData, String normalizedChampionName) {
		super();
		this.node = node;
		this.rawInputData = rawInputData;
		this.normalizedChampionName = normalizedChampionName;

		if (normalizedChampionName == null)
			this.normalizedChampionName = UNKNOWN_CHAMP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((normalizedChampionName == null) ? 0 : normalizedChampionName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WarDiversity other = (WarDiversity) obj;
		if (normalizedChampionName == null) {
			if (other.normalizedChampionName != null)
				return false;
		} else if (!normalizedChampionName.equals(other.normalizedChampionName))
			return false;
		return true;
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
	 * @return the rawInputData
	 */
	public String getRawInputData() {
		return rawInputData;
	}

	/**
	 * @param rawInputData
	 *            the rawInputData to set
	 */
	public void setRawInputData(String rawInputData) {
		this.rawInputData = rawInputData;
	}

	/**
	 * @return the normalizedChampionName
	 */
	public String getNormalizedChampionName() {
		return normalizedChampionName;
	}

	/**
	 * @param normalizedChampionName
	 *            the normalizedChampionName to set
	 */
	public void setNormalizedChampionName(String normalizedChampionName) {
		this.normalizedChampionName = normalizedChampionName;
	}

	/**
	 * is normalised champion name unknown?
	 * 
	 * @return
	 */
	public boolean isUnknown() {
		return UNKNOWN_CHAMP.equals(this.normalizedChampionName);
	}

}
