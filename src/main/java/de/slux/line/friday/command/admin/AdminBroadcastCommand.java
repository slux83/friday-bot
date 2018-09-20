/**
 * 
 */
package de.slux.line.friday.command.admin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class AdminBroadcastCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "friday broadcast";
	private static Logger LOG = LoggerFactory.getLogger(AdminBroadcastCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public AdminBroadcastCommand(LineMessagingClient messagingClient) {
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
		List<String> args = super.extractArgs(message);

		if (args.size() < 3)
			return new TextMessage("Please provide a message to broadcast");

		// Remove the command prefix
		String prefixPos0 = args.get(0);
		String prefixPos1 = args.get(1);
		String bcastMessage = message.replaceFirst(prefixPos0, "");
		bcastMessage = bcastMessage.replaceFirst(prefixPos1, "");
		bcastMessage = bcastMessage.trim();

		// Get all groups
		Map<String, WarGroup> groups = Collections.emptyMap();
		try {
			groups = new WarDeathLogic().getAllGroups();

			if (LOG.isDebugEnabled())
				LOG.debug("All groups: " + groups.values());

		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error retrieving groups: " + e);
		}

		// Remove all inactive groups
		groups.entrySet().removeIf(e -> e.getValue().getGroupStatus().equals(GroupStatus.GroupStatusInactive));

		String stats = FridayBotApplication.getInstance().pushMultiMessages(groups.values(), bcastMessage);

		return new TextMessage("Message broadcasted\n" + stats);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeAdmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <message>]\n");
		sb.append("Send the message broadcast to all the registered rooms");

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
