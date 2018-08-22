package de.slux.line.jarvis.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

/**
 * Abstract command class for all the commands
 * 
 * @author slux
 */
public abstract class AbstractCommand {

	/**
	 * The type of the command
	 * 
	 * @author Alessandro di Fazio
	 */
	public enum CommandType {
		CommandTypeEvent, CommandTypeWar, CommandTypeUnknown
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
}
