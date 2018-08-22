package de.slux.line.jarvis.war;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.dao.DbConnectionPool;
import de.slux.line.jarvis.dao.WarDaoDuplicatedAllianceTag;
import de.slux.line.jarvis.dao.WarDaoUnregisteredException;
import de.slux.line.jarvis.dao.WarDeathDao;
import de.slux.line.jarvis.dao.WarGroupDao;
import de.slux.line.jarvis.dao.WarHistoryDao;

/**
 * @author slux
 */
public class WarReportModel {
	public static final int WAR_POINTS_LOST_PER_DEATH = 80;
	public static final int WAR_POINTS_LOST_CAP = WAR_POINTS_LOST_PER_DEATH * 3;
	public static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd");

	/**
	 * Ctor
	 */
	public WarReportModel() {
	}

	/**
	 * Reset the model for groupId
	 * 
	 * @param groupId
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	public void resetFor(String groupId) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarDeathDao dao = new WarDeathDao(conn);

		dao.clearData(groupKey);
	}

	/**
	 * Add a new group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws Exception
	 */
	public void addNewGroup(String groupId, String groupName) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarGroupDao dao = new WarGroupDao(conn);

		dao.storeData(groupId, groupName);
	}

	/**
	 * Add death report
	 * 
	 * @param groupId
	 * @param deaths
	 * @param node
	 * @param champName
	 * @param userName
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	public void addDeath(String groupId, int deaths, int node,
			String champName, String userName) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarDeathDao dao = new WarDeathDao(conn);

		dao.storeData(groupKey, deaths, node, champName, userName);
	}

	/**
	 * checks if the group is registered.
	 * 
	 * @param groupId
	 * @return the key of the group
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	private int checkGroupRegistration(String groupId) throws Exception {
		int key = -1;
		if ((key = this.getKeyOfGroup(groupId)) == -1)
			throw new WarDaoUnregisteredException("Group not registered");

		return key;

	}

	/**
	 * Get a death report for groupId
	 * 
	 * @param groupId
	 * @return the group death report
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	public String getReport(String groupId) throws Exception {
		int groupKey = checkGroupRegistration(groupId);
		WarGroup model = getReportModel(groupKey);

		return model.getReport();
	}

	/**
	 * Get the report model
	 * 
	 * @param groupKey
	 * @return the report model
	 * @throws Exception
	 */
	public WarGroup getReportModel(int groupKey) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarDeathDao dao = new WarDeathDao(conn);

		WarGroup reportModel = dao.retrieveData(groupKey);

		return reportModel;

	}

	/**
	 * Undo the last insert for groupId
	 * 
	 * @param groupId
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	public void undoLast(String groupId) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarDeathDao dao = new WarDeathDao(conn);

		dao.undoLast(groupKey);
	}

	/**
	 * Return the summary report
	 * 
	 * @param groupId
	 * @return the summary text
	 * @throws WarDaoUnregisteredException
	 *             if the group is not registered
	 */
	public List<String> getSummary(String groupId) throws Exception {
		int groupKey = checkGroupRegistration(groupId);
		WarGroup model = getReportModel(groupKey);

		return model.getSummaryText();
	}

	/**
	 * Retrieve the group DB key
	 * 
	 * @param groupId
	 * @return the key or -1 if none
	 * @throws Exception
	 */
	public int getKeyOfGroup(String groupId) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarGroupDao dao = new WarGroupDao(conn);

		return dao.getKeyById(groupId);
	}

	/**
	 * Register the chat group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws Exception
	 */
	public void register(String groupId, String groupName) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarGroupDao dao = new WarGroupDao(conn);

		dao.storeData(groupId, groupName);

	}

	/**
	 * Save the current war into history
	 * 
	 * @param groupId
	 * @param allianceTag
	 * @throws WarDaoDuplicatedAllianceTag
	 *             and WarDaoUnregisteredException
	 */
	public void saveWar(String groupId, String allianceTag) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		// Check if there's already a saved alliance tag for today
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Map<String, WarGroup> history = getHistorySummary(groupId, c.getTime());

		if (history.containsKey(allianceTag))
			throw new WarDaoDuplicatedAllianceTag("The alliance " + allianceTag
					+ " has been already saved today");

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);

		dao.storeData(groupKey, allianceTag);

	}

	/**
	 * retrieve all the history for a group
	 * 
	 * @param groupId
	 * @return the history
	 * @throws Exception
	 */
	public Map<Timestamp, String> getHistory(String groupId) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);

		return dao.getAllData(groupKey);

	}

	public List<String> getHistoryText(String groupId) throws Exception {
		Map<Timestamp, String> history = getHistory(groupId);
		List<String> outcome = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder("=== WAR HISTORY ===\n");

		for (Map.Entry<Timestamp, String> entry : history.entrySet()) {
			if (sb.length() > JarvisBotApplication.MAX_LINE_MESSAGE_SIZE) {
				outcome.add(sb.toString());
				sb.setLength(0);
			}
			
			sb.append("\n");
			sb.append(SDF.format(new Date(entry.getKey().getTime())));
			sb.append(" : ");
			sb.append(entry.getValue());
		}

		if (history.isEmpty())
			sb.append("\nNo records found.");

		outcome.add(sb.toString());
		
		return outcome;

	}

	/**
	 * Retrieve the summaries of a given day
	 * 
	 * @param groupId
	 * @param day
	 * @return the war groups for the day (key is the alliance tag)
	 * @throws Exception
	 */
	public Map<String, WarGroup> getHistorySummary(String groupId, Date day)
			throws Exception {

		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(day.getTime());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		return dao.getAllData(groupKey, new Timestamp(c.getTimeInMillis()));
	}
	
	/**
	 * Delete a given day/ally from the history
	 * 
	 * @param groupId
	 * @param allianceTag
	 * @param date
	 * @return outcome
	 * @throws Exception
	 */
	public boolean deleteHistoryEntries(String groupId, String allianceTag, Date date) throws Exception {
		int groupKey = checkGroupRegistration(groupId);

		Connection conn = DbConnectionPool.getConnection();

		System.out.println("Connection to the DB valid");

		WarHistoryDao dao = new WarHistoryDao(conn);

		return dao.deleteData(groupKey, allianceTag, date);	
	}
}
