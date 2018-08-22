package de.slux.line.jarvis.command;

import com.linecorp.bot.client.LineMessagingClient;

/**
 * Abstract command class for all the commands
 * 
 * @author slux
 */
public abstract class AbstractCommand {

	public static final long RESPONSE_TIMEOUT_MS = 10*1000;
	
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
	 * @param userId - can be null
	 * @param senderId - can't be null
	 * @param message
	 * @return the message to send back or null
	 */
	public abstract String execute(String userId, String senderId, String message);

}
