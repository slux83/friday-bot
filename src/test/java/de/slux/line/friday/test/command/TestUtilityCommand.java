/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.EventInfoCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.InfoCommand;
import de.slux.line.friday.command.AbstractCommand.CommandType;
import de.slux.line.friday.command.admin.AdminBroadcastCommand;
import de.slux.line.friday.command.admin.AdminPushNotificationCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarHistoryCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.command.war.WarReportDeathCommand;
import de.slux.line.friday.command.war.WarSummaryDeathCommand;
import de.slux.line.friday.command.war.WarSummonerNodeCommand;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarSummonerDao;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupFeature;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.logic.war.WarDeathLogic;
import de.slux.line.friday.scheduler.McocSchedulerImporter;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;

/**
 * @author slux
 */
public class TestUtilityCommand {
	/**
	 * Reduce logging level
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
	}

	@Test
	public void testAdminStatusConstruction() throws Exception {

		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		friday.getCommandIncomingMsgCounter().set(10);
		friday.getTotalIncomingMsgCounter().set(100);
		Thread.sleep(1000);

		// 16 secs day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 16));

		// 1 day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 60 * 60 * 24));

		// 10 day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 60 * 60 * 24 * 10));

		// 10 day and 1h
		System.out.println(AdminStatusCommand.calculateUptime((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60));

		// 10 day and 1h and 12 min
		System.out.println(
		        AdminStatusCommand.calculateUptime(((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60) + 1000 * 60 * 12));

		// 10 day and 1h and 12 min and 40 secs
		System.out.println(AdminStatusCommand
		        .calculateUptime((((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60) + 1000 * 60 * 12) + 1000 * 40));

	}

	@Test
	public void testUserSchedulerCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		// User help
		MessageEvent<TextMessageContent> eventUserHelp = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

		// Today's events
		MessageEvent<TextMessageContent> eventTodayEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX);

		// Tomorrow's events
		MessageEvent<TextMessageContent> eventTomorrowEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " tomoRRow");

		// Week events
		MessageEvent<TextMessageContent> eventWeekEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " weeK");
		// Wrong events
		MessageEvent<TextMessageContent> eventWrongEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " Monthly");

		TextMessage response = friday.handleTextMessageEvent(eventUserHelp);
		assertTrue(response.getText().contains(EventInfoCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(HelpCommand.CMD_PREFIX));
		assertFalse(response.getText().contains(WarAddSummonersCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(eventTodayEvents);
		assertTrue(response.getText().contains("MCOC Today's events"));
		assertTrue(callback.takeAllMessages().isEmpty());
		System.out.println(response.getText());

		response = friday.handleTextMessageEvent(eventTomorrowEvents);
		assertTrue(response.getText().contains("MCOC Tomorrow's events"));
		assertTrue(callback.takeAllMessages().isEmpty());
		System.out.println(response.getText());

		response = friday.handleTextMessageEvent(eventWrongEvents);
		assertFalse(response.getText().contains("MCOC Tomorrow's events"));
		assertTrue(response.getText().contains("Sorry"));
		assertTrue(callback.takeAllMessages().isEmpty());
		System.err.println(response.getText());

		response = friday.handleTextMessageEvent(eventWeekEvents);
		System.out.println(response.getText());
		assertTrue(response.getText().contains("MCOC Week events"));
		assertTrue(callback.takeAllMessages().isEmpty());
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		String firstDay = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());
		c.add(Calendar.DAY_OF_MONTH, 7);
		String lastDay = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());
		c.add(Calendar.DAY_OF_MONTH, 1);
		String overBoundaryDay = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());
		assertTrue(response.getText().contains(firstDay));
		assertTrue(response.getText().contains(lastDay));
		assertFalse(response.getText().contains(overBoundaryDay));
		assertTrue(response.getText().contains("AQ Status"));
		assertTrue(response.getText().contains("AW Status"));
		assertTrue(response.getText().contains("3-Days"));
		assertTrue(response.getText().contains("1-Day"));
	}

	@Test
	public void testInfoCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEventGroupSource(
		        UUID.randomUUID().toString(), UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + InfoCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(event);

		assertTrue(response.getText().contains("F.R.I.D.A.Y. MCOC Line Bot"));
	}

	@Test
	public void testJoinEvent() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		JoinEvent event = MessageEventUtil.createJoinEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString());

		friday.handleDefaultMessageEvent(event);

		String answer = callback.takeAllMessages();
		assertTrue(answer.contains("Hello summoners!"));
		assertTrue(answer.contains("PAYPAL"));
		assertTrue(answer.contains(AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX));
	}

	@Test
	public void testFollowEvent() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String userId = UUID.randomUUID().toString();

		// Register command
		FollowEvent followEvent = MessageEventUtil.createFollowEvent(userId);

		friday.handleDefaultMessageEvent(followEvent);
		String pushedMessages = callback.takeAllMessages();

		System.out.print(pushedMessages);
		assertTrue(pushedMessages.contains("Hello Summoner!"));

	}

	@Test
	public void testLeaveEvent() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

		// Leave event
		LeaveEvent leaveEventRegistered = MessageEventUtil.createLeaveEvent(groupId, userId);
		LeaveEvent leaveEventUnregistered = MessageEventUtil.createLeaveEvent(UUID.randomUUID().toString(), userId);

		// History command to test the real exit of the bot from the group
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX);

		// Report death command
		MessageEvent<TextMessageContent> deathCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 55 5* dupe Dormammu");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
		                + " slux83, John Doe, Nemesis The Best, Tony 88");

		// Summoner node
		MessageEvent<TextMessageContent> summonerNodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3A 55 5* dupe IMIW");

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		// We want to test that the leave event clears the current war stuff
		response = friday.handleTextMessageEvent(deathCmd);
		assertTrue(response.getText().contains("160"));
		assertTrue(response.getText().contains("2"));

		response = friday.handleTextMessageEvent(summonersAddCmd);
		assertTrue(response.getText().contains("Added 4 new summoner"));
		assertFalse(response.getText().contains("Tony 88"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerNodeCmd);
		assertTrue(response.getText().contains("A. 5* dupe IMIW (55)"));
		assertTrue(callback.takeAllMessages().isEmpty());

		friday.handleDefaultMessageEvent(leaveEventRegistered);
		assertTrue(callback.takeAllMessages().isEmpty());

		friday.handleDefaultMessageEvent(leaveEventUnregistered);
		assertTrue(callback.takeAllMessages().isEmpty());

		// After the bot has been kicked from the group, the stuff we inserted
		// should not exist
		WarDeathLogic deathLogic = new WarDeathLogic();
		int keyOfUnactiveGroup = WarDeathLogic.getKeyOfGroup(groupId, GroupStatus.GroupStatusInactive);
		WarGroup groupModel = deathLogic.getReportModel(keyOfUnactiveGroup);
		String deathReport = groupModel.getReport();
		assertFalse(deathReport.contains("160"));
		assertFalse(deathReport.contains("2"));
		assertTrue(deathReport.contains("Total deaths: 0"));

		Connection conn = DbConnectionPool.getConnection();
		WarSummonerDao dao = new WarSummonerDao(conn);
		Map<Integer, WarSummoner> placement = dao.getAll(keyOfUnactiveGroup);
		assertTrue(placement.isEmpty());

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("This group is unregistered"));
		assertTrue(callback.takeAllMessages().isEmpty());
	}

	@Test
	public void testAdminAndUserEvent() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();
		friday.getCommandIncomingMsgCounter().set(1000);
		friday.getTotalIncomingMsgCounter().set(10000);

		String userId = FridayBotApplication.SLUX_ID;
		String groupId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummaryDeathCommand.CMD_PREFIX);

		MessageEvent<TextMessageContent> deathSummaryNoAdminCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, UUID.randomUUID().toString(),
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummaryDeathCommand.CMD_PREFIX);

		MessageEvent<TextMessageContent> userHelpCmd = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

		// Admin help command
		MessageEvent<TextMessageContent> adminHelpCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

		// Today's events
		MessageEvent<TextMessageContent> eventTodayEvents = MessageEventUtil.createMessageEventUserSource(
		        UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX);

		// Admin status command
		MessageEvent<TextMessageContent> adminStatusCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminStatusCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> adminStatusOperCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminStatusCommand.CMD_PREFIX + " operational");
		MessageEvent<TextMessageContent> adminStatusMaintCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminStatusCommand.CMD_PREFIX + " maintenance");
		MessageEvent<TextMessageContent> adminStatusInvalidCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminStatusCommand.CMD_PREFIX + " invalid");

		// Leave event
		LeaveEvent leaveEvent = MessageEventUtil.createLeaveEvent(groupId, userId);

		// Admin broadcast command
		MessageEvent<TextMessageContent> adminBroadcastCmd = MessageEventUtil.createMessageEventUserSource(
		        FridayBotApplication.SLUX_ID,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminBroadcastCommand.CMD_PREFIX + " hello everyone!");

		// Admin push notification command
		MessageEvent<TextMessageContent> adminPushCmd = MessageEventUtil.createMessageEventUserSource(
		        FridayBotApplication.SLUX_ID,
		        AbstractCommand.ALL_CMD_PREFIX + " " + AdminPushNotificationCommand.CMD_PREFIX + " hello everyone!");
		
		MessageEvent<TextMessageContent> adminBroadcastNoArgCmd = MessageEventUtil.createMessageEventUserSource(
		        FridayBotApplication.SLUX_ID, AbstractCommand.ALL_CMD_PREFIX + " " + AdminBroadcastCommand.CMD_PREFIX);

		// Admin and User invalid (but close) commands
		MessageEvent<TextMessageContent> adminCloseCmd = MessageEventUtil
		        .createMessageEventUserSource(FridayBotApplication.SLUX_ID, AbstractCommand.ALL_CMD_PREFIX + " broad");
		MessageEvent<TextMessageContent> userCloseCmd = MessageEventUtil
		        .createMessageEventUserSource(UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " hel");
		MessageEvent<TextMessageContent> userNotCloseCmd = MessageEventUtil
		        .createMessageEventUserSource(UUID.randomUUID().toString(), AbstractCommand.ALL_CMD_PREFIX + " broad");
		MessageEvent<TextMessageContent> userInvalidCmd = MessageEventUtil
		        .createMessageEventUserSource(UUID.randomUUID().toString(), "Hello!");

		/* Begin */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("group1"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminHelpCmd);
		assertNotNull(response);
		System.out.println(response.getText());
		assertTrue(response.getText().contains(HelpCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(AdminBroadcastCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(AdminStatusCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(EventInfoCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminStatusCmd);
		assertNotNull(response);
		assertFalse(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("OPERATIONAL"));
		System.out.println(response);
		String statusResponse = response.getText().substring(response.getText().indexOf("Active/Total"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminStatusMaintCmd);
		assertNotNull(response);
		assertFalse(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("MAINTENANCE"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// Test that admin can send commands even under maintenance
		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deathSummaryNoAdminCmd);
		assertTrue(response.getText().contains("maintenance"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userHelpCmd);
		assertTrue(response.getText().contains("maintenance"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminStatusOperCmd);
		assertNotNull(response);
		assertFalse(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("OPERATIONAL"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userCloseCmd);
		System.out.println(response.getText());
		assertTrue(response.getText().contains("Sorry, perhaps"));
		assertTrue(response.getText().contains(AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userNotCloseCmd);
		System.out.println(response.getText());
		assertFalse(response.getText().contains("Sorry, perhaps"));
		assertTrue(response.getText().contains("I didn't understand"));
		assertTrue(response.getText().contains(AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userInvalidCmd);
		assertNull(response);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminCloseCmd);
		System.out.println(response.getText());
		assertTrue(response.getText().contains("Sorry, perhaps"));
		assertTrue(
		        response.getText().contains(AbstractCommand.ALL_CMD_PREFIX + " " + AdminBroadcastCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userHelpCmd);
		System.out.println(response.getText());
		assertFalse(response.getText().contains("maintenance"));
		assertTrue(response.getText().contains(HelpCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(EventInfoCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminStatusInvalidCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("OPERATIONAL"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(eventTodayEvents);
		assertTrue(response.getText().contains("MCOC Today's events"));
		assertTrue(callback.takeAllMessages().isEmpty());
		System.out.println(response.getText());

		// Get all groups
		Map<String, WarGroup> groups = Collections.emptyMap();
		groups = new WarDeathLogic().getAllGroups();
		groups.entrySet().removeIf(g -> g.getValue().getGroupStatus().equals(GroupStatus.GroupStatusInactive));
		int totalActiveGroups = groups.size();
		response = friday.handleTextMessageEvent(adminBroadcastCmd);
		assertNotNull(response);
		String bcastResponse = response.getText();
		System.out.println(bcastResponse);
		assertTrue(bcastResponse.contains("pushed(" + totalActiveGroups + ")"));
		assertTrue(bcastResponse.contains("sent(" + totalActiveGroups + ")"));
		assertTrue(bcastResponse.contains("active_groups(" + totalActiveGroups + ")"));
		assertTrue(bcastResponse.contains("Message broadcasted"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));

		response = friday.handleTextMessageEvent(adminBroadcastNoArgCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("Please provide a message to broadcast"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// One group becomes inactive
		friday.handleDefaultMessageEvent(leaveEvent);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminBroadcastCmd);
		assertNotNull(response);
		System.out.println(response.getText());
		assertTrue(response.getText().contains("Message broadcasted"));
		assertTrue(bcastResponse.contains("pushed(" + totalActiveGroups + ")"));
		assertTrue(bcastResponse.contains("sent(" + totalActiveGroups + ")"));
		assertTrue(bcastResponse.contains("active_groups(" + totalActiveGroups + ")"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));
		// One less active group
		assertNotEquals(bcastResponse, response.getText());
		
		groups.entrySet().removeIf(g -> g.getValue().getGroupFeature().equals(GroupFeature.GroupFeatureWar));
		totalActiveGroups = groups.size();
		response = friday.handleTextMessageEvent(adminPushCmd);
		assertNotNull(response);
		System.out.println(response.getText());
		assertTrue(response.getText().contains("Notification pushed"));
		assertTrue(response.getText().contains("pushed(" + totalActiveGroups + ")"));
		assertTrue(response.getText().contains("sent(" + totalActiveGroups + ")"));
		assertTrue(response.getText().contains("active_groups(" + totalActiveGroups + ")"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));

		response = friday.handleTextMessageEvent(adminStatusCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("Version"));
		System.out.println(response);
		assertNotEquals(statusResponse, response.getText().substring(response.getText().indexOf("Active/Total")));
		assertTrue(callback.takeAllMessages().isEmpty());

		for (AbstractCommand c : friday.getCommands()) {
			// Help command with option (users)
			MessageEvent<TextMessageContent> helpDetailedCmd = MessageEventUtil.createMessageEventUserSource(
			        UUID.randomUUID().toString(),
			        AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX + " " + c.getCommandPrefix());

			if (c.getType().equals(CommandType.CommandTypeShared) || c.getType().equals(CommandType.CommandTypeUser)) {

				response = friday.handleTextMessageEvent(helpDetailedCmd);
				assertNotNull(response);
				String help = response.getText();
				System.out.println(help);
				System.out.println();
				assertFalse(help.isEmpty());
				assertFalse(help.contains(AdminStatusCommand.CMD_PREFIX));
				assertTrue(help.contains(c.getCommandPrefix()));
				assertEquals(AbstractCommand.ALL_CMD_PREFIX + " " + c.getHelp(true), help);
				assertTrue(callback.takeAllMessages().isEmpty());
			}
		}

		for (AbstractCommand c : friday.getCommands()) {
			// Help command with option (admin)
			MessageEvent<TextMessageContent> helpDetailedCmd = MessageEventUtil.createMessageEventUserSource(
			        FridayBotApplication.SLUX_ID,
			        AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX + " " + c.getCommandPrefix());

			if (c.getType().equals(CommandType.CommandTypeShared) || c.getType().equals(CommandType.CommandTypeAdmin)) {

				response = friday.handleTextMessageEvent(helpDetailedCmd);
				assertNotNull(response);
				String help = response.getText();
				System.out.println(help);
				System.out.println();
				assertFalse(help.isEmpty());
				assertTrue(help.contains(c.getCommandPrefix()));
				assertEquals(AbstractCommand.ALL_CMD_PREFIX + " " + c.getHelp(true), help);
				assertTrue(callback.takeAllMessages().isEmpty());
			}
		}
	}
}
