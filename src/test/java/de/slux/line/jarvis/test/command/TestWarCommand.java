/**
 * 
 */
package de.slux.line.jarvis.test.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
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
import de.slux.line.jarvis.command.war.WarDeleteCommand;
import de.slux.line.jarvis.command.war.WarHistoryCommand;
import de.slux.line.jarvis.command.war.WarRegisterCommand;
import de.slux.line.jarvis.command.war.WarReportDeathCommand;
import de.slux.line.jarvis.command.war.WarResetCommand;
import de.slux.line.jarvis.command.war.WarSaveCommand;
import de.slux.line.jarvis.command.war.WarSummaryDeathCommand;
import de.slux.line.jarvis.command.war.WarSummonerNodeCommand;
import de.slux.line.jarvis.command.war.WarSummonerRenameCommand;
import de.slux.line.jarvis.command.war.WarUndoDeathCommand;
import de.slux.line.jarvis.logic.war.WarDeathLogic;
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

		// Save war command
		MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSaveCommand.CMD_PREFIX + " DH DM");

		// Reset war command
		MessageEvent<TextMessageContent> resetWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarResetCommand.CMD_PREFIX);

		// All history command
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarHistoryCommand.CMD_PREFIX);

		// Specific history command
		MessageEvent<TextMessageContent> specificHistoryWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarHistoryCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()));

		// Delete history command
		MessageEvent<TextMessageContent> deleteHistoryWarCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarDeleteCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()) + " DH DM");

		/* Start the workflow */
		TextMessage response = jarvis.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = jarvis.handleTextMessageEvent(death1Cmd);
		response = jarvis.handleTextMessageEvent(death2Cmd);
		response = jarvis.handleTextMessageEvent(death3Cmd);
		assertTrue(response.getText().contains("480"));
		assertTrue(response.getText().contains("7"));

		response = jarvis.handleTextMessageEvent(deathSummaryCmd);
		assertNull(response);
		assertTrue(callback.takeAllMessages().contains("5* dupe KP"));

		response = jarvis.handleTextMessageEvent(undoDeathCmd);
		assertTrue(response.getText().contains("WAR DEATH REPORT"));

		response = jarvis.handleTextMessageEvent(deathSummaryCmd);
		assertNull(response);
		assertFalse(callback.takeAllMessages().contains("5* dupe KP"));

		response = jarvis.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = jarvis.handleTextMessageEvent(resetWarCmd);
		assertTrue(response.getText().contains("War reports cleared"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = jarvis.handleTextMessageEvent(historyWarCmd);
		assertNull(response);
		String allMessages = callback.takeAllMessages();
		assertTrue(allMessages.contains("DH DM"));
		assertTrue(allMessages.contains(WarDeathLogic.SDF.format(new Date())));

		response = jarvis.handleTextMessageEvent(specificHistoryWarCmd);
		assertNull(response);
		assertTrue(callback.takeAllMessages().contains("6* NC"));

		response = jarvis.handleTextMessageEvent(deleteHistoryWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(response.getText().contains(WarDeathLogic.SDF.format(new Date())));
		assertTrue(callback.takeAllMessages().isEmpty());
	}
}
