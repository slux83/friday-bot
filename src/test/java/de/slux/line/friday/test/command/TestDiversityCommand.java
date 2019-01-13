/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertEquals;
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
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarDeleteCommand;
import de.slux.line.friday.command.war.WarDeleteNodeCommand;
import de.slux.line.friday.command.war.WarDiversityCommand;
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
import de.slux.line.friday.test.util.PostConstructHolder;

/**
 * @author slux
 */
public class TestDiversityCommand {

	@Test
	public void testWarDiversityCommand() throws Exception {
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

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX
		                + " 2 55 5* Dormammu someSummoner");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 1 24 6* Sparky slux");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 4 28 5* Dorma blasto55");
		MessageEvent<TextMessageContent> death4Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 1 11 5* Domino slux83");
		MessageEvent<TextMessageContent> death5Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 14 5* somewrong fake");
		MessageEvent<TextMessageContent> death6Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 15 5* somemorewrong fake");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
		                + " slux83, John Doe, Fool");

		// Summoner node
		MessageEvent<TextMessageContent> summonerNodesCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + 
		        " 1a 28 5* Dormammu,"+
		        " 1b 55 5* dupe IMIW,"+
		        " 1c 54 5* Abomination,"+
		        " 1d 36 5* Dr Voodoo,"+
		        " 2a 24 5* dupe Storm,"+
		        " 2b 15 5* dupe Scarlet," +
		        " 3a 35 anotherfake");
		

		// Diversity command
		MessageEvent<TextMessageContent> diversityCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarDiversityCommand.CMD_PREFIX);

		MessageEvent<TextMessageContent> diversityVerboseCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDiversityCommand.CMD_PREFIX + " verbose");

		/* Start the workflow */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));


		response = friday.handleTextMessageEvent(diversityVerboseCmd);
		assertTrue(response.getText().contains("nothing found"));

		response = friday.handleTextMessageEvent(death1Cmd);
		response = friday.handleTextMessageEvent(death2Cmd);
		response = friday.handleTextMessageEvent(death3Cmd);
		response = friday.handleTextMessageEvent(death4Cmd);
		response = friday.handleTextMessageEvent(death5Cmd);
		response = friday.handleTextMessageEvent(death6Cmd);
		assertTrue(response.getText().contains("880"));
		assertTrue(response.getText().contains("12"));
		assertTrue(response.getText().contains("6/55"));

		response = friday.handleTextMessageEvent(summonersAddCmd);
		assertTrue(response.getText().contains("3 new"));
		
		response = friday.handleTextMessageEvent(summonerNodesCmd);
		assertTrue(response.getText().contains("slux83"));
		assertTrue(response.getText().contains("5* dupe Scarlet (15)"));
		
		response = friday.handleTextMessageEvent(diversityCmd);
		assertTrue(response.getText().contains("7/9"));

		response = friday.handleTextMessageEvent(diversityVerboseCmd);
		assertTrue(response.getText().contains("Abomination : [54]"));
		assertTrue(response.getText().contains("Domino : [11]"));
		assertTrue(response.getText().contains("somewrong fake : [14]"));
		assertTrue(response.getText().contains("anotherfake : [35]"));

	}

}
