/**
 * 
 */
package de.slux.line.friday.command.war;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarSummaryDeathCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "summary death";
	private static final String ARG_COMPACT = "compact";
	private static Logger LOG = LoggerFactory.getLogger(WarSummaryDeathCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarSummaryDeathCommand(LineMessagingClient messagingClient) {
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
		return message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX)
		        || message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " " + ARG_COMPACT);
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
			WarDeathLogic warModel = new WarDeathLogic();

			List<String> commandArgs = extractArgs(message);

			// clear up prefix
			commandArgs.remove(0);
			commandArgs.remove(0);
			commandArgs.remove(0);

			boolean compactView = !commandArgs.isEmpty() && commandArgs.get(0).equalsIgnoreCase(ARG_COMPACT);

			List<String> summary = warModel.getSummary(senderId, compactView);
			return super.pushMultipleMessages(senderId, "", summary);
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
			        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
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
		sb.append(CMD_PREFIX + " " + ARG_COMPACT + "?\n");
		if (verbose) {
			sb.append("Prints a detailed summary of deaths for the current war.\n");
			sb.append("\"" + ARG_COMPACT + "\" argument will give you a more dense summary view");
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
