package de.slux.line.friday.data.war;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
		GroupFeatureWar(1), GroupFeatureEvent(2), GroupFeatureWarEvent(3);

		private final int value;

		private GroupFeature(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public static final Integer TOTAL_AW_NODES = 55;

	private List<WarDeath> deathReports;
	private String groupId;
	private String groupName;
	private GroupStatus groupStatus;
	private GroupFeature groupFeature;
	private Date lastActivity;

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
		// Calculate how many missing nodes in the report
		Set<Integer> reportedNodes = new HashSet<>();
		for (WarDeath wd : this.deathReports) {
			reportedNodes.add(wd.getNodeNumber());
		}

		report.append("\nTotal lost points: " + totDeathReport.getTotalLostPoints());
		report.append("\nTotal deaths: " + totDeathReport.getTotalDeaths());
		report.append("\nTrue deaths: " + totDeathReport.getTrueDeaths());
		report.append("\nReported nodes: " + reportedNodes.size() + "/" + TOTAL_AW_NODES);

		if (reportedNodes.size() >= TOTAL_AW_NODES - 10 && reportedNodes.size() < TOTAL_AW_NODES) {
			// We show the missing nodes if the alliance goes hardcore
			List<Integer> missingNodes = new ArrayList<>();
			for (int i = 1; i <= TOTAL_AW_NODES; i++) {
				if (!reportedNodes.contains(i))
					missingNodes.add(i);
			}

			report.append("\nNodes to report: " + missingNodes);
		}
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
		int trueDeaths = 0;
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

		for (Map.Entry<Integer, Integer> deathsPerNode : totalDeathsPerNode.entrySet()) {
			 trueDeaths += Math.min(deathsPerNode.getValue(), 3);
		}

		return new TotalDeathReport(totalLostPoints, totalDeaths, trueDeaths);
	}

	/**
	 * Get the summary text but compact view
	 *
	 * @return the compact human readable version of the getSummary()
	 */
	public List<String> getSummaryTextCompact() {
		List<String> outcome = new ArrayList<String>();

		StringBuilder sb = new StringBuilder("*** WAR DEATH SUMMARY ***\n");

		List<WarDeath> reports = getDeathReports();

		// We sort by node number
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
			if (wd.getNodeDeaths() > 0) {
				sb.append(wd.getNodeNumber());
				sb.append(". ");
				sb.append(wd.getChampName());
				sb.append(" : [");
				sb.append(wd.getNodeDeaths());
				sb.append("] ");
				sb.append(wd.getUserName());
				sb.append("\n");
			}
		}

		if (reports.isEmpty())
			sb.append("Nothing to report\n\n");

		sb.append("\n");
		sb.append(getReport());

		outcome.add(sb.toString());

		return outcome;
	}

	/**
	 * Get the summary as text CSV export
	 *
	 * @return the human readable version of the getSummary()
	 */
	public String getSummaryTextCsv() {
		StringBuilder sb = new StringBuilder("Node,Deaths,Champion,Summoner\n");

		List<WarDeath> reports = getDeathReports();

		// We organize by players
		Map<String, List<WarDeath>> reportsByPlayer = new TreeMap<>();
		for (WarDeath wd : reports) {
			if (!reportsByPlayer.containsKey(wd.getUserName())) {
				List<WarDeath> playerReports = new ArrayList<>();
				playerReports.add(wd);
				reportsByPlayer.put(wd.getUserName(), playerReports);
			} else {
				reportsByPlayer.get(wd.getUserName()).add(wd);
			}
		}

		for (Entry<String, List<WarDeath>> playerReport : reportsByPlayer.entrySet()) {
			// We sort by node number
			Collections.sort(playerReport.getValue(), new Comparator<WarDeath>() {
				@Override
				public int compare(WarDeath o1, WarDeath o2) {
					return o1.getNodeNumber() - o2.getNodeNumber();
				}
			});

			for (WarDeath wd : playerReport.getValue()) {

				sb.append(wd.getNodeNumber());
				sb.append(",");
				sb.append(wd.getNodeDeaths());
				sb.append(",");
				sb.append(wd.getChampName());
				sb.append(",");
				sb.append(playerReport.getKey());
				sb.append("\n");
			}
		}

		return sb.toString();
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

		// We organize by players
		Map<String, List<WarDeath>> reportsByPlayer = new TreeMap<>();
		for (WarDeath wd : reports) {
			if (!reportsByPlayer.containsKey(wd.getUserName())) {
				List<WarDeath> playerReports = new ArrayList<>();
				playerReports.add(wd);
				reportsByPlayer.put(wd.getUserName(), playerReports);
			} else {
				reportsByPlayer.get(wd.getUserName()).add(wd);
			}
		}

		for (Entry<String, List<WarDeath>> playerReport : reportsByPlayer.entrySet()) {
			sb.append(playerReport.getKey());
			sb.append('\n');

			// We sort by node number
			Collections.sort(playerReport.getValue(), new Comparator<WarDeath>() {
				@Override
				public int compare(WarDeath o1, WarDeath o2) {
					return o1.getNodeNumber() - o2.getNodeNumber();
				}
			});

			for (WarDeath wd : playerReport.getValue()) {
				if (sb.length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
					// We need to split it and clear
					outcome.add(sb.toString());
					sb.setLength(0);
				}

				sb.append(wd.getNodeNumber());
				sb.append(". ");
				sb.append(wd.getChampName());
				sb.append(" : [");
				sb.append(wd.getNodeDeaths());
				sb.append("]\n");
			}

			sb.append('\n');
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
		int trueDeaths;
		int totalLostPoints;
		int totalDeaths;

		/**
		 * @param totalLostPoints
		 * @param totalDeaths
		 * @param trueDeaths
		 */
		public TotalDeathReport(int totalLostPoints, int totalDeaths, int trueDeaths) {
			super();
			this.totalLostPoints = totalLostPoints;
			this.totalDeaths = totalDeaths;
			this.trueDeaths = trueDeaths;
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

		/**
		 * @return the trueDeaths
		 */
		public int getTrueDeaths() { return trueDeaths; }

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

		if (GroupFeature.GroupFeatureEvent.getValue() == feature) {
			return GroupFeature.GroupFeatureEvent;
		}

		if (GroupFeature.GroupFeatureWarEvent.getValue() == feature) {
			return GroupFeature.GroupFeatureWarEvent;
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
		        + ", groupStatus=" + groupStatus + ", groupFeature=" + groupFeature + ", lastActivity=" + lastActivity
		        + "]";
	}

	/**
	 * @return the lastActivity
	 */
	public Date getLastActivity() {
		return lastActivity;
	}

	/**
	 * @param lastActivity
	 *            the lastActivity to set
	 */
	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}
}
