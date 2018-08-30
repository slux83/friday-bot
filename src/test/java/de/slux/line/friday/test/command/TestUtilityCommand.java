/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.InfoCommand;
import de.slux.line.friday.command.admin.AdminBroadcastCommand;
import de.slux.line.friday.command.admin.AdminHelpCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.WarHistoryCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
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
	public void testInfoCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEventGroupSource(
		        UUID.randomUUID().toString(), UUID.randomUUID().toString(), InfoCommand.CMD_PREFIX);

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
		assertTrue(answer.contains(HelpCommand.CMD_PREFIX));
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
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Leave event
		LeaveEvent event = MessageEventUtil.createLeaveEvent(groupId, userId);

		// History command to test the real exit of the bot from the group
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarHistoryCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		friday.handleDefaultMessageEvent(event);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("This group is unregistered"));
		assertTrue(callback.takeAllMessages().isEmpty());

	}

	@Test
	public void testAdminUserEvent() throws Exception {
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
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Admin help command
		MessageEvent<TextMessageContent> adminHelpCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AdminHelpCommand.CMD_PREFIX);

		// Admin status command
		MessageEvent<TextMessageContent> adminStatusCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AdminStatusCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> adminStatusOperCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AdminStatusCommand.CMD_PREFIX + " operational");
		MessageEvent<TextMessageContent> adminStatusMaintCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AdminStatusCommand.CMD_PREFIX + " maintenance");
		MessageEvent<TextMessageContent> adminStatusInvalidCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AdminStatusCommand.CMD_PREFIX + " invalid");

		// Leave event
		LeaveEvent leaveEvent = MessageEventUtil.createLeaveEvent(groupId, userId);

		// Admin broadcast command
		MessageEvent<TextMessageContent> adminBroadcastCmd = MessageEventUtil.createMessageEventUserSource(
		        FridayBotApplication.SLUX_ID, AdminBroadcastCommand.CMD_PREFIX + " hello everyone!");
		MessageEvent<TextMessageContent> adminBroadcastNoArgCmd = MessageEventUtil
		        .createMessageEventUserSource(FridayBotApplication.SLUX_ID, AdminBroadcastCommand.CMD_PREFIX);

		/* Begin */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("group1"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminHelpCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains(AdminHelpCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(AdminBroadcastCommand.CMD_PREFIX));
		assertTrue(response.getText().contains(AdminStatusCommand.CMD_PREFIX));
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

		response = friday.handleTextMessageEvent(adminStatusOperCmd);
		assertNotNull(response);
		assertFalse(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("OPERATIONAL"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminStatusInvalidCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("WARNING"));
		assertTrue(response.getText().contains("Version"));
		assertTrue(response.getText().contains("OPERATIONAL"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminBroadcastCmd);
		assertNotNull(response);
		String bcastResponse = response.getText();
		System.out.println(bcastResponse);
		assertTrue(response.getText().contains("Message broadcasted"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));

		response = friday.handleTextMessageEvent(adminBroadcastNoArgCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("Please provide a message to broadcast"));
		assertTrue(callback.takeAllMessages().isEmpty());

		friday.handleDefaultMessageEvent(leaveEvent);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(adminBroadcastCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("Message broadcasted"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));
		// One less active group
		assertNotEquals(bcastResponse, response.getText());
		System.out.println(response.getText());

		response = friday.handleTextMessageEvent(adminStatusCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("Version"));
		System.out.println(response);
		assertNotEquals(statusResponse, response.getText().substring(response.getText().indexOf("Active/Total")));
		assertTrue(callback.takeAllMessages().isEmpty());
	}
}
