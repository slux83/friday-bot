package de.slux.line.friday.logic;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarGroupDao;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupFeature;

/**
 * @author slux
 */
public class ScheduleEventsLogic {
	private static Logger LOG = LoggerFactory.getLogger(ScheduleEventsLogic.class);

	/**
	 * Ctor
	 */
	public ScheduleEventsLogic() {
	}

	/**
	 * Register the group to receive events
	 * 
	 * @param existingGroup
	 *            can be null
	 * @param groupId
	 * @return false if the group was already registered, true if the
	 *         registration succeeded
	 * @throws Exception
	 */
	public boolean register(WarGroup existingGroup, String groupId) throws Exception {
		if (existingGroup == null) {
			// It's a new one
			LOG.info("Registering the group " + groupId + " to receive schedule events");
			register(groupId, "EVENTS ONLY");
		} else if (existingGroup.getGroupFeature().equals(GroupFeature.GroupFeatureWar)) {
			// We need to update it adding both
			LOG.info("Registering (update) the group " + groupId + " to receive schedule events");
			updateGroupFeature(groupId, GroupFeature.GroupFeatureWarEvent);
		} else {
			// Nothing to do.. the group is already registered
			return false;
		}

		return true;
	}

	/**
	 * Unregister the group to events
	 * 
	 * @param existingGroup
	 * @param groupId
	 * @return false if the group was not registered, true otherwise
	 */
	public boolean unregister(WarGroup existingGroup, String groupId) throws Exception {
		if (existingGroup == null || existingGroup.getGroupFeature().equals(GroupFeature.GroupFeatureWar)) {
			// The group was never registered
			LOG.info("Group " + groupId + " was never registered to receive schedule events");
			return false;
		}

		// Go back to war only
		updateGroupFeature(groupId, GroupFeature.GroupFeatureWar);

		return true;
	}

	/**
	 * Register the chat group
	 * 
	 * @param groupId
	 * @param groupName
	 * @throws Exception
	 */
	private void register(String groupId, String groupName) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarGroupDao dao = new WarGroupDao(conn);

		dao.storeData(groupId, groupName, GroupFeature.GroupFeatureEvent);

	}

	/**
	 * Update the group feature
	 * 
	 * @param groupId
	 * @param feature
	 *            the new feature
	 * @throws Exception
	 */
	public void updateGroupFeature(String groupId, GroupFeature feature) throws Exception {
		Connection conn = DbConnectionPool.getConnection();

		LOG.debug("Connection to the DB valid");

		WarGroupDao dao = new WarGroupDao(conn);

		dao.updateGroupFeatures(groupId, feature);

	}

}
