package de.slux.line.friday.test.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.EventInfoCommand;
import de.slux.line.friday.command.RegisterEventsCommand;
import de.slux.line.friday.command.UnregisterEventsCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarGroupDao;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;
import de.slux.line.friday.scheduler.GroupActivityJob;
import de.slux.line.friday.scheduler.LinePushJob;
import de.slux.line.friday.scheduler.McocSchedulerImporter;
import de.slux.line.friday.scheduler.McocSchedulerJob;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import de.slux.line.friday.test.util.scheduler.ContextDummy;

/**
 * @author slux
 */
// @Ignore
public class TestScheduler {
	private static Set<String> groupsToDelete = new HashSet<>();

	@BeforeClass
	public static void beforeClass() throws Exception {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);

		// Adjust timezone via sys property
		System.setProperty(McocSchedulerJob.TIMEZONE_SYSPROP_ADJUSTMENT_FROM_UTC, "0");

		// Make sure we have more than 50 groups registered
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String userId = UUID.randomUUID().toString();

		for (int i = 0; i < FridayBotApplication.MAX_MESSAGE_BURST + 10; i++) {
			// Register command new group
			String groupId = UUID.randomUUID().toString();
			MessageEvent<TextMessageContent> registerNewCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
			        userId, AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);

			TextMessage response = friday.handleTextMessageEvent(registerNewCmd);
			assertTrue(response.getText().contains("MCoC event notifications"));
			assertTrue(callback.takeAllMessages().isEmpty());
			groupsToDelete.add(groupId);
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		WarDeathLogic warModel = new WarDeathLogic();

		for (String groupId : groupsToDelete) {
			// TODO: we should delete the group completely from the DB
			warModel.updateGroupStatus(groupId, GroupStatus.GroupStatusInactive);
		}
	}

	@Test
	public void testSchedulerInvalidDate() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		// EPOCH time
		friday.setClockReference(Clock.fixed(Instant.EPOCH, ZoneId.systemDefault()));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String userId = UUID.randomUUID().toString();

		// Today's
		MessageEvent<TextMessageContent> todayEvents = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX);
		// Tomorrow's events
		MessageEvent<TextMessageContent> eventTomorrowEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " tomoRRow");
		// Week events
		MessageEvent<TextMessageContent> eventWeekEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " weeK");

		TextMessage response = friday.handleTextMessageEvent(todayEvents);
		assertTrue(response.getText().contains("Nothing found"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(eventTomorrowEvents);
		assertTrue(response.getText().contains("Nothing found"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(eventWeekEvents);
		assertTrue(response.getText().contains("Missing data"));
		assertTrue(callback.takeAllMessages().isEmpty());
	}

	@Test
	public void testSchedulerJobs() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		Thread.sleep(15000);

		Scheduler scheduler = friday.getEventScheduler().getScheduler();
		boolean masterJobFound = false;
		for (String groupName : scheduler.getJobGroupNames()) {

			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();

				// get job's trigger
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
				Date nextFireTime = triggers.get(0).getNextFireTime();

				System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);

				if (jobName.contains("mcoc schedule master")) {
					masterJobFound = true;
				}
			}
		}

		assertTrue(masterJobFound);
	}

	@Test
	public void testMcocImporter() throws Exception {
		McocSchedulerImporter importer = new McocSchedulerImporter();
		System.out.println(importer.getMcocScheduler());
	}

	@Test
	public void testSchedulerJobsInvalidDate() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		// EPOCH time
		friday.setClockReference(Clock.fixed(Instant.EPOCH, ZoneId.systemDefault()));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		Thread.sleep(15000);

		Scheduler scheduler = friday.getEventScheduler().getScheduler();
		boolean masterJobFound = false;
		for (String groupName : scheduler.getJobGroupNames()) {

			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();

				// get job's trigger
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
				Date nextFireTime = triggers.get(0).getNextFireTime();

				System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);

				if (jobName.contains("mcoc schedule master")) {
					masterJobFound = true;
				}
			}
		}

		assertTrue(masterJobFound);
	}

	@Test
	public void testSchedulerJobsRollingDays() throws Exception {

		// Should be enough to touch all the events
		for (int i = 0; i < 10; i++) {
			MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
			FridayBotApplication friday = new FridayBotApplication(null);
			friday.setLineMessagingClient(new LineMessagingClientMock(callback));
			// Time is moving
			Instant instant = Instant.now().plus(i, ChronoUnit.DAYS);
			friday.setClockReference(Clock.fixed(instant, ZoneId.systemDefault()));
			friday.postConstruct();
			System.out.println("Time now is (instant " + instant.toString() + "): " + friday.getClockReference());

			PostConstructHolder.waitForPostConstruct(callback);

			Scheduler scheduler = friday.getEventScheduler().getScheduler();
			boolean masterJobFound = false;
			boolean warStatsJobFound = false;
			for (String groupName : scheduler.getJobGroupNames()) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();

					// get job's trigger
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					Date nextFireTime = triggers.get(0).getNextFireTime();

					System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);

					if (jobName.contains("mcoc schedule master")) {
						masterJobFound = true;
					}

					if (jobName.contains("war stats schedule")) {
						warStatsJobFound = true;
					}
				}
			}

			assertTrue(masterJobFound);
			assertTrue(warStatsJobFound);
			Thread.sleep(11000);
			friday.getEventScheduler().terminate();
		}
	}

	@Test
	public void testRegisterAndUnregisterForEvents() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String group1Id = UUID.randomUUID().toString();
		String group2Id = UUID.randomUUID().toString();
		String group3Id = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Admin status command
		MessageEvent<TextMessageContent> adminStatusCmd = MessageEventUtil.createMessageEventUserSource(
		        FridayBotApplication.SLUX_ID, AbstractCommand.ALL_CMD_PREFIX + " " + AdminStatusCommand.CMD_PREFIX);

		// Register command new group
		MessageEvent<TextMessageContent> registerNewCmd = MessageEventUtil.createMessageEventGroupSource(group1Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);

		// Register command events with an already existing registration for war
		MessageEvent<TextMessageContent> registerWarCmd = MessageEventUtil.createMessageEventGroupSource(group2Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");
		MessageEvent<TextMessageContent> registerExistingWithWarCmd = MessageEventUtil.createMessageEventGroupSource(
		        group2Id, userId, AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);

		// Register first the events and then the group for war tool
		MessageEvent<TextMessageContent> registerBeforeWarCmd = MessageEventUtil.createMessageEventGroupSource(group3Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> registerWarLaterCmd = MessageEventUtil.createMessageEventGroupSource(group3Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");
		MessageEvent<TextMessageContent> summonersPrintCmd = MessageEventUtil.createMessageEventGroupSource(group3Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX);

		// Unregister
		MessageEvent<TextMessageContent> unregisterInvalidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        UUID.randomUUID().toString(), userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + UnregisterEventsCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> unregisterGroup1Cmd = MessageEventUtil.createMessageEventGroupSource(group1Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + UnregisterEventsCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> unregisterGroup2Cmd = MessageEventUtil.createMessageEventGroupSource(group2Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + UnregisterEventsCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> unregisterGroup3Cmd = MessageEventUtil.createMessageEventGroupSource(group3Id,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + UnregisterEventsCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(registerNewCmd);
		assertTrue(response.getText().contains("MCoC event notifications"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerWarCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerExistingWithWarCmd);
		assertTrue(response.getText().contains("MCoC event notifications"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerExistingWithWarCmd);
		assertTrue(response.getText().contains("already registered"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// We try to push a notification message (this should be done by the
		// scheduler but there's no way to trigger it on demand)

		LinePushJob pushJob = new LinePushJob();

		pushJob.execute(new ContextDummy(false));
		String pushedMessages = callback.takeAllMessages();
		assertTrue(!pushedMessages.isEmpty());

		// We trigger a missfire
		pushJob.execute(new ContextDummy(true));
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());

		response = friday.handleTextMessageEvent(adminStatusCmd);
		assertTrue(response.getText().contains("pushed"));
		assertTrue(response.getText().contains("sent"));
		assertTrue(response.getText().contains("active_groups"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerBeforeWarCmd);
		assertTrue(response.getText().contains("MCoC event notifications"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerWarLaterCmd);
		assertTrue(response.getText().contains("successfully registered"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersPrintCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(unregisterInvalidGroupCmd);
		assertTrue(response.getText().contains("was never registered"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(unregisterGroup1Cmd);
		assertTrue(response.getText().contains("MCoC events have been disabled"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(unregisterGroup2Cmd);
		assertTrue(response.getText().contains("MCoC events have been disabled"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(unregisterGroup3Cmd);
		assertTrue(response.getText().contains("MCoC events have been disabled"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(unregisterGroup3Cmd);
		assertTrue(response.getText().contains("was never registered"));
		assertTrue(callback.takeAllMessages().isEmpty());
	}

	@Test
	public void testScheduledGroupsActivityChecker() throws Exception {
		// We make sure we have 3 obsolete groups and 2 warnings
		String[] newGroups = groupsToDelete.toArray(new String[0]);
		assertEquals(FridayBotApplication.MAX_MESSAGE_BURST + 10, newGroups.length);
		Connection conn = DbConnectionPool.getConnection();
		assertNotNull(conn);
		Collection<String> groupIdsObsolete = new HashSet<>();
		groupIdsObsolete.add(newGroups[0]);
		groupIdsObsolete.add(newGroups[1]);
		groupIdsObsolete.add(newGroups[2]);

		Collection<String> groupIdsToWarn = new HashSet<>();
		groupIdsToWarn.add(newGroups[3]);
		groupIdsToWarn.add(newGroups[4]);

		long now = System.currentTimeMillis();
		long leaveInactivityTime = now - GroupActivityJob.LEAVE_INACTIVITY_MS;
		long warningInactivityTime = now - GroupActivityJob.WARNING_INACTIVITY_MS;

		WarGroupDao dao = new WarGroupDao(conn);
		int updates = dao.updateGroupsActivity(groupIdsObsolete, new Date(leaveInactivityTime));
		assertEquals(3, updates);
		conn = DbConnectionPool.getConnection();
		assertNotNull(conn);
		dao = new WarGroupDao(conn);

		updates = dao.updateGroupsActivity(groupIdsToWarn, new Date(warningInactivityTime));
		assertEquals(2, updates);
		System.out.println("Group dates modified");

		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();
		friday.getGroupActivities().add(newGroups[10]);
		friday.getGroupActivities().add(newGroups[11]);

		String callbackMessages = PostConstructHolder.waitForPostConstruct(callback);

		assertTrue(callbackMessages.contains("Warned: 2"));
		assertTrue(callbackMessages.contains("To leave: 3"));
		assertTrue(callbackMessages.contains("Actual left: 3"));
	}
}
