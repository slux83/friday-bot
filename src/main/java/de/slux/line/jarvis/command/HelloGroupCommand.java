/**
 * 
 */
package de.slux.line.jarvis.command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	 * de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return JoinEvent.class.getSimpleName().equals(message);
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
		// Push back the message to the user
		CompletableFuture<BotApiResponse> response = super.messagingClient
				.pushMessage(new PushMessage(senderId, new TextMessage("Hello group of friend!")));

		try {
			response.get(AbstractCommand.RESPONSE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			LOG.warn("Timeout exceeded for " + this.getClass().getSimpleName() + " command: " + e);
		} catch (Exception e) {
			LOG.error("Unknown error when getting the response of the " + this.getClass().getSimpleName() + " command",
					e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeEvent;
	}

}
