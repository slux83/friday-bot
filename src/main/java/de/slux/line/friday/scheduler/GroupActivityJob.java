/**
 * 
 */
package de.slux.line.friday.scheduler;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.response.BotApiResponse;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * Job that periodically checks the groups activity
 * 
 * @author slux
 */
public class GroupActivityJob implements Job {
	private static Logger LOG = LoggerFactory.getLogger(GroupActivityJob.class);

	/** 5 days => warning */
	public static final long WARNING_INACTIVITY_MS = 5 * 24 * 60 * 60 * 1000l;

	/** 6+ days => leave group */
	public static final long LEAVE_INACTIVITY_MS = 6 * 24 * 60 * 60 * 1000l;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String eventId = context.getJobDetail().getJobDataMap().get(EventScheduler.ID_KEY).toString();

		LOG.info("Updating group activities: " + eventId);
		Date now = new Date();

		WarDeathLogic logic = new WarDeathLogic();
		try {
			logic.updateGroupsActivity(FridayBotApplication.getInstance().getGroupActivities());
			FridayBotApplication.getInstance().getGroupActivities().clear();

			Map<String, WarGroup> allGroups = logic.getAllGroups();

			// Remove the already inactive groups
			allGroups.entrySet().removeIf(e -> e.getValue().getGroupStatus().equals(GroupStatus.GroupStatusInactive));

			Set<WarGroup> warning = new HashSet<>();
			Set<WarGroup> leave = new HashSet<>();
			for (WarGroup group : allGroups.values()) {
				Date lastActivity = group.getLastActivity();
				long timeDiff = Math.abs(now.getTime() - lastActivity.getTime());

				if (timeDiff >= WARNING_INACTIVITY_MS && timeDiff < LEAVE_INACTIVITY_MS) {
					warning.add(group);
				}

				if (timeDiff >= LEAVE_INACTIVITY_MS) {
					leave.add(group);
				}
			}

			LOG.info("Groups to warn: " + warning.size());
			LOG.info("Groups to leave: " + leave.size());

			FridayBotApplication.getInstance().pushMultiMessages(warning,
			        "Hello, I have detected a 5+ days of inactivity in this group.\nI will leave tomorrow at this time if no one writes on this group chat.");

			FridayBotApplication.getInstance().pushMultiMessages(leave,
			        "Hello, I have detected a 6+ days of inactivity in this group.\nI will leave for now. Bye!");
			int leftGroups = 0;
			for (WarGroup group : leave) {
				CompletableFuture<BotApiResponse> response = FridayBotApplication.getInstance().getLineMessagingClient()
				        .leaveGroup(group.getGroupId());
				try {
					response.get();
					LeaveEvent leaveEvent = new LeaveEvent(new GroupSource(group.getGroupId(), null), now.toInstant());
					FridayBotApplication.getInstance().handleDefaultMessageEvent(leaveEvent);
					leftGroups++;
				} catch (Exception e) {
					LOG.error("Failed leaving the group " + group.getGroupId() + ": " + e, e);
				}
			}

			String execTime = String.format("%.2f", (Math.abs(System.currentTimeMillis() - now.getTime()) / 1000.0));
			StringBuilder sb = new StringBuilder("Groups Inactivity Report: ");
			sb.append("\nExecution time (sec): " + execTime);
			sb.append("\nTotal Groups: " + allGroups.size());
			sb.append("\nWarned: " + warning.size());
			sb.append("\nTo leave: " + leave.size());
			sb.append("\nActual left: " + leftGroups);

			notifyAdmin(sb.toString(), false);

		} catch (Exception e) {
			LOG.error("Can't update group activities: " + e, e);
			notifyAdmin("Can't update war stats: " + e, true);
		}

	}

	/**
	 * Push to the admin
	 * 
	 * @param message
	 * @param isError
	 */
	private void notifyAdmin(String message, boolean isError) {
		String error = "";
		if (isError)
			error = "ERROR: ";
		FridayBotApplication.getInstance().pushMessageToAdmin(error + message);

	}

}
