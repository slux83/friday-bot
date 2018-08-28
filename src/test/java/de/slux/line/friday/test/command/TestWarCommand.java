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
import de.slux.line.friday.command.HelpCommand;
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
	public void testHelpCommandInGroup() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Register command
		MessageEvent<TextMessageContent> helpCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        HelpCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(helpCmd);
		assertTrue(response.getText().contains(WarAddSummonersCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(helpCmd);
		assertTrue(response.getText().contains(WarAddSummonersCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());
	}

	@Test
	public void testNonNominalWarDeathReportingWorkflowCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");
		MessageEvent<TextMessageContent> registerMissingArgCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarRegisterCommand.CMD_PREFIX);

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " err 55 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 2 err 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 2 44");
		MessageEvent<TextMessageContent> death4Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 2 44 Medusa");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersTooLongAddCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, WarAddSummonersCommand.CMD_PREFIX + " 1,2,3,4,5,6,7,8,9,10,11");

		// Summoner node
		MessageEvent<TextMessageContent> summoner1NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 30A 55 5* dupe IMIW");
		MessageEvent<TextMessageContent> summoner2NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 10A 22 4* dupe Mephisto");
		MessageEvent<TextMessageContent> summoner3NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " XX 12 5* undupe Sentinel");
		MessageEvent<TextMessageContent> summoner4NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 3B err 5* duped Ronan");
		MessageEvent<TextMessageContent> summoner5NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 3B 55");
		MessageEvent<TextMessageContent> summoner6NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX);

		// Summoner rename
		MessageEvent<TextMessageContent> summonerRename1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerRenameCommand.CMD_PREFIX + " 20 Foo Bar 1");
		MessageEvent<TextMessageContent> summonerRename2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerRenameCommand.CMD_PREFIX + " err Foo Bar 1");
		MessageEvent<TextMessageContent> summonerRename3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerRenameCommand.CMD_PREFIX);

		// Save war command
		MessageEvent<TextMessageContent> saveWarNoArgCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSaveCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarSaveCommand.CMD_PREFIX + " DH DM");

		// Specific history command
		MessageEvent<TextMessageContent> specificHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarHistoryCommand.CMD_PREFIX + " wrong_date");

		// Delete history command
		MessageEvent<TextMessageContent> deleteHistory1WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarDeleteCommand.CMD_PREFIX + " wrong_date DH DM");
		MessageEvent<TextMessageContent> deleteHistor2WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarDeleteCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()));
		MessageEvent<TextMessageContent> deleteHistor3WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarDeleteCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()) + " 4Loki");

		/* Begin */
		TextMessage response = friday.handleTextMessageEvent(registerMissingArgCmd);
		assertTrue(response.getText().contains("Please specify your group name"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(death1Cmd);
		assertTrue(response.getText().contains("Number expected for <deaths>"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(death2Cmd);
		assertTrue(response.getText().contains("Number expected for <node>"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(death3Cmd);
		assertTrue(response.getText().contains("Incorrect syntax"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersTooLongAddCmd);
		assertTrue(response.getText().contains("You can add a maximum of"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner1NodeCmd);
		assertTrue(response.getText().contains("Invalid argument"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner2NodeCmd);
		assertTrue(response.getText().contains("cannot find the specified summoner at position 10"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner3NodeCmd);
		assertTrue(response.getText().contains("Invalid argument"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner4NodeCmd);
		assertTrue(response.getText().contains("Invalid node number"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner5NodeCmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner6NodeCmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerRename1Cmd);
		assertTrue(response.getText().contains("cannot find the specified summoner at position 20"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerRename2Cmd);
		assertTrue(response.getText().contains("Invalid argument"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerRename3Cmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(saveWarNoArgCmd);
		assertTrue(response.getText().contains("Missing argument"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// We add something
		response = friday.handleTextMessageEvent(death4Cmd);
		assertTrue(response.getText().contains("WAR DEATH REPORT"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// Previous one was empty
		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// Repeat again to trigger the consistency check
		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("has been already registered today"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(specificHistoryWarCmd);
		assertTrue(response.getText().contains("Incorrect date syntax"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteHistory1WarCmd);
		assertTrue(response.getText().contains("Incorrect date syntax"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteHistor2WarCmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteHistor3WarCmd);
		assertTrue(response.getText().contains("Could not find any war against '4Loki'"));
		assertTrue(callback.takeAllMessages().isEmpty());

	}

	@Test
	public void testResponseTooBig() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		for (int i = 1; i <= 55; i++) {
			MessageEvent<TextMessageContent> deathCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
			        WarReportDeathCommand.CMD_PREFIX + " 1 " + i + " 5* duped Dormammu" + i);

			response = friday.handleTextMessageEvent(deathCmd);
			assertTrue(response.getText().contains("Total deaths: " + i));
			assertTrue(callback.takeAllMessages().isEmpty());
		}

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummaryDeathCommand.CMD_PREFIX);

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertNull(response);
		String pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.length() > 2000);
		assertTrue(pushedMessages.contains("Total deaths: 55"));
		
		
	}

	@Test
	public void testNominalWarDeathReportingWorkflowCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarRegisterCommand.CMD_PREFIX + " group1");

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 2 55 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 1 24 6* NC");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarReportDeathCommand.CMD_PREFIX + " 4 28 5* dupe KP");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarAddSummonersCommand.CMD_PREFIX + " slux83, John Doe, Nemesis The Best, Tony 88");
		MessageEvent<TextMessageContent> summonersPrintCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarAddSummonersCommand.CMD_PREFIX);

		// Summoner node
		MessageEvent<TextMessageContent> summoner1NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 3A 55 5* dupe IMIW");
		MessageEvent<TextMessageContent> summoner2NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 1E 22 4* dupe Mephisto");
		MessageEvent<TextMessageContent> summoner3NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 3B 12 5* undupe Sentinel");
		MessageEvent<TextMessageContent> summoner3BisNodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerNodeCommand.CMD_PREFIX + " 3B 12 5* duped Ronan"); // replace

		// Summoner rename
		MessageEvent<TextMessageContent> summonerRenameCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummonerRenameCommand.CMD_PREFIX + " 3 Foo Bar 1");

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarSummaryDeathCommand.CMD_PREFIX);

		// Undo death command
		MessageEvent<TextMessageContent> undoDeathCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarUndoDeathCommand.CMD_PREFIX);

		// Save war command
		MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarSaveCommand.CMD_PREFIX + " DH DM");

		// Reset war command
		MessageEvent<TextMessageContent> resetWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarResetCommand.CMD_PREFIX);

		// All history command
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        WarHistoryCommand.CMD_PREFIX);

		// Specific history command
		MessageEvent<TextMessageContent> specificHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarHistoryCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()));

		// Delete history command
		MessageEvent<TextMessageContent> deleteHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, WarDeleteCommand.CMD_PREFIX + " " + WarDeathLogic.SDF.format(new Date()) + " DH DM");

		/* Start the workflow */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("No records found"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(specificHistoryWarCmd);
		assertNull(response);
		String history = callback.takeAllMessages();
		assertTrue(history.contains("No death reports found for"));
		assertTrue(history.contains("No placement reports found for"));

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
		history = callback.takeAllMessages();
		assertTrue(history.contains("6* NC"));
		assertTrue(history.contains("4. Tony 88"));
		assertTrue(history.contains("A. 5* dupe IMIW (55)"));

		response = friday.handleTextMessageEvent(deleteHistoryWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(response.getText().contains(WarDeathLogic.SDF.format(new Date())));
		assertTrue(callback.takeAllMessages().isEmpty());
	}
}
