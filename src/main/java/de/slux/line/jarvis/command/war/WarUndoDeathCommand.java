/**
 * 
 */
package de.slux.line.jarvis.command.war;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.exception.WarDaoUnregisteredException;
import de.slux.line.jarvis.logic.war.WarDeathLogic;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarUndoDeathCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis undo death";
	private static Logger LOG = LoggerFactory.getLogger(WarUndoDeathCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarUndoDeathCommand(LineMessagingClient messagingClient) {
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
		return message.equalsIgnoreCase(CMD_PREFIX);
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
			WarDeathLogic warModel = new WarDeathLogic();
			warModel.undoLast(senderId);
			return new TextMessage(warModel.getReport(senderId));
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
			        + "' for info on how to register your chat room");
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
		sb.append("[" + CMD_PREFIX + "]\n");
		sb.append("Undo the previous death report insertion");

		return sb.toString();
	}
}
