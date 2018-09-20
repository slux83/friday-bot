/**
 * 
 */
package de.slux.line.friday.command;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class HelpCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "help";
	private static Logger LOG = LoggerFactory.getLogger(HelpCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public HelpCommand(LineMessagingClient messagingClient) {
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
		return message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
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
		StringBuilder sb = new StringBuilder("*** F.R.I.D.A.Y. HELP ***\n");
		sb.append("Use: ");
		sb.append(AbstractCommand.ALL_CMD_PREFIX);
		sb.append(" <option>\n\n");
		sb.append("OPTIONS:\n");

		List<AbstractCommand> commands = FridayBotApplication.getInstance().getCommands();
		LOG.info("Constructing help using " + commands.size() + " command(s)");
		for (AbstractCommand c : commands) {

			// Help for groups
			if (senderId != null && (c.getType().equals(CommandType.CommandTypeWar)
			        || c.getType().equals(CommandType.CommandTypeUtility)
			        || c.getType().equals(CommandType.CommandTypeShared))) {
				// TODO: deal with verbose commands
				sb.append("  " + c.getHelp(false));
			}

			// Help for normal users only
			if (senderId == null && !FridayBotApplication.SLUX_ID.equals(userId)
			        && (c.getType().equals(CommandType.CommandTypeUser)
			                || c.getType().equals(CommandType.CommandTypeShared))) {
				sb.append("  " + c.getHelp(false));
			}

			// Help for admin only
			if (senderId == null && FridayBotApplication.SLUX_ID.equals(userId)
			        && (c.getType().equals(CommandType.CommandTypeUser)
			                || c.getType().equals(CommandType.CommandTypeShared)
			                || c.getType().equals(CommandType.CommandTypeAdmin))) {
				sb.append("  " + c.getHelp(false));
			}

		}

		return new TextMessage(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeShared;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
	 */
	@Override
	public String getHelp(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		sb.append(CMD_PREFIX + "\n");
		if (verbose) {
			sb.append("Prints this help message");
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
