/**
 *
 */
package de.slux.line.friday.dao.war;

import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupFeature;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * @author slux
 */
public class WarGroupDao {
    private static Logger LOG = LoggerFactory.getLogger(WarGroupDao.class);

    private Connection conn;

    private static final String ADD_DATA_STATEMENT = "INSERT INTO war_group (group_id, group_name, group_features) VALUES(?, ?, ?)";

    private static final String RETRIEVE_DATA_STATEMENT = "SELECT id, group_name FROM war_group WHERE group_id = ? AND group_status = ?";

    private static final String UPDATE_NAME_DATA_STATEMENT = "UPDATE war_group SET group_name = ?, group_status = ? WHERE group_id = ?";

    private static final String UPDATE_STATUS_DATA_STATEMENT = "UPDATE war_group SET group_status = ? WHERE group_id = ?";

    private static final String UPDATE_LAST_ACTIVITY_DATA_STATEMENT = "UPDATE war_group SET last_activity = ? WHERE group_id = ?";

    private static final String UPDATE_FEATURES_DATA_STATEMENT = "UPDATE war_group SET group_status= ?,  group_features = ? WHERE group_id = ?";

    private static final String RETRIEVE_ALL_DATA_STATEMENT = "SELECT group_id, group_name, group_status, group_features, last_activity FROM war_group";

    public WarGroupDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Store a new group, updating the name if already exists
     *
     * @param groupId
     * @param groupName
     * @throws SQLException
     */
    public void storeData(String groupId, String groupName, GroupFeature groupFeatures) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(ADD_DATA_STATEMENT);
            stmt.setString(1, groupId);
            stmt.setString(2, Base64.getEncoder().encodeToString(groupName.getBytes()));
            stmt.setInt(3, groupFeatures.getValue());
            stmt.execute();

        } catch (SQLIntegrityConstraintViolationException ex) {
            // The group already exists
            LOG.info("WarGroupDao.storeData() name needs to be updated: " + ex);
            updateGroupName(groupId, groupName);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        LOG.info("Data stored successfully: " + groupId + " (" + groupName + ")");
    }

    /**
     * Update the name of the group
     *
     * @param groupId
     * @param groupName
     * @throws SQLException
     */
    private void updateGroupName(String groupId, String groupName) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(UPDATE_NAME_DATA_STATEMENT);
            stmt.setString(1, Base64.getEncoder().encodeToString(groupName.getBytes()));
            stmt.setInt(2, GroupStatus.GroupStatusActive.getValue());
            stmt.setString(3, groupId);
            stmt.executeUpdate();

        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        LOG.info("Data updated successfully: " + groupId + " (" + groupName + ")");
    }

    /**
     * Update the name of the group
     *
     * @param groupId
     * @param groupName
     * @throws SQLException
     */
    public void updateGroupStatus(String groupId, GroupStatus groupStatus) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(UPDATE_STATUS_DATA_STATEMENT);
            stmt.setInt(1, groupStatus.getValue());
            stmt.setString(2, groupId);
            stmt.executeUpdate();

        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        LOG.info("Data updated successfully: " + groupId + " (newStatus=" + groupStatus + ")");
    }

    /**
     * Update the last activity of the groups with now time
     *
     * @param groupIds
     * @return affected uptades
     * @throws SQLException
     */
    public int updateGroupsActivity(Collection<String> groupIds) throws SQLException {
        return updateGroupsActivity(groupIds, new Date());
    }

    /**
     * Update the last activity of the group with a given time
     *
     * @param groupIds
     * @param lastActivity
     * @return affected uptades
     * @throws SQLException
     */
    public int updateGroupsActivity(Collection<String> groupIds, Date lastActivity) throws SQLException {
        PreparedStatement stmt = null;
        int totalUpdates = 0;
        try {
            for (String groupId : groupIds) {
                stmt = conn.prepareStatement(UPDATE_LAST_ACTIVITY_DATA_STATEMENT);
                stmt.setTimestamp(1, new Timestamp(lastActivity.getTime()));
                stmt.setString(2, groupId);
                stmt.executeUpdate();
                totalUpdates++;
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Unexpected error " + e, e);
                }
            }

        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        LOG.info("Updated activity successfully for " + totalUpdates + " group(s)");

        return totalUpdates;
    }

    /**
     * get the key of the table by groupId
     *
     * @param groupId
     * @return the key or -1 if none
     * @throws SQLException
     */
    public int getKeyById(String groupId, GroupStatus groupStatus) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int key = -1;
        try {

            stmt = conn.prepareStatement(RETRIEVE_DATA_STATEMENT);
            stmt.setString(1, groupId);
            stmt.setInt(2, groupStatus.getValue());
            rs = stmt.executeQuery();

            while (rs.next()) {
                key = rs.getInt("id");
                String groupName = rs.getString("group_name");
                groupName = new String(Base64.getDecoder().decode(groupName));
            }
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        return key;
    }

    /**
     * get all the uuid of the active groups
     *
     * @return the map of groups (with key=guid and value=name)
     * @throws SQLException
     */
    public Map<String, WarGroup> getAll() throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, WarGroup> groups = new HashMap<>();
        try {

            stmt = conn.prepareStatement(RETRIEVE_ALL_DATA_STATEMENT);
            rs = stmt.executeQuery();

            while (rs.next()) {
                WarGroup wg = new WarGroup();

                String guid = rs.getString("group_id");
                String groupName = rs.getString("group_name");
                groupName = new String(Base64.getDecoder().decode(groupName));
                int status = rs.getInt("group_status");
                int features = rs.getInt("group_features");
                Timestamp lastActivity = rs.getTimestamp("last_activity");
                GroupStatus gs = WarGroup.statusOf(status);
                GroupFeature gf = WarGroup.featureOf(features);

                wg.setGroupId(guid);
                wg.setGroupName(groupName);
                wg.setGroupStatus(gs);
                wg.setGroupFeature(gf);
                wg.setLastActivity(new Date(lastActivity.getTime()));

                groups.put(guid, wg);
            }
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        return groups;
    }

    public void updateGroupFeatures(String groupId, GroupFeature feature) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(UPDATE_FEATURES_DATA_STATEMENT);

            // We are forcing the active statuts of this group
            stmt.setInt(1, GroupStatus.GroupStatusActive.getValue());
            stmt.setInt(2, feature.getValue());
            stmt.setString(3, groupId);
            stmt.executeUpdate();

        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOG.error("Unexpected error " + e, e);
            }
        }

        LOG.info("Data updated successfully: " + groupId + " (new feature=" + feature + ")");

    }
}
