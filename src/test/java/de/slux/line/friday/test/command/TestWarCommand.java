/**
 * 
 */
package de.slux.line.friday.test.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.google.common.base.Strings;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.AbstractCommand.CommandType;
import de.slux.line.friday.command.EventInfoCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarDeleteCommand;
import de.slux.line.friday.command.war.WarDeleteNodeCommand;
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
import de.slux.line.friday.scheduler.McocSchedulerImporter;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;

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

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

		// Help command
		MessageEvent<TextMessageContent> helpCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(helpCmd);
		assertNotNull(response);
		String help = response.getText();
		System.out.println(help);
		assertFalse(help.isEmpty());
		assertTrue(help.contains(WarAddSummonersCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		for (AbstractCommand c : friday.getCommands()) {
			// Help command with option
			MessageEvent<TextMessageContent> helpDetailedCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
			        userId, AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX + " " + c.getCommandPrefix());

			if (c.getType().equals(CommandType.CommandTypeShared) || c.getType().equals(CommandType.CommandTypeUtility)
			        || c.getType().equals(CommandType.CommandTypeWar)) {

				response = friday.handleTextMessageEvent(helpDetailedCmd);
				assertNotNull(response);
				help = response.getText();
				System.out.println(help);
				System.out.println();
				assertFalse(help.isEmpty());
				assertFalse(help.contains(AdminStatusCommand.CMD_PREFIX));
				assertTrue(help.contains(c.getCommandPrefix()));
				assertEquals(AbstractCommand.ALL_CMD_PREFIX + " " + c.getHelp(true), help);
				assertTrue(callback.takeAllMessages().isEmpty());
			}
		}

		response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(helpCmd);
		assertNotNull(response);
		help = response.getText();
		assertFalse(help.isEmpty());
		assertTrue(callback.takeAllMessages().isEmpty());
		assertTrue(help.contains(WarAddSummonersCommand.CMD_PREFIX));
		assertTrue(help.contains(EventInfoCommand.CMD_PREFIX));
	}

	@Test
	public void testNonNominalWarDeathReportingWorkflowCommands() throws Exception {
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
		MessageEvent<TextMessageContent> registerMissingArgCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX);

		// Reset command
		MessageEvent<TextMessageContent> resetCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarResetCommand.CMD_PREFIX);

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " err 55 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 err 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 44");
		MessageEvent<TextMessageContent> death4Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 44 Medusa");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersTooLongAddCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX + " 1,2,3,4,5,6,7,8,9,10,11");
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
		                + " slux83, John Doe, Nemesis The Best, Tony 88");

		// Summoner node
		MessageEvent<TextMessageContent> summoner1NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 30A 55 5* dupe IMIW");
		MessageEvent<TextMessageContent> summoner2NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 10A 22 4* dupe Mephisto");
		MessageEvent<TextMessageContent> summoner3NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " XX 12 5* undupe Sentinel");
		MessageEvent<TextMessageContent> summoner4NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3B err 5* duped Ronan");
		MessageEvent<TextMessageContent> summoner5NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3B 55");
		MessageEvent<TextMessageContent> summoner6NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> summonerNodeMulti1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX
		                + " 2A 11 5* Elektra, 2Z 31 4* domino,6C 53 NC,4C 58 Thor, 1E Elektro");

		// Error case shown in 0.0.4
		MessageEvent<TextMessageContent> summonersAddAllCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX + " sum1,sum2,sum3,sum4,sum5");
		MessageEvent<TextMessageContent> summonerNodeMultiWithInvalidMixedCmd = MessageEventUtil
		        .createMessageEventGroupSource(groupId, userId,
		                AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 1blablabla, \n"
		                        + "1A 47 5* duped Quake,\n" + "1B 50 5* duped King Groot,\n"
		                        + "1C 24 6* duped Kingpin,\n" + "1D 16 5* duped Cable,\n" + "1E 29 5* duped Medusa, \n"
		                        + "2blablabla, \n" + "2A 55 6* sabretooth, \n" + "2B 31 5* Domino, \n"
		                        + "2C 39 5* duped Magik, \n" + "2D 5 5* duped Mordo, \n" + "2E 11 6* Kingpin,\n"
		                        + "3blablaRydah, \n" + "3A 20 5* duped Ghostrider,\n" + "3B 8 5* Rogue,\n"
		                        + "3C 6 6* IMIW,\n" + "3D 17 5* duped Gwenpool,\n" + "3E 35 6* Kingpin,\n"
		                        + "4blabalbal, \n" + "4A 3 5* Sentry,\n" + "4B 21 6* Kingpin,");

		// Summoner rename
		MessageEvent<TextMessageContent> summonerRename1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerRenameCommand.CMD_PREFIX + " 20 Foo Bar 1");
		MessageEvent<TextMessageContent> summonerRename2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerRenameCommand.CMD_PREFIX + " err Foo Bar 1");
		MessageEvent<TextMessageContent> summonerRename3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerRenameCommand.CMD_PREFIX);

		// Save war command
		MessageEvent<TextMessageContent> saveWarNoArgCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSaveCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSaveCommand.CMD_PREFIX + " DH DM");

		// Specific history command
		MessageEvent<TextMessageContent> specificHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX + " wrong_date");

		// Delete history command
		MessageEvent<TextMessageContent> deleteHistory1WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteCommand.CMD_PREFIX + " wrong_date DH DM");
		MessageEvent<TextMessageContent> deleteHistor2WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteCommand.CMD_PREFIX + " "
		                + WarDeathLogic.SDF.format(new Date()));
		MessageEvent<TextMessageContent> deleteHistor3WarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteCommand.CMD_PREFIX + " "
		                + WarDeathLogic.SDF.format(new Date()) + " 4Loki");

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
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().contains("Invalid argument"));

		response = friday.handleTextMessageEvent(summoner2NodeCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().contains("Invalid summoner at position 10"));

		response = friday.handleTextMessageEvent(summoner3NodeCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().contains("Invalid argument"));

		response = friday.handleTextMessageEvent(summoner4NodeCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().contains("Invalid node number"));

		response = friday.handleTextMessageEvent(summoner5NodeCmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summoner6NodeCmd);
		assertTrue(response.getText().contains("Missing arguments"));
		assertTrue(callback.takeAllMessages().isEmpty());

		// Multi-insert. We need to add valid summoners first
		response = friday.handleTextMessageEvent(summonersAddCmd);
		assertTrue(response.getText().contains("Added 4 new summoner"));
		assertFalse(response.getText().contains("Tony 88"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerNodeMulti1Cmd);
		String pushedMessages = callback.takeAllMessages();
		assertFalse(pushedMessages.isEmpty());
		assertTrue(pushedMessages.contains("2/5"));
		assertTrue(pushedMessages.contains("2Z"));
		assertTrue(pushedMessages.contains("position 6"));
		assertTrue(pushedMessages.contains("Missing args for"));
		assertFalse(response.getText().contains("A. 5* dupe IMIW (55)"));
		assertTrue(response.getText().contains("John Doe"));
		assertTrue(response.getText().contains("Tony 88"));
		assertFalse(response.getText().contains("slux83"));
		assertTrue(response.getText().contains("A. 5* Elektra (11)"));
		assertTrue(response.getText().contains("C. Thor (58)"));
		assertFalse(response.getText().contains("C. NC (53)"));

		response = friday.handleTextMessageEvent(summonerRename1Cmd);
		assertTrue(response.getText().contains("Invalid summoner at position 20"));
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

		response = friday.handleTextMessageEvent(resetCmd);
		assertTrue(response.getText().contains("War reports cleared"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersAddAllCmd);
		assertTrue(response.getText().contains("Added 5 new summoner(s)"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerNodeMultiWithInvalidMixedCmd);
		assertTrue(response.getText().contains("duped King Groot"));
		assertTrue(callback.takeAllMessages().contains("Missing args for"));

	}

	@Test
	public void testResponseTooBig() throws Exception {
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

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		for (int i = 1; i <= 55; i++) {
			MessageEvent<TextMessageContent> deathCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
			        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 1 " + i
			                + " 5* duped Dormammu hohohoho hohohoho hohohohoho" + i);

			response = friday.handleTextMessageEvent(deathCmd);
			assertTrue(response.getText().contains("Total deaths: " + i));
			assertTrue(callback.takeAllMessages().isEmpty());
		}

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummaryDeathCommand.CMD_PREFIX);

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertNull(response);
		String pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.length() > 2000);
		assertTrue(pushedMessages.contains("Total deaths: 55"));

	}

	@Test
	public void testStartupUnderMaintenance() throws Exception {
		System.setProperty(FridayBotApplication.FRIDAY_MAINTENANCE_KEY, "true");
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// We clear it for the rest of the tests
		System.clearProperty(FridayBotApplication.FRIDAY_MAINTENANCE_KEY);

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

		MessageEvent<TextMessageContent> invalidCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        "Hello guys");
		MessageEvent<TextMessageContent> closeToSomeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " register");

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertFalse(response.getText().contains("successfully registered using the name group1"));
		assertTrue(response.getText().contains("standby"));
		assertTrue(response.getText().contains("maintenance"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(invalidCmd);
		assertNull(response);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(closeToSomeCmd);
		assertNotNull(response);
		assertTrue(response.getText().contains("maintenance"));
		assertTrue(callback.takeAllMessages().isEmpty());
		System.out.println(response.getText());
	}

	@Test
	public void testNominalWarDeathReportingWorkflowCommands() throws Exception {
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

		// Invalid commands
		MessageEvent<TextMessageContent> userCloseCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " dea");
		MessageEvent<TextMessageContent> userInvalid = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        "Hello fellas");

		// Report death command
		MessageEvent<TextMessageContent> death1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 2 55 5* dupe Dormammu");
		MessageEvent<TextMessageContent> death2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 1 24 6* NC");
		MessageEvent<TextMessageContent> death3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 4 28 5* dupe KP");

		// Summoner placement command
		MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
		                + " slux83, John Doe, Nemesis The Best, Tony 88");
		MessageEvent<TextMessageContent> summonersPrintCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX);

		// Summoner node
		MessageEvent<TextMessageContent> summoner1NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3a 55 5* dupe IMIW");
		MessageEvent<TextMessageContent> summoner2NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 1E 22 4* dupe Mephisto");
		MessageEvent<TextMessageContent> summoner3NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3B 12 5* undupe Sentinel");
		MessageEvent<TextMessageContent> summoner3BisNodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3b 12 5* duped Ronan"); // replace
		MessageEvent<TextMessageContent> summoner4DuplicatedNodeCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3c 12 5* duped Ronan"); // duplicated
		                                                                                                             // node
		MessageEvent<TextMessageContent> summonerClear1NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 3B 0 5* duped Ronan"); // clear
		MessageEvent<TextMessageContent> summonerClear2NodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " 1E -1 4* dupe Mephisto"); // clear
		MessageEvent<TextMessageContent> summonerNodeMulti1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX
		                + " 2A 11 5* Elektra,   2B   31  4* domino,4C 53 NC");

		// Summoner rename
		MessageEvent<TextMessageContent> summonerRenameCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerRenameCommand.CMD_PREFIX + " 3 Foo Bar 1");

		// Death summary command
		MessageEvent<TextMessageContent> deathSummaryCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummaryDeathCommand.CMD_PREFIX);

		// Death summary compact command
		MessageEvent<TextMessageContent> deathSummaryCompactCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummaryDeathCommand.CMD_PREFIX + " compact");

		// Undo death command
		MessageEvent<TextMessageContent> undoDeathCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarUndoDeathCommand.CMD_PREFIX);

		// Delete node command
		MessageEvent<TextMessageContent> deleteNodeCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteNodeCommand.CMD_PREFIX + " 55");

		MessageEvent<TextMessageContent> deleteNodeInvalid1Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteNodeCommand.CMD_PREFIX);

		MessageEvent<TextMessageContent> deleteNodeInvalid2Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteNodeCommand.CMD_PREFIX + " wrong");

		MessageEvent<TextMessageContent> deleteNodeInvalid3Cmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteNodeCommand.CMD_PREFIX + " 155");

		// Save war command
		MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarSaveCommand.CMD_PREFIX + " DH DM");

		// Reset war command
		MessageEvent<TextMessageContent> resetWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarResetCommand.CMD_PREFIX);

		// All history command
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX);

		// Specific history command
		MessageEvent<TextMessageContent> specificHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX + " "
		                + WarDeathLogic.SDF.format(new Date()));

		// Specific history command CVS export
		MessageEvent<TextMessageContent> specificHistoryWarExportCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX + " "
		                + WarHistoryCommand.CMD_ARG_EXPORT + " " + WarDeathLogic.SDF.format(new Date()));

		// Delete history command
		MessageEvent<TextMessageContent> deleteHistoryWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDeleteCommand.CMD_PREFIX + " "
		                + WarDeathLogic.SDF.format(new Date()) + " DH DM");

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

		response = friday.handleTextMessageEvent(specificHistoryWarExportCmd);
		history = callback.takeAllMessages();
		assertTrue(history.contains("Exporting data..."));
		assertTrue(response.getText().contains("None"));

		response = friday.handleTextMessageEvent(death1Cmd);
		response = friday.handleTextMessageEvent(death2Cmd);
		response = friday.handleTextMessageEvent(death3Cmd);
		assertTrue(response.getText().contains("480"));
		assertTrue(response.getText().contains("7"));
		assertTrue(response.getText().contains("3/55"));

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertTrue(response.getText().contains("5* dupe KP"));
		assertTrue(callback.takeAllMessages().isEmpty());
		assertTrue(response.getText().contains("3/55"));

		response = friday.handleTextMessageEvent(deathSummaryCompactCmd);
		assertTrue(response.getText().contains("5* dupe KP"));
		assertTrue(callback.takeAllMessages().isEmpty());
		assertTrue(response.getText().contains("3/55"));
		assertTrue(response.getText().contains("[4]"));
		assertTrue(response.getText().contains("55. 5* dupe"));

		response = friday.handleTextMessageEvent(undoDeathCmd);
		assertTrue(response.getText().contains("WAR DEATH REPORT"));
		assertTrue(response.getText().contains("2/55"));

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertFalse(response.getText().contains("5* dupe KP"));
		assertTrue(callback.takeAllMessages().isEmpty());
		assertTrue(response.getText().contains("2/55"));

		response = friday.handleTextMessageEvent(summonersPrintCmd);
		assertTrue(response.getText().contains("Nothing to report"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersAddCmd);
		assertTrue(response.getText().contains("Added 4 new summoner"));
		assertFalse(response.getText().contains("Tony 88"));
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

		response = friday.handleTextMessageEvent(summoner4DuplicatedNodeCmd);
		assertFalse(response.getText().contains("C. 5* duped Ronan"));
		String callbackMessages = callback.takeAllMessages();
		assertFalse(callbackMessages.isEmpty());
		assertTrue(callbackMessages.contains("12 has been already reported"));

		response = friday.handleTextMessageEvent(summonerClear1NodeCmd);
		assertFalse(response.getText().contains("B. 5* duped Ronan"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerClear2NodeCmd);
		assertFalse(response.getText().contains("E. 4* dupe Mephisto"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerNodeMulti1Cmd);
		assertFalse(response.getText().contains("A. 5* dupe IMIW (55)"));
		assertTrue(response.getText().contains("John Doe"));
		assertTrue(response.getText().contains("Tony 88"));
		assertFalse(response.getText().contains("slux83"));
		assertTrue(response.getText().contains("A. 5* Elektra (11)"));
		assertTrue(response.getText().contains("B. 4* domino (31)"));
		assertTrue(response.getText().contains("C. NC (53)"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonersPrintCmd);
		assertTrue(response.getText().contains("Reported Nodes: 4/55"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(summonerRenameCmd);
		assertTrue(response.getText().contains("Summoner renamed"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteNodeCmd);
		assertTrue(response.getText().contains("Deleted 1"));
		assertTrue(response.getText().contains("node 55"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteNodeInvalid1Cmd);
		assertFalse(response.getText().contains("Deleted 1"));
		assertTrue(response.getText().contains("Missing argument"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteNodeInvalid2Cmd);
		assertFalse(response.getText().contains("Deleted 1"));
		assertTrue(response.getText().contains("Expected integer value for <node> but got 'wrong'"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(deleteNodeInvalid3Cmd);
		assertFalse(response.getText().contains("Deleted 1"));
		assertTrue(response.getText().contains("Deleted 0 entry"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertFalse(response.getText(), response.getText().contains("war DH DM"));
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
		assertTrue(history.contains("Reported nodes: 1/55")); // deaths report
		assertTrue(history.contains("Reported Nodes: 4/55")); // summoners
		                                                      // report

		response = friday.handleTextMessageEvent(deleteHistoryWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertTrue(response.getText().contains(WarDeathLogic.SDF.format(new Date())));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userCloseCmd);
		assertTrue(response.getText().contains("perhaps"));
		assertTrue(
		        response.getText().contains(AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(userInvalid);
		assertNull(response);
		assertTrue(callback.takeAllMessages().isEmpty());

		// Add almost all the death reports
		for (int i = 52, j = 1; i > 0; i--) {
			MessageEvent<TextMessageContent> addDeathCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
			        userId + "_" + j, AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 0 "
			                + i + " Some Champ very long description blablabla " + i);

			friday.handleTextMessageEvent(addDeathCmd);

			if ((i % 5) == 0)
				j++;
		}

		response = friday.handleTextMessageEvent(deathSummaryCmd);
		assertNull(response);
		callbackMessages = callback.takeAllMessages();
		assertTrue(callbackMessages.contains("Nodes to report: [53, 54, 55]"));

		// Add summoners
		MessageEvent<TextMessageContent> summonersAddAllCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
		                + " summoner1, summoner2, summoner3, summoner4, summoner5, summoner6, summoner7, summoner8, summoner9, summoner10");
		MessageEvent<TextMessageContent> summonersReadAllCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX);
		response = friday.handleTextMessageEvent(summonersAddAllCmd);
		assertEquals("Added 10 new summoner(s)", response.getText());
		assertTrue(callback.takeAllMessages().isEmpty());

		// Report all the placement for each summoner
		int node = 1;
		for (int k = 1; k <= 10; k++) {
			char ending = 'E';
			if (k >= 8)
				ending = 'D';
			for (char pos = 'A'; pos <= ending; pos++) {
				MessageEvent<TextMessageContent> summonerNodeCmd = MessageEventUtil.createMessageEventGroupSource(
				        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX + " "
				                + k + Character.toString(pos) + " " + node + " 5* dupe Medusa");
				response = friday.handleTextMessageEvent(summonerNodeCmd);
				assertTrue(response.getText().contains(Character.toString(pos) + ". 5* dupe Medusa (" + node + ")"));
				assertTrue(callback.takeAllMessages().isEmpty());

				node++;
			}
		}

		response = friday.handleTextMessageEvent(summonersReadAllCmd);
		assertTrue(response.getText().contains("Reported Nodes: 47/55"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(saveWarCmd);
		assertTrue(response.getText().contains("DH DM"));
		assertFalse(response.getText(), response.getText().contains("war DH DM"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(specificHistoryWarExportCmd);
		history = callback.takeAllMessages();
		assertTrue(history.contains("Exporting data..."));
		assertTrue(response.getText().contains("https://pastebin.com"));
		System.out.println(response.getText());
	}

	/**
	 * Tests the behavior of the bot when it goes out and in from a group
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGroupInOutScenario() throws Exception {
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
		LeaveEvent leaveEvent = MessageEventUtil.createLeaveEvent(groupId, userId);

		// History command to test the real exit of the bot from the group
		MessageEvent<TextMessageContent> historyWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarHistoryCommand.CMD_PREFIX);

		/* Begin */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("No records found"));
		assertTrue(callback.takeAllMessages().isEmpty());

		friday.handleDefaultMessageEvent(leaveEvent);
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("This group is unregistered"));
		assertTrue(callback.takeAllMessages().isEmpty());

		response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(historyWarCmd);
		assertTrue(response.getText().contains("No records found"));
		assertTrue(callback.takeAllMessages().isEmpty());
	}

	@Test
	public void testTextTooLong() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		String longName = Strings.repeat("long", 100);

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " " + longName);

		/* Begin */
		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("Data too long"));

	}

	@Test
	public void testGroupSchedulerCommand() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// User help
		MessageEvent<TextMessageContent> eventUserHelp = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

		// Today's events
		MessageEvent<TextMessageContent> eventTodayEvents = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX);

		// Tomorrow's events
		MessageEvent<TextMessageContent> eventTomorrowEvents = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " tomoRRow");

		// Week events
		MessageEvent<TextMessageContent> eventWeekEvents = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " weeK");
		// Wrong events
		MessageEvent<TextMessageContent> eventWrongEvents = MessageEventUtil.createMessageEventGroupSource(groupId,
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + EventInfoCommand.CMD_PREFIX + " Monthly");

		TextMessage response = friday.handleTextMessageEvent(eventUserHelp);
		assertNotNull(response);
		String help = response.getText();
		assertTrue(help.contains(EventInfoCommand.CMD_PREFIX));
		assertTrue(help.contains(HelpCommand.CMD_PREFIX));
		assertTrue(help.contains(WarAddSummonersCommand.CMD_PREFIX));
		assertFalse(help.isEmpty());
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
}
