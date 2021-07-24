/**
 *
 */
package de.slux.line.friday.test.command;

import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.*;
import de.slux.line.friday.command.AbstractCommand.CommandType;
import de.slux.line.friday.command.admin.AdminBroadcastCommand;
import de.slux.line.friday.command.admin.AdminNotificationCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.*;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarGroupDao;
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
import de.slux.line.friday.test.util.PostConstructHolder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

import static org.junit.Assert.*;

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

        PostConstructHolder.waitForPostConstruct(callback);

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

        PostConstructHolder.waitForPostConstruct(callback);

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
        assertTrue(response.getText().contains("MCOC Today's upcoming events"));
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

        PostConstructHolder.waitForPostConstruct(callback);

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

        PostConstructHolder.waitForPostConstruct(callback);

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

        PostConstructHolder.waitForPostConstruct(callback);

        String userId = UUID.randomUUID().toString();

        // Register command
        FollowEvent followEvent = MessageEventUtil.createFollowEvent(userId);

        friday.handleDefaultMessageEvent(followEvent);
        String pushedMessages = callback.takeAllMessages();

        System.out.print(pushedMessages);
        assertTrue(pushedMessages.contains("Hello Summoner!"));

    }

    @Test
    public void testLeaveAndRejoinScenario() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

        String groupId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // Join event
        JoinEvent joinEvent = MessageEventUtil.createJoinEvent(groupId, userId);

        // Register war command
        MessageEvent<TextMessageContent> registerWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

        // Register events command
        MessageEvent<TextMessageContent> registerEventsCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);

        // Leave event
        LeaveEvent leaveEventRegistered = MessageEventUtil.createLeaveEvent(groupId, userId);

        // Start the test
        friday.handleDefaultMessageEvent(joinEvent);

        TextMessage response = friday.handleTextMessageEvent(registerWarCmd);
        assertTrue(response.getText().contains("successfully registered using the name group1"));

        response = friday.handleTextMessageEvent(registerEventsCmd);
        System.out.println(response.getText());
        assertTrue(response.getText().contains("From now on this group will receive"));

        // Assert the data in the DB of the group
        Connection conn = DbConnectionPool.getConnection();
        WarGroupDao dao = new WarGroupDao(conn);
        Map<String, WarGroup> allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        WarGroup thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusActive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Leave the group
        friday.handleDefaultMessageEvent(leaveEventRegistered);

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusInactive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Join back event
        friday.handleDefaultMessageEvent(joinEvent);

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusInactive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Register again to receive the events
        response = friday.handleTextMessageEvent(registerEventsCmd);
        assertTrue(response.getText().contains("From now on this group will receive"));

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusActive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Leave the group again
        friday.handleDefaultMessageEvent(leaveEventRegistered);

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusInactive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Join back event
        friday.handleDefaultMessageEvent(joinEvent);

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusInactive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Register but only for wars
        response = friday.handleTextMessageEvent(registerWarCmd);
        assertTrue(response.getText().contains("successfully registered using the name group1"));

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusActive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());

        // Register again to receive the events
        response = friday.handleTextMessageEvent(registerEventsCmd);
        assertTrue(response.getText().contains("This group is already registered to receive notifications"));

        // Check data
        conn = DbConnectionPool.getConnection();
        dao = new WarGroupDao(conn);
        allGroups = dao.getAll();
        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());
        thisGroup = allGroups.get(groupId);
        assertNotNull(thisGroup);
        assertEquals(GroupStatus.GroupStatusActive, thisGroup.getGroupStatus());
        assertEquals(GroupFeature.GroupFeatureWarEvent, thisGroup.getGroupFeature());
    }


    @Test
    public void testLeaveEvent() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

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

        PostConstructHolder.waitForPostConstruct(callback);

        String userId = friday.getBotOwnerLineId();
        String groupId = UUID.randomUUID().toString();

        // Register command
        MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

        // Register events command
        MessageEvent<TextMessageContent> registerEventsCmd = MessageEventUtil.createMessageEventGroupSource(
                UUID.randomUUID().toString(), userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + RegisterEventsCommand.CMD_PREFIX);

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
                friday.getBotOwnerLineId(),
                AbstractCommand.ALL_CMD_PREFIX + " " + AdminBroadcastCommand.CMD_PREFIX + " hello everyone!");

        // Admin notification command
        MessageEvent<TextMessageContent> adminNotificationWithMessageCmd = MessageEventUtil.createMessageEventUserSource(
                friday.getBotOwnerLineId(),
                AbstractCommand.ALL_CMD_PREFIX + " " + AdminNotificationCommand.CMD_PREFIX + " This is a nice notification");
        MessageEvent<TextMessageContent> adminNotificationWithNoMessageCmd = MessageEventUtil.createMessageEventUserSource(
                friday.getBotOwnerLineId(),
                AbstractCommand.ALL_CMD_PREFIX + " " + AdminNotificationCommand.CMD_PREFIX);

        MessageEvent<TextMessageContent> adminBroadcastNoArgCmd = MessageEventUtil.createMessageEventUserSource(
                friday.getBotOwnerLineId(), AbstractCommand.ALL_CMD_PREFIX + " " + AdminBroadcastCommand.CMD_PREFIX);

        // Admin and User invalid (but close) commands
        MessageEvent<TextMessageContent> adminCloseCmd = MessageEventUtil
                .createMessageEventUserSource(friday.getBotOwnerLineId(), AbstractCommand.ALL_CMD_PREFIX + " broad");
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
        assertTrue(response.getText().contains("MCOC Today's upcoming events"));
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

        // We register at least one to receive events
        response = friday.handleTextMessageEvent(registerEventsCmd);
        assertTrue(response.getText().contains("MCoC event notifications"));
        assertTrue(callback.takeAllMessages().isEmpty());

        response = friday.handleTextMessageEvent(adminNotificationWithMessageCmd);
        assertNotNull(response);
        System.out.println(response.getText());
        assertTrue(response.getText().contains("Notification saved"));
        assertTrue(response.getText().contains("This is a nice notification"));
        assertTrue(callback.takeAllMessages().isEmpty());

        response = friday.handleTextMessageEvent(adminStatusCmd);
        assertNotNull(response);
        assertTrue(response.getText().contains("Version"));
        assertTrue(response.getText().contains("WarSummonerNodeCommand: 0"));
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
                assertTrue(help.contains(AbstractCommand.ALL_CMD_PREFIX + " " + c.getHelp(true)));
                assertTrue(callback.takeAllMessages().isEmpty());
            }
        }

        for (AbstractCommand c : friday.getCommands()) {
            // Help command with option (admin)
            MessageEvent<TextMessageContent> helpDetailedCmd = MessageEventUtil.createMessageEventUserSource(
                    friday.getBotOwnerLineId(),
                    AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX + " " + c.getCommandPrefix());

            if (c.getType().equals(CommandType.CommandTypeShared) || c.getType().equals(CommandType.CommandTypeAdmin)) {

                response = friday.handleTextMessageEvent(helpDetailedCmd);
                assertNotNull(response);
                String help = response.getText();
                System.out.println(help);
                System.out.println();
                assertFalse(help.isEmpty());
                assertTrue(help.contains(c.getCommandPrefix()));
                assertTrue(help.contains(AbstractCommand.ALL_CMD_PREFIX + " " + c.getHelp(true)));
                assertTrue(callback.takeAllMessages().isEmpty());
            }
        }

        response = friday.handleTextMessageEvent(adminNotificationWithNoMessageCmd);
        assertNotNull(response);
        System.out.println(response.getText());
        assertTrue(response.getText().startsWith("The notification:"));
        assertTrue(response.getText().contains("This is a nice notification"));
        assertTrue(callback.takeAllMessages().isEmpty());
    }

    @Test
    public void testSendAllCommand() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        LineMessagingClientMock clientMock = new LineMessagingClientMock(callback);
        friday.setLineMessagingClient(clientMock);
        friday.postConstruct();
        friday.getCommandIncomingMsgCounter().set(1000);
        friday.getTotalIncomingMsgCounter().set(10000);

        PostConstructHolder.waitForPostConstruct(callback);

        // Register command send all for 180 users
        MessageEvent<TextMessageContent> event180 = MessageEventUtil.createMessageEventGroupSource(
                LineMessagingClientMock.GROUP_180_MEMBERS_ID, LineMessagingClientMock.KNOWN_USER_ID,
                AbstractCommand.ALL_CMD_PREFIX + " " + SendAllCommand.CMD_PREFIX + " Hello World");

        // Register command send all for 20 users
        MessageEvent<TextMessageContent> event20 = MessageEventUtil.createMessageEventGroupSource(
                LineMessagingClientMock.GROUP_20_MEMBERS_ID, LineMessagingClientMock.KNOWN_USER_ID,
                AbstractCommand.ALL_CMD_PREFIX + " " + SendAllCommand.CMD_PREFIX + " Hello World");

        // Register command send all for 1 user
        MessageEvent<TextMessageContent> event1 = MessageEventUtil.createMessageEventGroupSource(
                LineMessagingClientMock.GROUP_1_MEMBER_ID, LineMessagingClientMock.KNOWN_USER_ID,
                AbstractCommand.ALL_CMD_PREFIX + " " + SendAllCommand.CMD_PREFIX + " Hello World");

        // Register command send all with no text
        MessageEvent<TextMessageContent> eventNoText = MessageEventUtil.createMessageEventGroupSource(
                LineMessagingClientMock.GROUP_1_MEMBER_ID, LineMessagingClientMock.KNOWN_USER_ID,
                AbstractCommand.ALL_CMD_PREFIX + " " + SendAllCommand.CMD_PREFIX);

        // Register command send all with no text
        MessageEvent<TextMessageContent> eventSpammerTest = MessageEventUtil.createMessageEventGroupSource(
                LineMessagingClientMock.GROUP_20_MEMBERS_ID, LineMessagingClientMock.KNOWN_USER_ID,
                AbstractCommand.ALL_CMD_PREFIX + " " + SendAllCommand.CMD_PREFIX + " I'm the spammer");

        clientMock.cleanupMulticast();
        TextMessage response = friday.handleTextMessageEvent(event180);
        // System.out.println(response);
        // System.out.println(clientMock.getRecordedMulticast());
        Assert.assertEquals("Message sent privately to 180 user(s)", response.getText());
        Assert.assertEquals(2, clientMock.getRecordedMulticast().size());
        Assert.assertTrue(clientMock.getRecordedMulticast().get(0).getMessages().toString().contains("slux sent this message from"));
        Assert.assertTrue(clientMock.getRecordedMulticast().get(0).getMessages().toString().contains("Hello World"));
        Assert.assertEquals(100, clientMock.getRecordedMulticast().get(0).getTo().size());
        Assert.assertFalse(clientMock.getRecordedMulticast().get(0).getTo().contains(LineMessagingClientMock.SPLIT_USER_ID));
        Assert.assertTrue(clientMock.getRecordedMulticast().get(1).getMessages().toString().contains("slux sent this message from"));
        Assert.assertTrue(clientMock.getRecordedMulticast().get(1).getMessages().toString().contains("Hello World"));
        Assert.assertEquals(80, clientMock.getRecordedMulticast().get(1).getTo().size());
        Assert.assertTrue(clientMock.getRecordedMulticast().get(1).getTo().contains(LineMessagingClientMock.SPLIT_USER_ID));

        clientMock.cleanupMulticast();
        response = friday.handleTextMessageEvent(event20);
        Assert.assertEquals("Message sent privately to 20 user(s)", response.getText());
        Assert.assertEquals(1, clientMock.getRecordedMulticast().size());
        Assert.assertTrue(clientMock.getRecordedMulticast().get(0).getMessages().toString().contains("slux sent this message from"));
        Assert.assertTrue(clientMock.getRecordedMulticast().get(0).getMessages().toString().contains("Hello World"));
        Assert.assertEquals(20, clientMock.getRecordedMulticast().get(0).getTo().size());
        Assert.assertFalse(clientMock.getRecordedMulticast().get(0).getTo().contains(LineMessagingClientMock.SPLIT_USER_ID));

        clientMock.cleanupMulticast();
        response = friday.handleTextMessageEvent(event1);
        Assert.assertEquals("It looks like you are the only one in this room. Operation aborted.", response.getText());
        Assert.assertEquals(0, clientMock.getRecordedMulticast().size());

        clientMock.cleanupMulticast();
        response = friday.handleTextMessageEvent(eventNoText);
        Assert.assertEquals("Please provide a message to send to all the members of this chat group", response.getText());

        clientMock.cleanupMulticast();
        response = friday.handleTextMessageEvent(eventSpammerTest);
        Assert.assertEquals("Sorry, you can't spam multicast messages. You will be able to send another multicast message in 60 minute(s)", response.getText());
    }
}
