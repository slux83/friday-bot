package de.slux.line.friday.test.scheduler;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.RegisterEventsCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;

/**
 * @author slux
 */
public class TestScheduler {

	@Test
	public void testSchedulerJobs() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		Thread.sleep(15000);

		Scheduler scheduler = friday.getEventScheduler().getScheduler();
		boolean masterJobFound = false;
		for (String groupName : scheduler.getJobGroupNames()) {

			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();

				// get job's trigger
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
				Date nextFireTime = triggers.get(0).getNextFireTime();

				System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);

				if (jobName.contains("mcoc schedule master")) {
					masterJobFound = true;
				}
			}
		}

		assertTrue(masterJobFound);
	}

	@Test
	public void testRegisterForEvents() throws Exception {
		MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
		FridayBotApplication friday = new FridayBotApplication(null);
		friday.setLineMessagingClient(new LineMessagingClientMock(callback));
		friday.postConstruct();

		String group1Id = UUID.randomUUID().toString();
		String group2Id = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		// Register command new group
		MessageEvent<TextMessageContent> registerNewCmd = MessageEventUtil.createMessageEventGroupSource(group1Id,
		        userId, RegisterEventsCommand.CMD_PREFIX);

		// Register command events with an already existing registration for war
		MessageEvent<TextMessageContent> registerWarCmd = MessageEventUtil.createMessageEventGroupSource(group2Id,
		        userId, WarRegisterCommand.CMD_PREFIX + " group1");
		MessageEvent<TextMessageContent> registerExistingWithWarCmd = MessageEventUtil
		        .createMessageEventGroupSource(group2Id, userId, RegisterEventsCommand.CMD_PREFIX);

		TextMessage response = friday.handleTextMessageEvent(registerNewCmd);
		assertTrue(response.getText().contains("MCoC event notifications"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(registerWarCmd);
		assertTrue(response.getText().contains("successfully registered using the name group1"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(registerExistingWithWarCmd);
		assertTrue(response.getText().contains("MCoC event notifications"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
		response = friday.handleTextMessageEvent(registerExistingWithWarCmd);
		assertTrue(response.getText().contains("already registered"));
		assertTrue(callback.takeAllMessages().isEmpty());
		
	}
}
