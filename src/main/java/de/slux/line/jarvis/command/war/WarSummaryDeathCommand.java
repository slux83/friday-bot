/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.WarDaoUnregisteredException;
import de.slux.line.jarvis.war.WarReportModel;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarSummaryDeathCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis death summary";
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
			WarReportModel warModel = new WarReportModel();
			List<String> summary = warModel.getSummary(senderId);
			for (String s : summary) {
				PushMessage pushMessage = new PushMessage(senderId, new TextMessage(s));
				super.messagingClient.pushMessage(pushMessage).get();
			}
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
			        + "' for info on how to register your chat room");
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error: " + e);
		}

		return null;
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
		sb.append("Prints a detailed summary of deaths for the current war");

		return sb.toString();
	}
}
