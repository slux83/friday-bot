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
public class WarReportDeathCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "death";
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
	 * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " ");
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

		// Clean up prefix
		args.remove(0);
		args.remove(0);

		if (args.size() < 3) {
			return new TextMessage("Incorrect syntax. Please use the following:\n" + AbstractCommand.ALL_CMD_PREFIX
			        + " " + CMD_PREFIX + " <deaths> <node> <champ>");
		}

		int deaths = -1;
		try {
			deaths = Integer.parseInt(args.get(0).trim());
		} catch (NumberFormatException e) {
			return new TextMessage("Number expected for <deaths>. Please use the following:\n"
			        + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " <deaths> <node> <champ>");
		}

		int node = -1;
		try {
			node = Integer.parseInt(args.get(1).trim());
		} catch (NumberFormatException e) {
			return new TextMessage("Number expected for <node>. Please use the following:\n"
			        + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " <deaths> <node> <champ>");
		}

		args.remove(0);
		args.remove(0);
		String champName = String.join(" ", args.toArray(new String[] {}));
		String userName = super.getUserName(senderId, userId);

		try {
			WarDeathLogic warModel = new WarDeathLogic();
			warModel.addDeath(senderId, deaths, node, champName.trim(), userName);
			return new TextMessage(warModel.getReport(senderId));
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
			        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
		} catch (Exception e) {
			LOG.error("Unexpected error " + e, e);
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
		sb.append(CMD_PREFIX + " <deaths> <node> <champ>\n");
		if (verbose) {
			sb.append("Report a new death in AW\n");
			sb.append("Example " + CMD_PREFIX + " 2 45 Medusa");
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
