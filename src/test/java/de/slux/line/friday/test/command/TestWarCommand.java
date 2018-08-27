/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarDeleteCommand;
import de.slux.line.friday.command.war.WarHistoryCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.command.war.WarReportDeathCommand;
import de.slux.line.friday.command.war.WarResetCommand;
import de.slux.line.friday.command.war.WarSaveCommand;
import de.slux.line.friday.command.war.WarSummaryDeathCommand;
import de.slux.line.friday.command.war.WarSummonerNodeCommand;
import de.slux.line.friday.command.war.WarSummonerRenameCommand;
import de.slux.line.friday.command.war.WarUndoDeathCommand;
import de.slux.line.friday.logic.war.WarDeathLogic;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;

/**
 * @author slux
 */
public class TestWarCommand {

	@Test
	public void testNominalWarDeathReportingWorkflowCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

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

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarAddSummonersCommand.CMD_PREFIX + " slux83, John Doe, Nemesis The Best, Tony 88");
		MessageEvent<TextMessageContent> summonersPrintCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarAddSummonersCommand.CMD_PREFIX);

		// Summoner node
		MessageEvent<TextMessageContent> summoner1NodeCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummonerNodeCommand.CMD_PREFIX + " 3A 55 5* dupe IMIW");
		MessageEvent<TextMessageContent> summoner2NodeCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummonerNodeCommand.CMD_PREFIX + " 1E 22 4* dupe Mephisto");
		MessageEvent<TextMessageContent> summoner3NodeCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummonerNodeCommand.CMD_PREFIX + " 3B 12 5* undupe Sentinel");
		MessageEvent<TextMessageContent> summoner3BisNodeCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummonerNodeCommand.CMD_PREFIX + " 3B 12 5* duped Ronan"); // replace

		// Summoner rename
		MessageEvent<TextMessageContent> summonerRenameCmd = MessageEventUtil.createMessageEvent(groupId, userId,
		        WarSummonerRenameCommand.CMD_PREFIX + " 3 Foo Bar 1");

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
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(death1Cmd);
		response = friday.handleTextMessageEvent(death2Cmd);
		response = friday.handleTextMessageEvent(death3Cmd);
		assertTrue(response.getText().contains("480"));
		assertTrue(response.getText().contains("7"));

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertTrue(response.getText().contains("5* dupe KP"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(undoDeathCmd);
		assertTrue(response.getText().contains("WAR DEATH REPORT"));

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertFalse(response.getText().contains("5* dupe KP"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersPrintCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summonersAddCmd);
		assertTrue(response.getText().contains("slux83"));
		assertTrue(response.getText().contains("Tony 88"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner1NodeCmd);
		assertTrue(response.getText().contains("A. 5* dupe IMIW (55)"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summoner2NodeCmd);
		assertTrue(response.getText().contains("E. 4* dupe Mephisto (22)"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summoner3NodeCmd);
		assertTrue(response.getText().contains("B. 5* undupe Sentinel (12)"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summoner3BisNodeCmd);
		assertTrue(response.getText().contains("B. 5* duped Ronan (12)"));
		assertFalse(response.getText().contains("B. 5* undupe Sentinel (12)"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summonerRenameCmd);
		assertTrue(response.getText().contains("3. Foo Bar 1"));
		assertFalse(response.getText().contains("3. Nemesis The Best"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(resetWarCmd);
		assertTrue(response.getText().contains("War reports cleared"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(summonersPrintCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(response.getText().contains(WarDeathLogic.SDF.format(new Date())));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(specificHistoryWarCmd);
		assertNull(response);
		String history = callback.takeAllMessages();
		assertTrue(history.contains("6* NC"));
		assertTrue(history.contains("4. Tony 88"));
		assertTrue(history.contains("A. 5* dupe IMIW (55)"));

		response = friday.handleTextMessageEvent(deleteHistoryWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(response.getText().contains(WarDeathLogic.SDF.format(new Date())));
		assertTrue(callback.takeAllMessages().isEmpty());
	}
}
