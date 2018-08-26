/**
 * 
 */
package de.slux.line.jarvis.test.command;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.command.war.WarAddSummonersCommand;
import de.slux.line.jarvis.command.war.WarRegisterCommand;
import de.slux.line.jarvis.command.war.WarReportDeathCommand;
import de.slux.line.jarvis.command.war.WarSummaryDeathCommand;
import de.slux.line.jarvis.command.war.WarSummonerNodeCommand;
import de.slux.line.jarvis.command.war.WarSummonerRenameCommand;
import de.slux.line.jarvis.command.war.WarUndoDeathCommand;
import de.slux.line.jarvis.test.util.LineMessagingClientMock;
import de.slux.line.jarvis.test.util.MessageEventUtil;
import de.slux.line.jarvis.test.util.MessagingClientCallbackImpl;

/**
 * @author slux
 */
public class TestWarCommand {

	@Test
	public void testNominalWarDeathReportingWorkflowCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 2 55 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 1 24 6* NC");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 4 28 5* dupe KP");

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummaryDeathCommand.CMD_PREFIX);

		// Undo death command
		MessageEvent<TextMessageContent> undoDeathCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarUndoDeathCommand.CMD_PREFIX);

		/* Start the workflow */
		TextMessage response = jarvis.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = jarvis.handleTextMessageEvent(death1Cmd);
		response = jarvis.handleTextMessageEvent(death2Cmd);
		response = jarvis.handleTextMessageEvent(death3Cmd);
		assertTrue(response.getText().contains("480"));
		assertTrue(response.getText().contains("7"));

		// TODO: complete me
	}

	@Test
	public void testAddSummonersCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.setLineMessagingClient(new LineMessagingClientMock(callback));
		jarvis.postConstruct();

		// Add summoners command
		Source source = new GroupSource("group-id", UUID.randomUUID().toString());
		Instant timestamp = Instant.now();
		String summoners = " Summoner 1   , Summoner2, Summoner 3";
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX + summoners);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id", UUID.randomUUID().toString());
		TextMessageContent messageRegister = new TextMessageContent("001",
		        WarRegisterCommand.CMD_PREFIX + " test-group");
		MessageEvent<TextMessageContent> eventRegister = new MessageEvent<TextMessageContent>("reply-token",
		        sourceRegister, messageRegister, timestamp);

		TextMessage response = jarvis.handleTextMessageEvent(eventRegister);
		Assert.assertTrue(response.getText().contains("registered using the name test-group"));

		response = jarvis.handleTextMessageEvent(event);
		Assert.assertTrue(callback.takeAllMessages().contains("Summoner2"));

		// Edit element
		Source sourceEdit = new GroupSource("group-id", UUID.randomUUID().toString());
		TextMessageContent messageEdit = new TextMessageContent("001",
		        WarSummonerNodeCommand.CMD_PREFIX + " 2E 5 5* dupe Medusa");
		MessageEvent<TextMessageContent> eventEdit = new MessageEvent<TextMessageContent>("reply-token", sourceEdit,
		        messageEdit, timestamp);

		response = jarvis.handleTextMessageEvent(eventEdit);
		String pushedText = callback.takeAllMessages();
		Assert.assertTrue(pushedText.contains("Summoner2"));
		Assert.assertTrue(pushedText.contains("5* dupe Medusa"));

		// Rename summoner
		Source sourceRename = new GroupSource("group-id", UUID.randomUUID().toString());
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
		Source source = new GroupSource("group-id1", UUID.randomUUID().toString());
		Instant timestamp = Instant.now();
		String summoners = " Summoner 1, Summoner 2, Summoner 3, Summoner 4, Summoner 5, Summoner 6, Summoner 7, Summoner 8, Summoner 9, Summoner 10, Summoner 11";
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX + summoners);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id1", UUID.randomUUID().toString());
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
		Source source = new GroupSource("group-id", UUID.randomUUID().toString());
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", WarAddSummonersCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		// Register command
		Source sourceRegister = new GroupSource("group-id", UUID.randomUUID().toString());
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
