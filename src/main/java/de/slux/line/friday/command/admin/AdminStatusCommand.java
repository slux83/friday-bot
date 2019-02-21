/**
 * 
 */
package de.slux.line.friday.command.admin;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class AdminStatusCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "status";
	private static Logger LOG = LoggerFactory.getLogger(AdminStatusCommand.class);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public AdminStatusCommand(LineMessagingClient messagingClient) {
		super(messagingClient);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.friday.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		StringBuilder sb = new StringBuilder("*** F.R.I.D.A.Y. MCOC Line Bot Status ***\n\n");

		// Check if we need to change the status
		List<String> args = super.extractArgs(message);
		if (args.size() > 2) {
			String newStatus = args.get(2).trim();

			if (newStatus.equalsIgnoreCase("operational")) {
				FridayBotApplication.getInstance().getIsOperational().set(true);
			} else if (newStatus.equalsIgnoreCase("maintenance")) {
				FridayBotApplication.getInstance().getIsOperational().set(false);
			} else {
				sb.append("WARNING: submitted unknown status ");
				sb.append(newStatus);
				sb.append("\n");
			}
		}

		sb.append("Version: ");
		sb.append(FridayBotApplication.FRIDAY_VERSION);
		sb.append("\n");
		sb.append("Status: ");
		sb.append(FridayBotApplication.getInstance().getIsOperational().get() ? "OPERATIONAL" : "MAINTENANCE");
		sb.append("\n");
		long msgCmdCounter = FridayBotApplication.getInstance().getCommandIncomingMsgCounter().get();
		long msgTotalCounter = FridayBotApplication.getInstance().getTotalIncomingMsgCounter().get();
		long startupMs = FridayBotApplication.getInstance().getStartup().getTime();
		long nowMs = System.currentTimeMillis();
		long msDiff = Math.abs(nowMs - startupMs);
		double msgCmdSec = (msgCmdCounter / (msDiff / 1000.0));
		double msgTotalSec = (msgTotalCounter / (msDiff / 1000.0));
		sb.append("Cmd/Tot messages: ");
		sb.append(Long.toString(msgCmdCounter));
		sb.append("/");
		sb.append(Long.toString(msgTotalCounter));
		sb.append("\n");
		sb.append("Uptime: ");
		sb.append(calculateUptime(msDiff));
		sb.append("\n");
		sb.append("Commands Messages/sec: ");
		sb.append(DECIMAL_FORMAT.format(msgCmdSec));
		sb.append("\n");
		sb.append("Total Messages/sec: ");
		sb.append(DECIMAL_FORMAT.format(msgTotalSec));
		sb.append("\n");
		sb.append("Active/Total groups: ");
		WarDeathLogic model = new WarDeathLogic();
		long groupCounter = -1;
		long activeGroups = -1;
		try {
			Map<String, WarGroup> groups = model.getAllGroups();
			activeGroups = groups.values().stream()
			        .filter(g -> g.getGroupStatus().equals(GroupStatus.GroupStatusActive)).count();
			groupCounter = groups.size();
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
		}
		sb.append(activeGroups != -1 ? Long.toString(activeGroups) : "unknown");
		sb.append("/");
		sb.append(groupCounter != -1 ? Long.toString(groupCounter) : "unknown");
		sb.append("\n");

		LinkedList<String> pushedMsgs = FridayBotApplication.getInstance().getPushStatistics();
		sb.append("Last Pushed Messages:\n");
		for (String p : pushedMsgs)
			sb.append("# " + p + "\n");

		LOG.info("Messages/sec: " + DECIMAL_FORMAT.format(msgCmdSec));
		LOG.info("Total groups: " + groupCounter);
		LOG.info("Status: "
		        + (FridayBotApplication.getInstance().getIsOperational().get() ? "OPERATIONAL" : "MAINTENANCE"));

		sb.append("Scheduled jobs:\n");
		Scheduler scheduler = FridayBotApplication.getInstance().getEventScheduler().getScheduler();
		try {
			for (String groupName : scheduler.getJobGroupNames()) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();

					// get job's trigger
					@SuppressWarnings("unchecked")
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					Date nextFireTime = triggers.get(0).getNextFireTime();

					sb.append("# ");
					sb.append(FridayBotApplication.SDF.format(nextFireTime));
					sb.append(" ");
					sb.append(jobName);
					sb.append("\n");
				}
			}
		} catch (SchedulerException e) {
			sb.append("ERROR - Cannot get the scheduled jobs: " + e + "\n");
		}

		return new TextMessage(sb.toString());
	}

	/**
	 * Calculate uptime in a human readable
	 * 
	 * @param millisecsTime
	 * @return the uptime string
	 */
	public static String calculateUptime(long millisecsTime) {
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long timeMilli = millisecsTime;
		long elapsedDays = timeMilli / daysInMilli;
		timeMilli = timeMilli % daysInMilli;

		long elapsedHours = timeMilli / hoursInMilli;
		timeMilli = timeMilli % hoursInMilli;

		long elapsedMinutes = timeMilli / minutesInMilli;
		timeMilli = timeMilli % minutesInMilli;

		long elapsedSeconds = timeMilli / secondsInMilli;

		return String.format("%d days, %d hours, %d mins, %d secs", elapsedDays, elapsedHours, elapsedMinutes,
		        elapsedSeconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeAdmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
	 */
	@Override
	public String getHelp(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		sb.append(CMD_PREFIX + " <maintenance|operational>?\n");
		if (verbose) {
			sb.append("Prints the current status of FRIDAY. Set FRIDAY new status (maintenance|operational)");
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getCommandPrefix()
	 */
	@Override
	public String getCommandPrefix() {
		return CMD_PREFIX;
	}
}
