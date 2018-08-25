/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.exception.SummonerNotFoundException;
import de.slux.line.jarvis.dao.exception.SummonerNumberExceededException;
import de.slux.line.jarvis.dao.exception.WarDaoUnregisteredException;
import de.slux.line.jarvis.data.war.WarSummoner;
import de.slux.line.jarvis.logic.war.WarPlacementLogic;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class WarSummonerNodeCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis node";
	private static Logger LOG = LoggerFactory.getLogger(WarSummonerNodeCommand.class);
	private static final String POSITION_REGEX = "^(10?|[1-9])[A-E]";

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarSummonerNodeCommand(LineMessagingClient messagingClient) {
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
		try {
			// we can use this command only to print the last version
			WarPlacementLogic logic = new WarPlacementLogic();

			List<String> args = extractArgs(message);

			// clear up prefix
			args.remove(0);
			args.remove(0);

			if (args.size() < 3) {
				return new TextMessage("Missing arguments, please use " + HelpCommand.CMD_PREFIX
				        + " to see the list of commands and arguments");
			}

			String arg1 = args.remove(0).trim();
			String arg2 = args.remove(0).trim();

			// Validate the position
			if (!arg1.matches(POSITION_REGEX)) {
				return new TextMessage("Invalid argument '" + arg1
				        + "'. Specify the number of the summoner and the position of the placement. E.g. '1A', '3B', '7E', etc...");
			}

			String summonerNum = arg1.substring(0, arg1.length() - 1);
			char placement = arg1.charAt(arg1.length() - 1);

			int summoner = -1;

			try {
				summoner = Integer.parseInt(summonerNum);
			} catch (NumberFormatException e) {
				return new TextMessage("Invalid summoner position. Expected 1-10 values, got " + summonerNum);
			}

			int node = -1;

			try {
				node = Integer.parseInt(arg2);
			} catch (NumberFormatException e) {
				return new TextMessage("Invalid node number. Expected integer, got " + arg2);
			}

			logic.editPlacement(senderId, summoner, placement, node, String.join(" ", args));

			// Return the new placement
			Map<Integer, WarSummoner> updatedSummoners = logic.getSummoners(senderId);
			return new TextMessage(logic.getSummonersText(updatedSummoners));
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
			        + "' for info on how to register your chat room");
		} catch (SummonerNotFoundException e) {
			return new TextMessage(e.getMessage());
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
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeWar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <position> <node> <champ>\n");
		sb.append("Edit the champion information giving a node and the position in the placement table.\n");
		sb.append("Example " + CMD_PREFIX + " 3B 55 5* dupe Medusa");

		return sb.toString();
	}
}
