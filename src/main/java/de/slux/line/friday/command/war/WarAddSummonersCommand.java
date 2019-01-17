/**
 * 
 */
package de.slux.line.friday.command.war;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.SummonerNumberExceededException;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.logic.war.WarPlacementLogic;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class WarAddSummonersCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "summoners";
	private static final String ARG_COMPACT = "compact";
	private static Logger LOG = LoggerFactory.getLogger(WarAddSummonersCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarAddSummonersCommand(LineMessagingClient messagingClient) {
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
			// we can use this command only to print the last version
			WarPlacementLogic logic = new WarPlacementLogic();
			List<String> args = extractArgs(message);

			// Summoner insertion
			if (!message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX) && !message
			        .equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " " + ARG_COMPACT)) {

				List<String> summonerNames = new ArrayList<>();

				// clear up prefix
				args.remove(0);
				args.remove(0);

				if (args.size() == 0) {
					// Can't really happen
					return new TextMessage("Please specify a list of summoner names");
				}
				String summoners = String.join(" ", args);
				String[] summonerNamesSplit = summoners.split(",");
				for (String s : summonerNamesSplit) {
					summonerNames.add(s.trim());
				}

				// Add names
				logic.addSummoners(senderId, summonerNames);
				return new TextMessage("Added " + summonerNames.size() + " new summoner(s)");
			}

			// clear up prefix
			args.remove(0);
			args.remove(0);

			boolean compactView = !args.isEmpty() && args.get(0).equalsIgnoreCase(ARG_COMPACT);

			// Return the placement summary
			Map<Integer, WarSummoner> updatedSummoners = logic.getSummoners(senderId);
			List<String> text = null;

			if (compactView)
				text = WarPlacementLogic.getSummonersCompactText(updatedSummoners);
			else
				text = WarPlacementLogic.getSummonersText(updatedSummoners);
			return super.pushMultipleMessages(senderId, "*** CURRENT WAR PLACEMENTS ***\n\n", text);
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
			        + "' for info on how to register your chat room");
		} catch (SummonerNumberExceededException e) {
			return new TextMessage(e.getMessage());
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeWar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
	 */
	@Override
	public String getHelp(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		sb.append(CMD_PREFIX + " " + ARG_COMPACT + "? or <name1, name2, ...?>\n");
		if (verbose) {
			sb.append("Add summoner names (max " + WarPlacementLogic.MAX_SUMMONERS
			        + ") to the placement table for the current war.\n");
			sb.append("Use '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX
			        + "' only to simply print the placement table.\n");
			sb.append("Example '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " John Doe, FooBar, slux83'");
			sb.append("Use '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " " + ARG_COMPACT
			        + "' only to simply print the placement table but with a dense view.\n");
			sb.append("Example '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " John Doe, FooBar, slux83'");
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
