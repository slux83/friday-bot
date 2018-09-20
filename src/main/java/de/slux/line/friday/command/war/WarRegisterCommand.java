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
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarRegisterCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "register war";
	private static Logger LOG = LoggerFactory.getLogger(WarRegisterCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarRegisterCommand(LineMessagingClient messagingClient) {
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
		return message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
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

		if (args.size() < 4) {
			return new TextMessage("Please specify your group name. E.g. DM-BG3");
		}

		// Remove prefix
		args.remove(0);
		args.remove(0);
		args.remove(0);
		String arg = String.join(" ", args);

		try {
			WarDeathLogic warModel = new WarDeathLogic();
			warModel.register(senderId, arg.trim());
		} catch (Exception e) {
			LOG.error("Failed executing command " + this.getClass().getSimpleName(), e);
			return new TextMessage("Cannot register BG group: " + e);
		}

		return new TextMessage("BG group chat room successfully registered using the name " + arg.trim());

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
		sb.append(CMD_PREFIX + " <ALLYTAG-BG>\n");
		if (verbose) {
			sb.append("Register this chat group for FRIDAY war tools. ");
			sb.append("Use this command later on to modify the name if needed\n");
			sb.append("Example " + CMD_PREFIX + " 4Loki-BG1");
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
