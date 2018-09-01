/**
 * 
 */
package de.slux.line.friday.command;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.scheduler.McocDayInfo;
import de.slux.line.friday.scheduler.McocSchedulerImporter;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class EventInfoCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "friday events";
	private static Logger LOG = LoggerFactory.getLogger(EventInfoCommand.class);

	private McocSchedulerImporter schedulerImporter;

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public EventInfoCommand(LineMessagingClient messagingClient, McocSchedulerImporter schedulerImporter) {
		super(messagingClient);
		this.schedulerImporter = schedulerImporter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX + " ") || message.equalsIgnoreCase(CMD_PREFIX);
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
		try {

			// Today's events
			if (message.equalsIgnoreCase(CMD_PREFIX)) {
				Date now = new Date();
				String today = McocSchedulerImporter.DATE_FORMAT.format(now);
				McocDayInfo todayEvents = this.schedulerImporter.getMcocScheduler().get(today);
				if (todayEvents == null) {
					return new TextMessage("Nothing found for today");
				}
				List<String> response = getMessageForEvents(Arrays.asList(todayEvents));

				return super.pushMultipleMessages(senderId, "MCOC Today's events:\n\n", response);
			} else {
				// TODO: tomorrow, week
			}
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error: " + e);
		}

		return null;
	}

	/**
	 * Get the String from the list of events
	 * 
	 * @param eventsList
	 * @return the readable string(s)
	 */
	private List<String> getMessageForEvents(List<McocDayInfo> eventsList) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeShared;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <tomorrow> OR <week>\n");
		sb.append("Shows the MCOC events for today (default with no arguments), tomorrow or the week.\n");
		sb.append("Example '" + CMD_PREFIX + "' or '" + CMD_PREFIX + " week'");

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
