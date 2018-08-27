/**
 * 
 */
package de.slux.line.friday.command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;

/**
 * This command is triggered when the user adds the BOT as friend (follow)
 * 
 * @author slux
 */
public class HelloUserCommand extends AbstractCommand {
	private static Logger LOG = LoggerFactory.getLogger(HelloUserCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public HelloUserCommand(LineMessagingClient messagingClient) {
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
		return FollowEvent.class.getSimpleName().equals(message);
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
		// FIXME add a proper message here
		CompletableFuture<BotApiResponse> response = super.messagingClient
		        .pushMessage(new PushMessage(senderId, new TextMessage(getGroupWelcomeMessage(userId))));

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

	/**
	 * Get the welcome message when the bot is followed by an user
	 * 
	 * @param userId
	 * @return the welcome message
	 */
	private String getGroupWelcomeMessage(String userId) {
		CompletableFuture<UserProfileResponse> userProfileFuture = super.messagingClient.getProfile(userId);

		String userName = "Summoner";
		try {
			UserProfileResponse userProfile = userProfileFuture.get();
			userName = userProfile.getDisplayName();
		} catch (Exception e) {
			LOG.warn("Cannot retrieve user profile for " + userId + ". Reason " + e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Hello " + userName + "!\n\n");
		sb.append("Thanks for the add! I'm F.R.I.D.A.Y. the BOT and I'm here to assist you during the MCOC wars!\n");
		sb.append(
		        "I'm a bot that works inside a Line chat group, so don't waste more time and invite me in your battle group chat!\n");

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
}
