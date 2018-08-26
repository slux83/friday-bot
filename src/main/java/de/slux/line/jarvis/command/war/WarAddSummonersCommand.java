/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.exception.SummonerNumberExceededException;
import de.slux.line.jarvis.dao.exception.WarDaoUnregisteredException;
import de.slux.line.jarvis.data.war.WarSummoner;
import de.slux.line.jarvis.logic.war.WarPlacementLogic;

/**
 * This command adds summoners of the current war
 * 
 * @author slux
 */
public class WarAddSummonersCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis summoners";
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
	 * de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX + " ") || message.equalsIgnoreCase(CMD_PREFIX);
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
			if (!message.equalsIgnoreCase(CMD_PREFIX)) {
				List<String> args = extractArgs(message);
				// clear up prefix
				args.remove(0);
				args.remove(0);

				if (args.size() == 0) {
					return new TextMessage("Please specify a list of summoner names");
				}
				String summoners = String.join(" ", args);
				String[] summonerNamesSplit = summoners.split(",");
				List<String> summonerNames = new ArrayList<>();
				for (String s : summonerNamesSplit) {
					summonerNames.add(s.trim());
				}

				// Add names
				logic.addSummoners(senderId, summonerNames);
			}

			// Return the new placement
			Map<Integer, WarSummoner> updatedSummoners = logic.getSummoners(senderId);
			return new TextMessage(logic.getSummonersText(updatedSummoners));
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
		sb.append("[" + CMD_PREFIX + " <name1, name2, ...?>\n");
		sb.append("Add summoner names (max " + WarPlacementLogic.MAX_SUMMONERS
		        + ") to the placement table for the current war.\n");
		sb.append("Use '" + CMD_PREFIX + "' only to simply print the placement table.\n");
		sb.append("Example " + CMD_PREFIX + " John Doe, FooBar, slux83");

		return sb.toString();
	}
}
