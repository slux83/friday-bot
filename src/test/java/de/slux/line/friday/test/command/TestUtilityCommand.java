/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.InfoCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
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
		FridayBotApplication jarvis = new FridayBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEvent(null, FridayBotApplication.SLUX_ID,
		        AdminStatusCommand.CMD_PREFIX);

		jarvis.getIncomingMsgCounter().set(10);
		Thread.sleep(1000);
		TextMessage response = jarvis.handleTextMessageEvent(event);

		System.out.println(response);

		event = MessageEventUtil.createMessageEvent(null, FridayBotApplication.SLUX_ID,
		        AdminStatusCommand.CMD_PREFIX + " fake foo bar");
		response = jarvis.handleTextMessageEvent(event);

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
	public void testInfoCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication jarvis = new FridayBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		MessageEvent<TextMessageContent> event = MessageEventUtil.createMessageEvent(UUID.randomUUID().toString(),
		        UUID.randomUUID().toString(), InfoCommand.CMD_PREFIX);

		TextMessage response = jarvis.handleTextMessageEvent(event);

		assertTrue(response.getText().contains("J.A.R.V.I.S. MCOC Line Bot"));
	}

}
