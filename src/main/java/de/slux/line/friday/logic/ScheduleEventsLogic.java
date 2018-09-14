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
	 * @throws Exception
	 */
	public void register(WarGroup existingGroup, String groupId) throws Exception {
		if (existingGroup == null) {
			// It's a new one
			LOG.info("Registering the group " + groupId + " to receive schedule events");
			register(groupId, "EVENTS");
		} else if (existingGroup.getGroupFeature().equals(GroupFeature.GroupFeatureWar)) {
			// We need to update it adding both
			LOG.info("Registering (update) the group " + groupId + " to receive schedule events");
			updateGroupFeature(groupId, GroupFeature.GroupFeatureWarEvent);
		} else {
			// Nothing to do.. the group is already registered
		}
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