/**
 * 
 */
package de.slux.line.friday.test.issues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.EventInfoCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarDiversityCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.command.war.WarReportDeathCommand;
import de.slux.line.friday.scheduler.McocSchedulerImporter;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;

/**
 * @author slux
 *         <p>
 *         Testing
 *         <a href="https://github.com/slux83/friday-bot/issues/33">Issue
 *         #33</a>
 *         </p>
 */
public class TestIssue33 {
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
	public void testIssue33() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);
		String userId = UUID.randomUUID().toString();
		String groupId = UUID.randomUUID().toString();

		// Register command
		MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

		// Diversity command verbose
		MessageEvent<TextMessageContent> diversityCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarDiversityCommand.CMD_PREFIX + " " + WarDiversityCommand.ARG);

		// User help
		MessageEvent<TextMessageContent> eventUserHelp = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX + " 0 5  sabretooth 5*  ,\n"
		                + " 0 32  modok 5* dub  ,\n" + " 0 39  magik 5* dub  ,\n" + " 0 43  mr sinister 5* dub  ,\n"
		                + " 0 1  5* Hulk dupe  ,\n" + " 0 19  6* Guillotine  ,\n" + " 0 25  5* jugger dupe  ,\n"
		                + " 0 46  5* Hyperion  ,\n" + " 0 49  6* Spider Morales  ,\n" + " 0 50  5* King Groot dupe  ,\n"
		                + " 0 53  5* Darkhawk dupe  ,\n" + " 0 3  6* yondu  ,\n" + " 0 21  5* Morningstar dupe  ,\n"
		                + " 0 36  5* Void  ,\n" + " 0 15  6*elektra  ,\n" + " 0 33  6* Thor Foster  ,\n"
		                + " 0 40  6* red skull  ,\n" + " 0 44  6* IM IW  ,\n" + " 0 55  5*wasp dupe  ,\n"
		                + " 0 12  6* proxima midnight  ,\n" + " 0 18  6* night crawler  ,\n" + " 0 24  5* korg  ,\n"
		                + " 0 31  6* rocket raccoon  ,\n" + " 0 38  5* mephisto  ,\n" + " 0 42  5* taskmaster  ,\n"
		                + " 0 52  6* green goblin  ,\n" + " 0 10  VenomDuck  ,\n" + " 0 16  Venom  ,\n"
		                + " 0 23  5* Abo  ,\n" + " 0 29  Killmonger  ,\n" + " 0 36  Void  ,\n" + " 0 51  Spidy  ,\n"
		                + " 0 54  Medusa (Boss)  ,\n" + " 0 4  6*sentry  ,\n" + " 0 5  sabretooth 5*  ,\n"
		                + " 0 7  5*strange  ,\n" + " 0 11  6*punisher 2099  ,\n" + " 0 13  5*karnak  ,\n"
		                + " 0 14  5*angela  ,\n" + " 0 17  6*yellow jacket  ,\n" + " 0 22  5*domino dup  ,\n"
		                + " 0 28  5*Champion dupe  ,\n" + " 0 34  5*symbiont supreme dupe  ,\n"
		                + " 0 26  5* IMIW dupe  ,\n" + " 0 27  5* Corvus dupe  ,\n" + " 0 41  6* Beast  ,\n"
		                + " 0 45  5* Ghost  ,\n" + " 0 47  6* Sentinel  ,\n" + " 0 4  Sentry  ,\n"
		                + " 0 7  Strange  ,\n" + " 0 13  Karnak  ,\n" + " 0 14  Angela  ,\n" + " 0 30  ThorR  ,\n"
		                + " 0 35  Dormammu  ,\n" + " 0 37  Gulk");

		TextMessage response = friday.handleTextMessageEvent(registerCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));

		response = friday.handleTextMessageEvent(eventUserHelp);
		assertTrue(response.getText().contains("Reported nodes: 49/55"));

		response = friday.handleTextMessageEvent(diversityCmd);
		System.err.println(response.getText());

	}

}
