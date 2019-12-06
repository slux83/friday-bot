package de.slux.line.friday.logic.war;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.exception.WarDaoDuplicatedAllianceTagException;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.dao.war.WarDeathDao;
import de.slux.line.friday.dao.war.WarGroupDao;
import de.slux.line.friday.dao.war.WarHistoryDao;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupFeature;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.data.war.WarSummoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author slux
 */
public class WarDeathLogic {
    private static Logger LOG = LoggerFactory.getLogger(WarDeathLogic.class);

    public static final int WAR_POINTS_LOST_PER_DEATH = 80;
    public static final int WAR_POINTS_LOST_CAP = WAR_POINTS_LOST_PER_DEATH * 3;
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Ctor
     */
    public WarDeathLogic() {
    }

    /**
     * Reset the model for groupId
     *
     * @param groupId
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public void resetFor(String groupId) throws Exception {
        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarDeathDao dao = new WarDeathDao(conn);

        dao.clearData(groupKey);
    }

    /**
     * Get all the groups
     *
     * @return the map of groups
     * @throws Exception
     */
    public Map<String, WarGroup> getAllGroups() throws Exception {
        Connection conn = DbConnectionPool.getConnection();

        WarGroupDao dao = new WarGroupDao(conn);

        return dao.getAll();
    }

    /**
     * Add death report
     *
     * @param groupId
     * @param deaths
     * @param node
     * @param champName
     * @param userName
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public void addDeath(String groupId, int deaths, int node, String champName, String userName) throws Exception {
        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarDeathDao dao = new WarDeathDao(conn);

        dao.storeData(groupKey, deaths, node, champName, userName);
    }

    /**
     * Update the group activities
     *
     * @param groupIds
     * @return affected updates
     * @throws Exception
     */
    public int updateGroupsActivity(Collection<String> groupIds) throws Exception {
        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarGroupDao dao = new WarGroupDao(conn);

        int updates = dao.updateGroupsActivity(groupIds);

        if (updates != groupIds.size()) {
            LOG.warn("updateGroupsActivity(groupIds=" + groupIds.size() + ") but updated only " + updates);
        }

        return updates;
    }

    /**
     * checks if the group is registered.
     *
     * @param groupId
     * @return the key of the group
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public static int checkGroupRegistration(String groupId) throws Exception {
        int key = -1;
        if ((key = getKeyOfGroup(groupId, GroupStatus.GroupStatusActive)) == -1)
            throw new WarDaoUnregisteredException("Group not registered");

        return key;

    }

    /**
     * Get a death report for groupId
     *
     * @param groupId
     * @return the group death report
     * @throws WarDaoUnregisteredException if the group is not registered
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

        LOG.debug("Connection to the DB valid");

        WarDeathDao dao = new WarDeathDao(conn);

        WarGroup reportModel = dao.retrieveData(groupKey);

        return reportModel;

    }

    /**
     * Update the group status
     *
     * @param groupId
     * @param newStatus
     * @throws Exception
     */
    public void updateGroupStatus(String groupId, GroupStatus newStatus) throws Exception {
        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarGroupDao dao = new WarGroupDao(conn);

        dao.updateGroupStatus(groupId, newStatus);
    }

    /**
     * Undo the last insert for groupId
     *
     * @param groupId
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public void undoLast(String groupId) throws Exception {
        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarDeathDao dao = new WarDeathDao(conn);

        dao.undoLast(groupKey);
    }

    /**
     * Delete all the entries for the given node and groupId
     *
     * @param groupId
     * @param node
     * @return the number of deleted entries
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public int deleteNode(String groupId, int node) throws Exception {
        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarDeathDao dao = new WarDeathDao(conn);

        return dao.deleteNode(groupKey, node);
    }

    /**
     * Return the summary report
     *
     * @param groupId
     * @param compactView
     * @return the summary text
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public List<String> getSummary(String groupId, boolean compactView) throws Exception {
        int groupKey = checkGroupRegistration(groupId);
        WarGroup model = getReportModel(groupKey);

        if (compactView)
            return model.getSummaryTextCompact();
        else
            return model.getSummaryText();
    }

    /**
     * Return the summary report model
     *
     * @param groupId
     * @return the summary report model
     * @throws WarDaoUnregisteredException if the group is not registered
     */
    public WarGroup getSummaryModel(String groupId) throws Exception {
        int groupKey = checkGroupRegistration(groupId);
        return getReportModel(groupKey);
    }

    /**
     * Retrieve the group DB key
     *
     * @param groupId
     * @param groupStatus
     * @return the key or -1 if none
     * @throws Exception
     */
    public static int getKeyOfGroup(String groupId, GroupStatus groupStatus) throws Exception {
        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarGroupDao dao = new WarGroupDao(conn);

        return dao.getKeyById(groupId, groupStatus);
    }

    /**
     * Register the chat group for war
     *
     * @param groupId
     * @param groupName
     * @throws Exception
     */
    public void register(String groupId, String groupName) throws Exception {
        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        Map<String, WarGroup> groups = getAllGroups();

        WarGroupDao dao = new WarGroupDao(conn);

        // new registration
        if (!groups.containsKey(groupId)) {
            dao.storeData(groupId, groupName, GroupFeature.GroupFeatureWar);
            return;
        }

        WarGroup wg = groups.get(groupId);
        switch (wg.getGroupFeature()) {
            case GroupFeatureEvent:
                // Name update + group feature update
                dao.storeData(groupId, groupName, GroupFeature.GroupFeatureWarEvent);
                new WarGroupDao(DbConnectionPool.getConnection()).updateGroupFeatures(groupId,
                        GroupFeature.GroupFeatureWarEvent);
                break;
            case GroupFeatureWar:
            case GroupFeatureWarEvent:
                // Just name update
                dao.storeData(groupId, groupName, wg.getGroupFeature());
                break;
        }

    }

    /**
     * Save the current war into history
     *
     * @param groupId
     * @param allianceTag
     * @throws WarDaoDuplicatedAllianceTagException and WarDaoUnregisteredException
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
        Map<String, WarGroup> history = getHistorySummaryForDeaths(groupId, c.getTime());

        if (history.containsKey(allianceTag))
            throw new WarDaoDuplicatedAllianceTagException(
                    "The alliance " + allianceTag + " has been already saved today");

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

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

        LOG.debug("Connection to the DB valid");

        WarHistoryDao dao = new WarHistoryDao(conn);

        return dao.getAllData(groupKey);

    }

    public List<String> getHistoryText(String groupId) throws Exception {
        Map<Timestamp, String> history = getHistory(groupId);
        List<String> outcome = new ArrayList<String>();

        StringBuilder sb = new StringBuilder("*** WAR HISTORY ***\n");

        for (Map.Entry<Timestamp, String> entry : history.entrySet()) {
            if (sb.length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
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
     * Retrieve the summaries of deaths of a given day
     *
     * @param groupId
     * @param day
     * @return the war groups for the day (key is the alliance tag)
     * @throws Exception
     */
    public Map<String, WarGroup> getHistorySummaryForDeaths(String groupId, Date day) throws Exception {

        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarHistoryDao dao = new WarHistoryDao(conn);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day.getTime());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return dao.getAllDataForDeaths(groupKey, new Timestamp(c.getTimeInMillis()));
    }

    /**
     * Retrieve the table of reports of a given day
     *
     * @param groupId
     * @param day
     * @return the war groups for the day (key is the alliance tag)
     * @throws Exception
     */
    public Map<String, Map<Integer, WarSummoner>> getHistorySummaryForReports(String groupId, Date day)
            throws Exception {

        int groupKey = checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarHistoryDao dao = new WarHistoryDao(conn);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day.getTime());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return dao.getAllDataForReports(groupKey, new Timestamp(c.getTimeInMillis()));
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

        LOG.debug("Connection to the DB valid");

        WarHistoryDao dao = new WarHistoryDao(conn);

        return dao.deleteData(groupKey, allianceTag, date);
    }
}
