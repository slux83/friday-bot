/**
 * 
 */
package de.slux.line.friday.command;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

/**
 * This command is triggered when the user adds the BOT to a group (join event)
 * 
 * @author slux
 */
public class HelloGroupCommand extends AbstractCommand {
	private static Logger LOG = LoggerFactory.getLogger(HelloGroupCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public HelloGroupCommand(LineMessagingClient messagingClient) {
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
		return JoinEvent.class.getSimpleName().equals(message);
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
		// Push back the message to the user
		CompletableFuture<BotApiResponse> response = super.messagingClient
		        .pushMessage(new PushMessage(senderId, new TextMessage(getGroupWelcomeMessage(senderId))));

		try {
			response.get();
		} catch (Exception e) {
			LOG.error("Unknown error when getting the response of the " + this.getClass().getSimpleName() + " command",
			        e);
		}

		return null;
	}

	/**
	 * Get the welcome message when the bot joins a group
	 * 
	 * @param senderId
	 * @return the welcome message
	 */
	private String getGroupWelcomeMessage(String senderId) {
		StringBuilder sb = new StringBuilder();
		sb.append("Hello summoners!\n\n");
		sb.append("I'm F.R.I.D.A.Y. the BOT and I'm here to assist you during the MCOC wars!\n");
		sb.append("I can also do many other things, just type '" + AbstractCommand.ALL_CMD_PREFIX + " "
		        + HelpCommand.CMD_PREFIX + "' for the list of commands!\n\n");
		sb.append("Here you can find some info and useful things about me:\n");
		sb.append(InfoCommand.getInfo(senderId));
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeEvent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getCommandPrefix()
	 */
	@Override
	public String getCommandPrefix() {
		return null;
	}
}
