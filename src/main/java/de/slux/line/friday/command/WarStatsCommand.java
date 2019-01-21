/**
 * 
 */
package de.slux.line.friday.command;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.logic.StatsLogic;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class WarStatsCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "war stats";
	private static Logger LOG = LoggerFactory.getLogger(WarStatsCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarStatsCommand(LineMessagingClient messagingClient) {
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

		List<String> args = super.extractArgs(message);

		if (args.size() < 4)
			return new TextMessage("Argument missing, please provide a war node or champion name");

		// Remove the command prefix
		args.remove(0);
		args.remove(0);
		args.remove(0);

		String arg = String.join(" ", args).trim();
		Integer nodeNumber;
		try {
			StatsLogic logic = new StatsLogic(false);

			try {
				nodeNumber = Integer.parseInt(arg);
				Map<Integer, List<HistoryStats>> warNodeStats = FridayBotApplication.getInstance()
				        .getWarNodeStatistics();

				if (warNodeStats == null) {
					return new TextMessage("Sorry, statistics are not available yet. Please try later");
				}

				return new TextMessage(logic.getNodeStats(warNodeStats, nodeNumber));
			} catch (NumberFormatException ne) {
				// We try with champion matching
				Map<String, List<HistoryStats>> warChampStats = FridayBotApplication.getInstance()
				        .getWarChampStatistics();

				if (warChampStats == null) {
					return new TextMessage("Sorry, statistics are not available yet. Please try later");
				}

				return new TextMessage(logic.getChampionStats(warChampStats, arg.trim()));
			}

		} catch (IOException e) {
			// This can't happen
			LOG.error("Unexpected I/O error", e);
			return new TextMessage("Unexpected error " + e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
	 */
	@Override
	public String getHelp(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		sb.append(CMD_PREFIX + " <node|champ>\n");
		if (verbose) {
			sb.append("Prints the statistics of the top-5 champions for the given node or champion.\n");
			sb.append("Examples:\n");
			sb.append(AbstractCommand.ALL_CMD_PREFIX);
			sb.append(" ");
			sb.append(CMD_PREFIX);
			sb.append(" 44\n");
			sb.append(AbstractCommand.ALL_CMD_PREFIX);
			sb.append(" ");
			sb.append(CMD_PREFIX);
			sb.append(" Medusa\n");
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
