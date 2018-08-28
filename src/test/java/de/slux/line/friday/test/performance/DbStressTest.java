/**
 * 
 */
package de.slux.line.friday.test.performance;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.command.war.WarReportDeathCommand;
import de.slux.line.friday.command.war.WarResetCommand;
import de.slux.line.friday.command.war.WarSaveCommand;
import de.slux.line.friday.command.war.WarSummonerNodeCommand;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;

/**
 * Testing the performances with a huge DB
 * 
 * @author Slux
 */
public class DbStressTest {

	/**
	 * Reduce logging level
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
	}

	/**
	 * Disable this test as default and make sure you use a dedicated test DB
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHugeDbPerformances() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		int totalGroups = 500 * 3; // X alliances with 3 BGs each
		int totalWars = 20; // X wars total for each group
		String userId = UUID.randomUUID().toString();
		
		for (int i = 1; i <= totalGroups; i++) {
			long begin = System.currentTimeMillis();
			String groupId = UUID.randomUUID().toString();

			// Register command
			MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
			        userId, WarRegisterCommand.CMD_PREFIX + " group" + i);
			TextMessage response = friday.handleTextMessageEvent(registerCmd);
			assertTrue(response.getText().contains("successfully registered using the name group" + i));

			// For all the wars
			for (int j = 0; j < totalWars; j++) {

				// Add 55 kill reports
				for (int z = 1; z <= 55; z++) {
					MessageEvent<TextMessageContent> deathCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
					        userId, WarReportDeathCommand.CMD_PREFIX + " 1 " + z + " 5* duped Dormammu" + z);

					response = friday.handleTextMessageEvent(deathCmd);
					assertTrue(response.getText().contains("Total deaths: " + z));
					assertTrue(callback.takeAllMessages().isEmpty());
				}

				// Add summoners
				MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil
				        .createMessageEventGroupSource(groupId, userId, WarAddSummonersCommand.CMD_PREFIX
				                + " summoner1, summoner2, summoner3, summoner4, summoner5, summoner6, summoner7, summoner8, summoner9, summoner10");
				response = friday.handleTextMessageEvent(summonersAddCmd);
				assertTrue(response.getText().contains("summoner1"));
				assertTrue(response.getText().contains("summoner10"));
				assertTrue(callback.takeAllMessages().isEmpty());

				// Report all the placement for each summoner
				int node = 1;
				for (int k = 1; k <= 10; k++) {
					for (char pos = 'A'; pos <= 'E'; pos++) {
						MessageEvent<TextMessageContent> summonerNodeCmd = MessageEventUtil
						        .createMessageEventGroupSource(groupId, userId, WarSummonerNodeCommand.CMD_PREFIX + " "
						                + k + Character.toString(pos) + " " + node + " 5* dupe Medusa");
						response = friday.handleTextMessageEvent(summonerNodeCmd);
						assertTrue(response.getText()
						        .contains(Character.toString(pos) + ". 5* dupe Medusa (" + node + ")"));
						assertTrue(callback.takeAllMessages().isEmpty());

						node++;
					}
				}

				// Save war command
				String allyTag = "ALLY_" + Integer.toString(i) + "_" + Integer.toString(j);
				MessageEvent<TextMessageContent> saveWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
				        userId, WarSaveCommand.CMD_PREFIX + " " + allyTag);

				response = friday.handleTextMessageEvent(saveWarCmd);
				assertTrue(response.getText().contains(allyTag));
				assertTrue(callback.takeAllMessages().isEmpty());

				// Reset war command
				MessageEvent<TextMessageContent> resetWarCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
				        userId, WarResetCommand.CMD_PREFIX);

				response = friday.handleTextMessageEvent(resetWarCmd);
				assertTrue(response.getText().contains("War reports cleared"));
				assertTrue(callback.takeAllMessages().isEmpty());
				
			}
			
			long timeDiffMs = Math.abs(System.currentTimeMillis() - begin);
			System.err.println("Processed group N." + i + " in " + (timeDiffMs/1000.0) + " sec(s)");
		}

	}
}
