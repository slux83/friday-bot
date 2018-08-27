/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.InfoCommand;
import de.slux.line.friday.command.admin.AdminBroadcastCommand;
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

	@Test
	public void testAdminStatusCommand() throws Exception {

		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEvent(null, FridayBotApplication.SLUX_ID,
		        AdminStatusCommand.CMD_PREFIX);

		friday.getCommandIncomingMsgCounter().set(10);
		friday.getTotalIncomingMsgCounter().set(100);
		Thread.sleep(1000);
		TextMessage response = friday.handleTextMessageEvent(event);

		System.out.println(response);

		event = MessageEventUtil.createMessageEvent(null, FridayBotApplication.SLUX_ID,
		        AdminStatusCommand.CMD_PREFIX + " fake foo bar");
		response = friday.handleTextMessageEvent(event);

		Assert.assertTrue(response.getText().contains("status fake"));

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
	public void testAdminBroadcastCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEvent(null, FridayBotApplication.SLUX_ID,
		        AdminBroadcastCommand.CMD_PREFIX + " hello everyone!");

		TextMessage response = friday.handleTextMessageEvent(event);

		assertNotNull(response);
		assertTrue(response.getText().contains("Message broadcasted"));
		assertTrue(callback.takeAllMessages().contains("hello everyone!"));

	}

	@Test
	public void testInfoCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEvent(UUID.randomUUID().toString(),
		        UUID.randomUUID().toString(), InfoCommand.CMD_PREFIX);

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
	public void testLeaveEvent() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Leave event
		LeaveEvent event = MessageEventUtil.createLeaveEvent(groupId, userId);

		// History command to test the real exit of the bot from the group
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarHistoryCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		friday.handleDefaultMessageEvent(event);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("This group is unregistered"));
		assertTrue(callback.takeAllMessages().isEmpty());

	}
}
