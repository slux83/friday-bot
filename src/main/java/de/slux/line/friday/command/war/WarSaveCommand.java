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
import de.slux.line.friday.dao.exception.WarDaoDuplicatedAllianceTagException;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarSaveCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "friday save";
	private static Logger LOG = LoggerFactory.getLogger(WarSaveCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarSaveCommand(LineMessagingClient messagingClient) {
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
		String allyTag = "";
		try {
			// Get the summary of a specific day
			WarDeathLogic warModel = new WarDeathLogic();
			List<String> argsAsList = super.extractArgs(message);

			if (argsAsList.size() < 3)
				return new TextMessage("Please specify the opponent alliance tag");

			StringBuilder allyTagBuilder = new StringBuilder();
			for (int i = 2; i < argsAsList.size(); ++i) {
				allyTagBuilder.append(argsAsList.get(i));
				allyTagBuilder.append(" ");
			}

			allyTag = allyTagBuilder.toString().trim();
			warModel.saveWar(senderId, allyTag);
			return new TextMessage("War reports against '" + allyTag + "' saved successfully");
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
			        + "' for info on how to register your chat room");
		} catch (WarDaoDuplicatedAllianceTagException e) {
			return new TextMessage(
			        "Error: the alliance '" + allyTag + "' has been already registered today. Use the command '"
			                + WarDeleteCommand.CMD_PREFIX + "' to delete the previeus one");
		} catch (Exception e) {
			LOG.error("Unexpected exception: " + e, e);
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
		sb.append("[" + CMD_PREFIX + " <ally_tag>]\n");
		sb.append("Saves the current reports in the archive for future reference.\n");
		sb.append("Use this command only after the war has ended");

		return sb.toString();
	}
}
