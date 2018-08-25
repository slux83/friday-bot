/**
 * 
 */
package de.slux.line.jarvis.command.admin;

import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.logic.war.WarReportModel;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class AdminStatusCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis status";
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
	 * de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.jarvis.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		StringBuilder sb = new StringBuilder("*** J.A.R.V.I.S. MCOC Line Bot Status ***\n\n");

		// Check if we need to change the status
		List<String> args = super.extractArgs(message);
		if (args.size() > 2) {
			String newStatus = args.get(2).trim();

			if (newStatus.equalsIgnoreCase("operational")) {
				JarvisBotApplication.getInstance().getIsOperational().set(true);
			} else if (newStatus.equalsIgnoreCase("maintenance")) {
				JarvisBotApplication.getInstance().getIsOperational().set(false);
			} else {
				sb.append("WARNING: submitted unknown status ");
				sb.append(newStatus);
				sb.append("\n");
			}
		}

		sb.append("Version: ");
		sb.append(JarvisBotApplication.JARVIS_VERSION);
		sb.append("\n");
		sb.append("Status: ");
		sb.append(JarvisBotApplication.getInstance().getIsOperational().get() ? "OPERATIONAL" : "MAINTENANCE");
		sb.append("\n");
		long msgCounter = JarvisBotApplication.getInstance().getIncomingMsgCounter().get();
		long startupMs = JarvisBotApplication.getInstance().getStartup().getTime();
		long nowMs = System.currentTimeMillis();
		long msDiff = Math.abs(nowMs - startupMs);
		double msgSec = (msgCounter / (msDiff / 1000.0));
		sb.append("Total messages: ");
		sb.append(Long.toString(msgCounter));
		sb.append("\n");
		sb.append("Uptime: ");
		sb.append(calculateUptime(msDiff));
		sb.append("\n");
		sb.append("Messages/sec: ");
		sb.append(DECIMAL_FORMAT.format(msgSec));
		sb.append("\n");

		sb.append("Total groups: ");
		WarReportModel model = new WarReportModel();
		int groupCounter = -1;
		try {
			groupCounter = model.getAllGroups().size();
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
		}
		sb.append(groupCounter != -1 ? Integer.toString(groupCounter) : "unknown");
		sb.append("\n");

		LOG.info("Messages/sec: " + DECIMAL_FORMAT.format(msgSec));
		LOG.info("Total groups: " + groupCounter);
		LOG.info("Status: "
		        + (JarvisBotApplication.getInstance().getIsOperational().get() ? "OPERATIONAL" : "MAINTENANCE"));

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
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeAdmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <(maintenance|operational)?>]\n");
		sb.append("Prints the current status of JARVIS. Set JARVIS new status (maintenance|operational)");

		return sb.toString();
	}
}
