/**
 * 
 */
package de.slux.line.friday.command;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.scheduler.McocDayInfo;
import de.slux.line.friday.scheduler.McocSchedulerImporter;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class EventInfoCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "events";
	private static final String TOMORROW_ARG = "tomorrow";
	private static final String WEEK_ARG = "week";
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
		return message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " ")
		        || message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
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
			if (message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX)) {
				Date now = new Date(FridayBotApplication.getInstance().getClockReference().millis());
				String today = McocSchedulerImporter.DATE_FORMAT.format(now);
				McocDayInfo todayEvents = this.schedulerImporter.getMcocScheduler().get(today);
				if (todayEvents == null) {
					return new TextMessage("Nothing found for today");
				}

				String response = getMessageForEvents(todayEvents);
				return new TextMessage("MCOC Today's events:\n" + response);
			} else {
				List<String> args = super.extractArgs(message);
				// Clear up prefix
				args.remove(0);
				args.remove(0);

				String eventQualifier = args.get(0);
				if (TOMORROW_ARG.equalsIgnoreCase(eventQualifier)) {
					// Show tomorrow's events
					Calendar c = Calendar.getInstance();
					c.setTime(new Date(FridayBotApplication.getInstance().getClockReference().millis()));
					c.add(Calendar.DAY_OF_MONTH, 1);

					String tomorrow = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());
					McocDayInfo tomorrowEvents = this.schedulerImporter.getMcocScheduler().get(tomorrow);

					if (tomorrowEvents == null) {
						return new TextMessage("Nothing found for tomorrow");
					}

					String response = getMessageForEvents(tomorrowEvents);
					return new TextMessage("MCOC Tomorrow's events:\n" + response);
				} else if (WEEK_ARG.equalsIgnoreCase(eventQualifier)) {
					// Show the week events
					Calendar c = Calendar.getInstance();

					StringBuilder sb = new StringBuilder("MCOC Week events:\n\n");
					for (int i = 0; i < 8; i++) {
						c.setTime(new Date(FridayBotApplication.getInstance().getClockReference().millis()));
						c.add(Calendar.DAY_OF_MONTH, i);
						String day = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());
						McocDayInfo dayEvents = this.schedulerImporter.getMcocScheduler().get(day);

						if (dayEvents == null) {
							LOG.error("Missing events for day " + day);
							sb.append("Missing data\n");
							continue;
						}

						String eventText = getMessageForEvents(dayEvents);
						sb.append(" * ");
						sb.append(day);
						sb.append(" *\n");
						sb.append(eventText);
						sb.append("\n");
					}

					if (sb.toString().length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
						LOG.error("BUG: weekly events produced a super long message that has to be split");
					}

					return new TextMessage(sb.toString());
				} else {
					// Something unknown
					return new TextMessage("Sorry, I can't provide events for '" + eventQualifier
					        + "'. Only 'tomorrow' or 'week' arguments are accepted\n");
				}
			}
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error: " + e);
		}
	}

	/**
	 * Get the String from the events
	 * 
	 * @param events
	 * @return the readable string(s)
	 */
	private String getMessageForEvents(McocDayInfo events) {
		StringBuilder sb = new StringBuilder();

		sb.append("AQ Status: ");
		switch (events.getAqStatus()) {
			case DAY1:
				sb.append("Day 1\n");
				break;
			case DAY2:
				sb.append("Day 2\n");
				break;
			case DAY3:
				sb.append("Day 3\n");
				break;
			case DAY4:
				sb.append("Day 4\n");
				break;
			case DAY5:
				sb.append("Day 5\n");
				break;
			case OFF:
				sb.append("Off\n");
				break;
		}

		sb.append("AW Status: ");
		switch (events.getAwStatus()) {
			case ATTACK:
				sb.append("Attack\n");
				break;
			case MAINTENANCE:
				sb.append("Maintenance\n");
				break;
			case PLACEMENT:
				sb.append("Placement\n");
				break;
		}

		switch (events.getCatArenaStatus()) {
			case OFF:
				break;
			case T1A:
				sb.append("Arena: T1 Alpha\n");
				break;
			case T4B:
				sb.append("Arena: T4 Basic\n");
				break;
		}

		switch (events.getOneDayEventStatus()) {
			case ALLY_HELP:
				sb.append("1-Day: Alliance Help\n");
				break;
			case LOYALTY_SPEND:
				sb.append("1-Day: Loyalty Spend\n");
				break;
			case OFF:
				break;
		}

		switch (events.getThreeDaysEventStatus()) {
			case COMPLETION:
				sb.append("3-Days: Completion\n");
				break;
			case ITEMUSE:
				sb.append("3-Days: Item Use\n");
				break;
			case TEAMUSE:
				if (events.getTeamUse() == null)
					sb.append("3-Days: Team Use\n");
				else
					sb.append("3-Days: Team Use " + events.getTeamUse() + "\n");
				break;
			case ARENA_WINS:
				sb.append("3-Days: Arena Wins\n");
				break;
			case OFF:
				break;
			default:
				break;

		}

		return sb.toString();
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
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
	 */
	@Override
	public String getHelp(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		sb.append(CMD_PREFIX + " <tomorrow|week>\n");
		if (verbose) {
			sb.append("Shows the MCOC events for today (default with no arguments), tomorrow or the week.\n");
			sb.append("Example '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + "' or '"
			        + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " week'");
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
