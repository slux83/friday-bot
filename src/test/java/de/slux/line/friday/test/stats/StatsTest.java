package de.slux.line.friday.test.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.WarStatsCommand;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.logic.StatsLogic;
import de.slux.line.friday.scheduler.WarStatsJob;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import de.slux.line.friday.test.util.scheduler.ContextDummy;

/**
 * @author Slux
 */
public class StatsTest {

	/**
	 * Reduce logging level and switch DB
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);

		// DbConnectionPool.DB_NAME = "friday_test";
	}

	/**
	 * Switch DB
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void afterClass() throws Exception {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);

		DbConnectionPool.DB_NAME = "friday";
	}

	@Test
	public void testStatisticsCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Node stats command valid (group)
		MessageEvent<TextMessageContent> nodeStatsValidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " 44");

		// Node stats command valid (user)
		MessageEvent<TextMessageContent> nodeStatsValidUserCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " 44");

		// Node stats command invalid (group)
		MessageEvent<TextMessageContent> nodeStatsInvalidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " 144");

		// Node stats command invalid (user)
		MessageEvent<TextMessageContent> nodeStatsInvalidUserCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " 144");

		// Champ stats command valid (group)
		MessageEvent<TextMessageContent> champStatsValidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " dusa");

		// Champ stats command valid (user)
		MessageEvent<TextMessageContent> champStatsValidUserCmd = MessageEventUtil.createMessageEventUserSource(userId,
		        AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " medusa1");

		// Champ stats command invalid (group)
		MessageEvent<TextMessageContent> champStatsInvalidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " FAKE NOT VALID");

		// Champ stats command invalid (user)
		MessageEvent<TextMessageContent> champStatsInvalidUserCmd = MessageEventUtil.createMessageEventUserSource(
		        userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " FAKE NOT VALID");

		// Node stats tests
		TextMessage response = friday.handleTextMessageEvent(nodeStatsValidGroupCmd);
		String pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		System.out.println(response.getText());
		assertTrue(response.getText().contains("Node 44"));
		assertTrue(response.getText().contains("Mortality"));

		response = friday.handleTextMessageEvent(nodeStatsValidUserCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertTrue(response.getText().contains("Node 44"));
		assertTrue(response.getText().contains("Mortality"));

		response = friday.handleTextMessageEvent(nodeStatsInvalidGroupCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertFalse(response.getText().contains("Node 144"));
		assertTrue(response.getText().contains("Cannot find any War Statistics for node 144"));

		response = friday.handleTextMessageEvent(nodeStatsInvalidUserCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertFalse(response.getText().contains("Node 144"));
		assertTrue(response.getText().contains("Cannot find any War Statistics for node 144"));

		// Champ stats tests
		response = friday.handleTextMessageEvent(champStatsValidGroupCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertTrue(response.getText().contains("Medusa"));
		assertTrue(response.getText().contains("Mortality"));

		response = friday.handleTextMessageEvent(champStatsValidUserCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertTrue(response.getText().contains("Medusa"));
		assertTrue(response.getText().contains("Mortality"));

		response = friday.handleTextMessageEvent(champStatsInvalidGroupCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertFalse(response.getText().contains("Medusa"));
		assertTrue(response.getText().contains("FAKE NOT VALID"));

		response = friday.handleTextMessageEvent(champStatsInvalidUserCmd);
		pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertFalse(response.getText().contains("Medusa"));
		assertTrue(response.getText().contains("FAKE NOT VALID"));

	}

	@Ignore
	public void testStatisticsTooQuickCommands() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		String groupId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Node stats command valid (group)
		MessageEvent<TextMessageContent> nodeStatsValidGroupCmd = MessageEventUtil.createMessageEventGroupSource(
		        groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarStatsCommand.CMD_PREFIX + " 44");

		TextMessage response = friday.handleTextMessageEvent(nodeStatsValidGroupCmd);
		String pushedMessages = callback.takeAllMessages();
		assertTrue(pushedMessages.isEmpty());
		assertEquals("Sorry, statistics are not available yet. Please try later", response.getText());

	}

	@Test
	public void testSchedulerJobUpdateStatistics() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		PostConstructHolder.waitForPostConstruct(callback);

		WarStatsJob job = new WarStatsJob();
		job.execute(new ContextDummy(false));
		String pushedMessage = callback.takeAllMessages();
		System.out.println(pushedMessage);
		assertTrue(pushedMessage.contains("War node stats updated"));

		StatsLogic logic = new StatsLogic(false);
		String stats = logic.getNodeStats(FridayBotApplication.getInstance().getWarNodeStatistics(), 44);
		System.out.println(stats);
		Assert.assertTrue(stats.contains("Node 44"));
		Assert.assertTrue(stats.contains("Mortality"));

		stats = logic.getNodeStats(FridayBotApplication.getInstance().getWarNodeStatistics(), 144);
		System.out.println(stats);
		Assert.assertTrue(stats.contains("Cannot find any War Statistics for node 144"));
	}

}
