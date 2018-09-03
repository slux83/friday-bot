/**
 * 
 */
package de.slux.line.friday.command;

import java.util.concurrent.CompletableFuture;

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
	public static final String INTRO_VIDEO_LINK = "https://tinyurl.com/y75zw7u8";

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
		CompletableFuture<BotApiResponse> response = super.messagingClient
		        .pushMessage(new PushMessage(senderId, new TextMessage(getGroupWelcomeMessage(userId))));

		try {
			response.get();
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
			if (userProfileFuture != null) {
				UserProfileResponse userProfile = userProfileFuture.get();
				userName = userProfile.getDisplayName();
			}
		} catch (Exception e) {
			LOG.warn("Cannot retrieve user profile for " + userId + ". Reason " + e, e);
		}

		LOG.info("New follower: " + userName + " (" + userId + ")");

		StringBuilder sb = new StringBuilder();
		sb.append("Hello " + userName + "!\n\n");
		sb.append(
		        "Thanks for the add! I'm F.R.I.D.A.Y. the BOT and I'm here to assist you during the MCOC wars and many things more!\n");
		sb.append(
		        "I'm a bot that works best inside a Line chat group, so don't waste more time and invite me in your battle group chat!\n");
		sb.append("Type 'friday help' to see what I can do outside a chat group.\n");

		sb.append("\n");
		sb.append("How-To videos: ");
		sb.append(INTRO_VIDEO_LINK);

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
