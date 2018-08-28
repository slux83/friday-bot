/**
 * 
 */
package de.slux.line.friday.command.admin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import de.slux.line.friday.command.AbstractCommand;
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

		// Get all groups
		Map<String, String> groups = Collections.emptyMap();
		try {
			groups = new WarDeathLogic().getAllGroups();
			if (LOG.isDebugEnabled())
				LOG.debug(groups.toString());

		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error retrieving groups: " + e);
		}

		int totalSent = 0;
		for (Entry<String, String> group : groups.entrySet()) {
			try {
				PushMessage pushMessage = new PushMessage(group.getKey(), new TextMessage(bcastMessage));
				CompletableFuture<BotApiResponse> response = super.messagingClient.pushMessage(pushMessage);

				// It will throw an exception if the bot is not any longer in
				// the chat room
				response.get();
				totalSent++;
			} catch (Exception e) {
				LOG.warn("Cannot push message to group " + group + ". Reason: " + e);
			}
		}

		return new TextMessage("Message broadcasted " + totalSent + "/" + groups.size());
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
}
