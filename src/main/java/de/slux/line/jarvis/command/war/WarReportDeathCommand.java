/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
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
public class WarReportDeathCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis report";
	public static final String CMD_PREFIX_1 = "jarvis";
	public static final String CMD_PREFIX_2 = "report";
	private static Logger LOG = LoggerFactory.getLogger(WarReportDeathCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarReportDeathCommand(LineMessagingClient messagingClient) {
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
		List<String> argsAsList = super.extractArgs(message.replace(CMD_PREFIX, ""));

		// Clean up the empty ones and command
		for (Iterator<String> iterator = argsAsList.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			if (string.trim().isEmpty() || string.trim().equalsIgnoreCase(CMD_PREFIX_1)
					|| string.trim().equalsIgnoreCase(CMD_PREFIX_2)) {
				// Remove the current element from the iterator and the
				// list.
				iterator.remove();
			}
		}
		LOG.debug("war args after clean up: " + argsAsList);
		if (argsAsList.size() < 3) {
			return new TextMessage(
					"Incorrect syntax. Please use the following:\n" + CMD_PREFIX + " <deaths> <node> <champ>");
		}

		int deaths = -1;
		try {
			deaths = Integer.parseInt(argsAsList.get(0).trim());
		} catch (NumberFormatException e) {
			return new TextMessage("Number expected for <deaths>. Please use the following:\n" + CMD_PREFIX
					+ " <deaths> <node> <champ>");
		}

		int node = -1;
		try {
			node = Integer.parseInt(argsAsList.get(1).trim());
		} catch (NumberFormatException e) {
			return new TextMessage("Number expected for <node>. Please use the following:\n" + CMD_PREFIX
					+ " <deaths> <node> <champ>");
		}

		argsAsList.remove(0);
		argsAsList.remove(0);
		String champName = String.join(" ", argsAsList.toArray(new String[] {}));
		String userName = super.getUserName(senderId, userId);

		try {
			WarReportModel warModel = new WarReportModel();
			warModel.addDeath(senderId, deaths, node, champName.trim(), userName);
			return new TextMessage(warModel.getReport(senderId));
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
					+ "' for info on how to register your chat room");
		} catch (Exception e) {
			e.printStackTrace();
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
		sb.append("[" + CMD_PREFIX + " <deaths> <node> <champ>]\n");
		sb.append("Report a new death in AW\n");
		sb.append("Example " + CMD_PREFIX + " 2 45 Medusa");

		return sb.toString();
	}
}
