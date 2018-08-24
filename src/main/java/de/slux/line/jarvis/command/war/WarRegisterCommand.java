/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.war.WarReportModel;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarRegisterCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis register";
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
		List<String> args = super.extractArgs(message);

		if (args.size() < 3) {
			return new TextMessage("Please specify your group name. E.g. DM-BG3");
		}

		// Remove prefix
		args.remove(0);
		args.remove(0);
		String arg = String.join(" ", args);

		try {
			WarReportModel warModel = new WarReportModel();
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
		sb.append("[" + CMD_PREFIX + " <ALLYTAG-BG>]\n");
		sb.append("Register this chat group for Jarvis war tools. ");
		sb.append("Use this command later on to modify the name if needed\n");
		sb.append("Example " + CMD_PREFIX + " 4Loki-BG1");

		return sb.toString();
	}
}
