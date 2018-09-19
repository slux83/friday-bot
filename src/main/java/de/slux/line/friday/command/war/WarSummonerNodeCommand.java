/**
 * 
 */
package de.slux.line.friday.command.war;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.SummonerNotFoundException;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.logic.war.WarPlacementLogic;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class WarSummonerNodeCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "friday node";
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
	 * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX);
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

			List<String> commandArgs = extractArgs(message);

			// clear up prefix
			commandArgs.remove(0);
			commandArgs.remove(0);

			if (commandArgs.size() < 3) {
				return new TextMessage("Missing arguments, please use " + HelpCommand.CMD_PREFIX
				        + " to see the list of commands and arguments");
			}

			// Deal with multi-insert
			String content = String.join(" ", commandArgs);
			List<String> commands = Arrays.asList(content.split(","));

			int totalUpdates = 0;
			int validUpdates = 0;
			StringBuilder warnings = new StringBuilder();
			Map<Integer, Set<Character>> changes = new HashMap<>();
			for (String command : commands) {
				try {
					totalUpdates++;
					List<String> args = super.extractArgs(command.trim());
					String arg1 = args.remove(0).trim().toUpperCase();
					String arg2 = args.remove(0).trim();

					// Validate the position
					if (!arg1.matches(POSITION_REGEX)) {
						warnings.append("- Invalid argument '" + arg1 + "'\n");
						continue;
					}

					String summonerNum = arg1.substring(0, arg1.length() - 1);
					char placement = arg1.charAt(arg1.length() - 1);

					int summoner = -1;

					// Already validated by the regex
					summoner = Integer.parseInt(summonerNum);

					int node = -1;

					try {
						node = Integer.parseInt(arg2);
					} catch (NumberFormatException e) {
						warnings.append("- Invalid node number. Expected integer, got '" + arg2 + "'\n");
						continue;
					}

					logic.editPlacement(senderId, summoner, placement, node, String.join(" ", args));
					Set<Character> slots = changes.get(summoner);
					if (slots == null) {
						slots = new HashSet<>();
						changes.put(summoner, slots);
					}
					slots.add(placement);
					validUpdates++;

				} catch (WarDaoUnregisteredException e) {
					return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
					        + "' for info on how to register your chat room");
				} catch (SummonerNotFoundException e) {
					warnings.append("- " + e.getMessage() + "\n");
				}
			}

			// Something went wrong
			if (totalUpdates != validUpdates) {
				// We push a warning message
				super.pushMultipleMessages(senderId,
				        validUpdates + "/" + totalUpdates + " updates have been applied:\n",
				        Arrays.asList(warnings.toString()), true);
			}

			// Return the new placement
			Map<Integer, WarSummoner> updatedSummoners = logic.getSummoners(senderId);

			// Filter out the unaffected items
			updatedSummoners.entrySet().removeIf(entry -> !changes.containsKey(entry.getKey()));
			for (Entry<Integer, WarSummoner> entry : updatedSummoners.entrySet()) {
				Set<Character> placements = changes.get(entry.getKey());
				entry.getValue().getPlacements().entrySet().removeIf(e -> !placements.contains(e.getKey()));
			}

			List<String> text = WarPlacementLogic.getSummonersText(updatedSummoners);
			return super.pushMultipleMessages(senderId, "Updated entries:\n\n", text);
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
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <position> <node> <champ>]\n");
		sb.append("Edit the champion information giving a node and the position in the placement table.\n");
		sb.append("Example " + CMD_PREFIX + " 3B 55 5* dupe Medusa");

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
