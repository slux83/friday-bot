/**
 * 
 */
package de.slux.line.friday.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupFeature;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * @author slux
 *
 */
public class LinePushJob implements Job {
	private static final long MAX_MISFIRE_DELAY_MS = 60 * 1000;
	private static Logger LOG = LoggerFactory.getLogger(LinePushJob.class);

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {

		String eventId = context.getJobDetail().getJobDataMap().get(EventScheduler.ID_KEY).toString();
		String message = context.getJobDetail().getJobDataMap().get(EventScheduler.MESSAGE_KEY).toString();

		Date nextTask = context.getTrigger().getNextFireTime();

		LOG.info("EVENT-ID=" + eventId + " ST=" + context.getScheduledFireTime() + " NOW=" + new Date()
		        + " Executing event message: " + message + " NEXT=" + nextTask);

		// Misfire handler
		Date startTime = context.getTrigger().getStartTime();
		Date now = new Date();

		if (Math.abs(startTime.getTime() - now.getTime()) > MAX_MISFIRE_DELAY_MS) {
			LOG.warn("MISFIRE. Job " + eventId + " discarded due to misfire. trigger.startTime=[" + startTime
			        + "] now=[" + now + "]");
			return;
		}

		try {
			WarDeathLogic logic = new WarDeathLogic();
			Map<String, WarGroup> groups = logic.getAllGroups();
			groups.entrySet().removeIf(e -> e.getValue().getGroupFeature().equals(GroupFeature.GroupFeatureWar));

			// Send the message to all groups who have registered
			for (Entry<String, WarGroup> group : groups.entrySet()) {

				try {

					PushMessage pushMessage = new PushMessage(group.getKey(), new TextMessage(message));
					// TODO: this must be more efficient (add friday status
					// statistics about the last 5 pushes)
					FridayBotApplication.getInstance().getLineMessagingClient().pushMessage(pushMessage);
				} catch (Exception e) {
					LOG.warn("Cannot push message to group " + group.getKey() + "(" + group.getValue() + ")"
					        + ". Reason: " + e, e);
				}
			}
		} catch (Exception e) {

		}

	}

}
