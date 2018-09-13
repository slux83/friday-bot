package de.slux.line.friday.data.war;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * The status of the war of a specific group. This is also used to gather group
 * information in general
 * 
 * @author slux
 */
public class WarGroup {
	public enum HistoryType {
		HistoryTypeDeathReport(0), HistoryTypePlacementReport(1);

		private final int value;

		private HistoryType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum GroupStatus {
		GroupStatusInactive(0), GroupStatusActive(1);

		private final int value;

		private GroupStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum GroupFeature {
		GroupFeatureWar(1),
		GroupFeatureEvent(2),
		GroupFeatureWarEvent(3);

		private final int value;

		private GroupFeature(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private List<WarDeath> deathReports;
	private String groupId;
	private String groupName;
	private GroupStatus groupStatus;
	private GroupFeature groupFeature;

	/**
	 * Ctor
	 */
	public WarGroup() {
		this.deathReports = new ArrayList<WarDeath>();
	}

	/**
	 * Reset the internal death reports
	 */
	public void reset() {
		this.deathReports.clear();
	}

	/**
	 * @return the deathReports
	 */
	public List<WarDeath> getDeathReports() {
		return deathReports;
	}

	/**
	 * Add death report
	 * 
	 * @param deaths
	 * @param node
	 * @param champName
	 * @param userName
	 */
	public void addDeath(int deaths, int node, String champName, String userName) {
		this.deathReports.add(new WarDeath(node, deaths, champName, userName));

	}

	/**
	 * Get the war death report
	 * 
	 * @return string that describes the report
	 */
	public String getReport() {
		StringBuilder report = new StringBuilder("*** WAR DEATH REPORT ***");

		TotalDeathReport totDeathReport = calculateDeathReport();

		report.append("\nTotal lost points: " + totDeathReport.getTotalLostPoints());
		report.append("\nTotal deaths: " + totDeathReport.getTotalDeaths());

		return report.toString();
	}

	/**
	 * Calculate the current death report
	 * 
	 * @return the current death report
	 */
	public TotalDeathReport calculateDeathReport() {
		int totalLostPoints = 0;
		int totalDeaths = 0;
		// Node, deaths
		Map<Integer, Integer> totalDeathsPerNode = new HashMap<Integer, Integer>();
		for (WarDeath wdr : this.deathReports) {
			if (!totalDeathsPerNode.containsKey(wdr.getNodeNumber()))
				totalDeathsPerNode.put(wdr.getNodeNumber(), 0);

			totalDeathsPerNode.put(wdr.getNodeNumber(),
			        wdr.getNodeDeaths() + totalDeathsPerNode.get(wdr.getNodeNumber()));
		}

		for (Integer deaths : totalDeathsPerNode.values()) {

			int nodeLostPoints = deaths * WarDeathLogic.WAR_POINTS_LOST_PER_DEATH;
			if (nodeLostPoints > WarDeathLogic.WAR_POINTS_LOST_CAP)
				nodeLostPoints = WarDeathLogic.WAR_POINTS_LOST_CAP;

			totalLostPoints += nodeLostPoints;
			totalDeaths += deaths;

		}
		return new TotalDeathReport(totalLostPoints, totalDeaths);
	}

	/**
	 * Get the summary as text
	 * 
	 * @return the human readable version of the getSummary()
	 */
	public List<String> getSummaryText() {
		List<String> outcome = new ArrayList<String>();

		StringBuilder sb = new StringBuilder("*** WAR DEATH SUMMARY ***\n");

		List<WarDeath> reports = getDeathReports();
		Collections.sort(reports, new Comparator<WarDeath>() {
			@Override
			public int compare(WarDeath o1, WarDeath o2) {
				return o1.getNodeNumber() - o2.getNodeNumber();
			}

		});

		for (WarDeath wd : reports) {

			if (sb.length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
				// We need to split it and clear
				outcome.add(sb.toString());
				sb.setLength(0);
			}

			sb.append("Node: ");
			sb.append(wd.getNodeNumber());
			sb.append("\nTotal Deaths: ");
			sb.append(wd.getNodeDeaths());
			sb.append("\nChampion: ");
			sb.append(wd.getChampName());
			sb.append("\nPlayer: ");
			sb.append(wd.getUserName());
			sb.append("\n\n");
		}

		if (reports.isEmpty())
			sb.append("Nothing to report\n\n");

		sb.append("\n");
		sb.append(getReport());

		outcome.add(sb.toString());

		return outcome;
	}

	/**
	 * Undo last inserted death report
	 */
	public void undoLast() {
		if (this.deathReports.size() > 0)
			this.deathReports.remove(this.deathReports.size() - 1);
	}

	/**
	 * Internal class to get the overall current death report status. This is an
	 * immutable class
	 * 
	 * @author Slux
	 *
	 */
	public class TotalDeathReport {
		int totalLostPoints;
		int totalDeaths;

		/**
		 * @param totalLostPoints
		 * @param totalDeaths
		 */
		public TotalDeathReport(int totalLostPoints, int totalDeaths) {
			super();
			this.totalLostPoints = totalLostPoints;
			this.totalDeaths = totalDeaths;
		}

		/**
		 * @return the totalLostPoints
		 */
		public int getTotalLostPoints() {
			return totalLostPoints;
		}

		/**
		 * @return the totalDeaths
		 */
		public int getTotalDeaths() {
			return totalDeaths;
		}

	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the groupStatus
	 */
	public GroupStatus getGroupStatus() {
		return groupStatus;
	}

	/**
	 * @param groupStatus
	 *            the groupStatus to set
	 */
	public void setGroupStatus(GroupStatus groupStatus) {
		this.groupStatus = groupStatus;
	}

	/**
	 * @return the groupFeature
	 */
	public GroupFeature getGroupFeature() {
		return groupFeature;
	}

	/**
	 * @param groupFeature
	 *            the groupFeature to set
	 */
	public void setGroupFeature(GroupFeature groupFeature) {
		this.groupFeature = groupFeature;
	}

	/**
	 * get the status from the integer
	 * 
	 * @param status
	 * @return the status enum or null
	 */
	public static GroupStatus statusOf(int status) {
		if (GroupStatus.GroupStatusActive.getValue() == status) {
			return GroupStatus.GroupStatusActive;
		}

		if (GroupStatus.GroupStatusInactive.getValue() == status) {
			return GroupStatus.GroupStatusInactive;
		}

		return null;
	}

	/**
	 * get the feature from the integer
	 * 
	 * @param feature
	 * @return the feature enum or null
	 */
	public static GroupFeature featureOf(int feature) {
		if (GroupFeature.GroupFeatureWar.getValue() == feature) {
			return GroupFeature.GroupFeatureWar;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WarGroup [deathReports=" + deathReports + ", groupId=" + groupId + ", groupName=" + groupName
		        + ", groupStatus=" + groupStatus + ", groupFeature=" + groupFeature + "]";
	}

}
