package de.slux.line.friday.test.stats;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.logic.StatsLogic;
import de.slux.line.friday.scheduler.WarStatsJob;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.scheduler.ContextDummy;

/**
 * @author Slux
 */
public class StatsTest {
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

	@Ignore
	public void testStatistics() throws Exception {
		StatsLogic logic = new StatsLogic();
		Map<Integer, List<HistoryStats>> nodeStats = logic.updateNodeStats();
		String stats = logic.getNodeStats(nodeStats, 1);
		Assert.assertTrue(stats.contains("Node 1"));
		Assert.assertTrue(stats.contains("Mortality"));
	}

	@Test
	public void testSchedulerJobUpdateStatistics() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		WarStatsJob job = new WarStatsJob();
		job.execute(new ContextDummy(false));
		String pushedMessage = callback.takeAllMessages();
		System.out.println(pushedMessage);
		assertTrue(pushedMessage.contains("War node stats updated"));

		StatsLogic logic = new StatsLogic();
		String stats = logic.getNodeStats(FridayBotApplication.getInstance().getWarNodeStatistics(), 44);
		System.out.println(stats);
		Assert.assertTrue(stats.contains("Node 44"));
		Assert.assertTrue(stats.contains("Mortality"));
		
		stats = logic.getNodeStats(FridayBotApplication.getInstance().getWarNodeStatistics(), 144);
		System.out.println(stats);
		Assert.assertTrue(stats.contains("Cannot find any War Statistics for node 144"));
	}

}
