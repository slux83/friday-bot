package de.slux.line.jarvis.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;

/**
 * Abstract command class for all the commands
 * 
 * @author slux
 */
public abstract class AbstractCommand {
	private static Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);

	/**
	 * The type of the command
	 * 
	 * @author Alessandro di Fazio
	 */
	public enum CommandType {
		CommandTypeEvent, CommandTypeWar, CommandTypeUnknown, CommandTypeUtility, CommandTypeAdmin
	}

	public static final long RESPONSE_TIMEOUT_MS = 10 * 1000;

	protected LineMessagingClient messagingClient;

	/**
	 * Constructor
	 * 
	 * @param messagingClient
	 */
	public AbstractCommand(LineMessagingClient messagingClient) {
		this.messagingClient = messagingClient;
	}

	/**
	 * Method will check if the message can trigger this command
	 * 
	 * @param message
	 * @return true if the message triggers the command, false otherwise
	 */
	public abstract boolean canTrigger(String message);

	/**
	 * Execute this command
	 *
	 * @param userId
	 *            - can be null
	 * @param senderId
	 *            - can't be null
	 * @param message
	 * @return the message to send back or null
	 */
	public abstract TextMessage execute(String userId, String senderId, String message);

	/**
	 * Get the help for this command
	 * 
	 * @return default implementation returns null;
	 */
	public String getHelp() {
		return null;
	}

	/**
	 * Get the command type
	 * 
	 * @return Default implementation returns
	 *         {@link CommandType#CommandTypeUnknown}
	 */
	public CommandType getType() {
		return CommandType.CommandTypeUnknown;
	}

	/**
	 * Extract arguments from message, cleaning up the C2A0 bytes
	 * 
	 * @param message
	 * @return the list of args
	 */
	protected List<String> extractArgs(String message) {
		// Replace stupid C2A0 bytes in UTF8 (html white space)
		String msg = message.replaceAll("[\\p{Zs}\\s]+", " ");
		String args[] = msg.trim().split(" ");
		List<String> argsAsList = new ArrayList<String>(Arrays.asList(args));

		return argsAsList;
	}

	/**
	 * Retrieve the user id or unknown generated string if none
	 * 
	 * @param groupId
	 * @param userId
	 * @return the user display name or unknown_UID_END
	 */
	protected String getUserName(String groupId, String userId) {
		CompletableFuture<UserProfileResponse> userProfileFuture = this.messagingClient.getGroupMemberProfile(groupId,
				userId);

		UserProfileResponse userProfile = null;
		String userName = "unknown_" + userId.substring(userId.length() - 10);
		try {
			userProfile = userProfileFuture.get();
			userName = userProfile.getDisplayName();
		} catch (Exception e) {
			LOG.warn("Cannot retrieve profile for user (id=" + userId + "): " + e.getMessage());
		}

		LOG.debug("Got username " + userName + " for userId=" + userId);

		return userName;
	}
}
