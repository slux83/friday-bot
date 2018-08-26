/**
 * 
 */
package de.slux.line.jarvis.test.command;

import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.command.InfoCommand;
import de.slux.line.jarvis.command.admin.AdminStatusCommand;
import de.slux.line.jarvis.command.war.WarAddSummonersCommand;
import de.slux.line.jarvis.command.war.WarRegisterCommand;
import de.slux.line.jarvis.command.war.WarSummonerNodeCommand;
import de.slux.line.jarvis.command.war.WarSummonerRenameCommand;
import de.slux.line.jarvis.test.util.LineMessagingClientMock;
import de.slux.line.jarvis.test.util.MessagingClientCallbackImpl;

/**
 * @author slux
 */
public class TestCommand {

	@Test
	public void testAdminStatusCommand() throws Exception {

		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		Source source = new UserSource(JarvisBotApplication.SLUX_ID);
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", AdminStatusCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		jarvis.getIncomingMsgCounter().set(10);
		Thread.sleep(1000);
		TextMessage response = jarvis.handleTextMessageEvent(event);

		System.out.println(response);

		message = new TextMessageContent("001", AdminStatusCommand.CMD_PREFIX + " fake foo bar");
		event = new MessageEvent<TextMessageContent>("reply-token", source, message, timestamp);
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
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		Source source = new GroupSource("group-id", "user-id");
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", InfoCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		TextMessage response = jarvis.handleTextMessageEvent(event);

		assertTrue(response.getText().contains("J.A.R.V.I.S. MCOC Line Bot"));
	}

	@Test
	public void testAddSummonersCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		// Add summoners command
		Source source = new GroupSource("group-id", "user-id");
		Instant timestamp = Instant.now();
		String summoners = " Summoner 1   , Summoner2, Summoner 3";
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX + summoners);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id", "user-id");
		TextMessageContent messageRegister = new TextMessageContent("001",
		        WarRegisterCommand.CMD_PREFIX + " test-group");
		MessageEvent<TextMessageContent> eventRegister = new MessageEvent<TextMessageContent>("reply-token",
		        sourceRegister, messageRegister, timestamp);

		TextMessage response = jarvis.handleTextMessageEvent(eventRegister);
		Assert.assertTrue(response.getText().contains("registered using the name test-group"));

		response = jarvis.handleTextMessageEvent(event);
		Assert.assertTrue(callback.takeAllMessages().contains("Summoner2"));

		// Edit element
		Source sourceEdit = new GroupSource("group-id", "user-id");
		TextMessageContent messageEdit = new TextMessageContent("001",
		        WarSummonerNodeCommand.CMD_PREFIX + " 2E 5 5* dupe Medusa");
		MessageEvent<TextMessageContent> eventEdit = new MessageEvent<TextMessageContent>("reply-token", sourceEdit,
		        messageEdit, timestamp);

		response = jarvis.handleTextMessageEvent(eventEdit);
		String pushedText = callback.takeAllMessages();
		Assert.assertTrue(pushedText.contains("Summoner2"));
		Assert.assertTrue(pushedText.contains("5* dupe Medusa"));

		// Rename summoner
		Source sourceRename = new GroupSource("group-id", "user-id");
		TextMessageContent messageRename = new TextMessageContent("001",
		        WarSummonerRenameCommand.CMD_PREFIX + " 1 slux 83");
		MessageEvent<TextMessageContent> eventRename = new MessageEvent<TextMessageContent>("reply-token", sourceRename,
		        messageRename, timestamp);

		response = jarvis.handleTextMessageEvent(eventRename);
		pushedText = callback.takeAllMessages();
		Assert.assertFalse(pushedText.contains("Summoner 1"));
		Assert.assertTrue(pushedText.contains("slux 83"));
	}

	@Test
	public void testAddSummonersCommandTooMany() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		// Add summoners command
		Source source = new GroupSource("group-id1", "user-id");
		Instant timestamp = Instant.now();
		String summoners = " Summoner 1, Summoner 2, Summoner 3, Summoner 4, Summoner 5, Summoner 6, Summoner 7, Summoner 8, Summoner 9, Summoner 10, Summoner 11";
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX + summoners);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id1", "user-id");
		TextMessageContent messageRegister = new TextMessageContent("001",
		        WarRegisterCommand.CMD_PREFIX + " test-group");
		MessageEvent<TextMessageContent> eventRegister = new MessageEvent<TextMessageContent>("reply-token",
		        sourceRegister, messageRegister, timestamp);

		TextMessage response = jarvis.handleTextMessageEvent(eventRegister);
		Assert.assertTrue(response.getText().contains("registered using the name test-group"));

		response = jarvis.handleTextMessageEvent(event);
		Assert.assertTrue(response.getText().contains("You can add a maximum of"));
	}

	@Test
	public void testAddSummonersJustPrint() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		// Add summoners command
		Source source = new GroupSource("group-id", "user-id");
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id", "user-id");
		TextMessageContent messageRegister = new TextMessageContent("001",
		        WarRegisterCommand.CMD_PREFIX + " test-group");
		MessageEvent<TextMessageContent> eventRegister = new MessageEvent<TextMessageContent>("reply-token",
		        sourceRegister, messageRegister, timestamp);

		TextMessage resp = jarvis.handleTextMessageEvent(eventRegister);
		Assert.assertTrue(resp.getText().contains("registered using the name test-group"));

		jarvis.handleTextMessageEvent(event);
		String response = callback.takeAllMessages();
		Assert.assertTrue(response.contains("CURRENT WAR PLACEMENT"));
	}
}
