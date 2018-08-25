package de.slux.line.jarvis.data.war;

/**
 * The report submitted by individuals
 * 
 * @author slux
 */
public class WarDeath {

	private int nodeNumber;
	private int nodeDeaths;
	private String champName;
	private String userName;

	/**
	 * @param nodeNumber
	 * @param nodeDeaths
	 * @param champName
	 * @param userName
	 */
	public WarDeath(int nodeNumber, int nodeDeaths, String champName, String userName) {
		this.nodeNumber = nodeNumber;
		this.nodeDeaths = nodeDeaths;
		this.champName = champName;
		this.userName = userName;
	}

	/**
	 * @return the nodeNumber
	 */
	public int getNodeNumber() {
		return nodeNumber;
	}

	/**
	 * @param nodeNumber
	 *            the nodeNumber to set
	 */
	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	/**
	 * @return the nodeDeaths
	 */
	public int getNodeDeaths() {
		return nodeDeaths;
	}

	/**
	 * @param nodeDeaths
	 *            the nodeDeaths to set
	 */
	public void setNodeDeaths(int nodeDeaths) {
		this.nodeDeaths = nodeDeaths;
	}

	/**
	 * @return the champName
	 */
	public String getChampName() {
		return champName;
	}

	/**
	 * @param champName
	 *            the champName to set
	 */
	public void setChampName(String champName) {
		this.champName = champName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WarDeathReport [nodeNumber=" + nodeNumber + ", nodeDeaths=" + nodeDeaths + ", champName=" + champName
		        + "]";
	}

}
