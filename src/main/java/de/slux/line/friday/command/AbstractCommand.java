package de.slux.line.friday.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract command class for all the commands
 *
 * @author slux
 */
public abstract class AbstractCommand {
    private static Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);
    public static final String ALL_CMD_PREFIX = "friday";

    protected AtomicInteger hits;

    /**
     * The type of the command
     *
     * @author slux
     */
    public enum CommandType {
        CommandTypeEvent, CommandTypeWar, CommandTypeUnknown, CommandTypeUtility, CommandTypeAdmin, CommandTypeShared, CommandTypeUser
    }

    public static final long RESPONSE_TIMEOUT_MS = 10 * 1000;

    protected LineMessagingClient messagingClient;

    /**
     * Constructor
     *
     * @param messagingClient
     */
    public AbstractCommand(LineMessagingClient messagingClient) {
        this.hits = new AtomicInteger();
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
     * Get the command prefix
     *
     * @return the command prefix or null if none
     */
    public abstract String getCommandPrefix();

    /**
     * Execute this command
     *
     * @param userId   - can be null
     * @param senderId - can't be null
     * @param message
     * @return the message to send back or null
     */
    public abstract TextMessage execute(String userId, String senderId, String message);

    /**
     * Get the help for this command
     *
     * @param verbose if the help has to be verbose and detailed
     * @return default implementation returns null;
     */
    public String getHelp(boolean verbose) {
        return null;
    }

    /**
     * Get the command type
     *
     * @return Default implementation returns
     * {@link CommandType#CommandTypeUnknown}
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
        String[] args = msg.trim().split(" ");
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
            LOG.warn("Cannot retrieve profile (via group chat) for user (id=" + userId + "): " + e.getMessage());
        }

        // Second attempt to get the user name from the profile itself.
        if (userName.startsWith("unknown_")) {
            CompletableFuture<UserProfileResponse> userProfileDirectFuture = this.messagingClient.getProfile(userId);
            try {
                userProfile = userProfileDirectFuture.get();
                userName = userProfile.getDisplayName();
            } catch (Exception e) {
                LOG.warn("Cannot retrieve profile (directly) for user (id=" + userId + "): " + e.getMessage());
            }
        }

        LOG.debug("Got username " + userName + " for userId=" + userId);

        return userName;
    }

    protected TextMessage pushMultipleMessages(String senderId, String header, List<String> messages, boolean forcePush)
            throws Exception {

        if (messages.isEmpty())
            return null;

        TextMessage firstMessage = new TextMessage(header + messages.get(0));

        if (messages.size() == 1 && !forcePush)
            return firstMessage;

        PushMessage pushMessage = new PushMessage(senderId, firstMessage);
        this.messagingClient.pushMessage(pushMessage).get();

        for (int i = 1; i < messages.size(); i++) {
            pushMessage = new PushMessage(senderId, new TextMessage(messages.get(i)));
            this.messagingClient.pushMessage(pushMessage).get();
        }

        return null;
    }

    protected String pushMultipleMessages2(String senderId, String header, List<String> messages)
            throws Exception {

        if (messages.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        sb.append(header + messages.get(0) + "\n");

        for (int i = 1; i < messages.size(); i++) {
            sb.append(messages.get(i) + "\n");
        }

        if (sb.length() > 2000)
        {
            sb.delete(1950, sb.length());
            sb.append("...");
        }

        return sb.toString();
    }

    /**
     * Pushes the messages with a header to the senderId
     *
     * @param senderId
     * @param header
     * @param messages
     * @return TextMessage if the messages contains only one text, otherwise
     * null
     * @throws Exception
     */
    protected TextMessage pushMultipleMessages(String senderId, String header, List<String> messages) throws Exception {
        return pushMultipleMessages(senderId, header, messages, false);
    }

    /**
     * Get the number of times this command has been hit
     *
     * @return number of hits
     */
    public int getHits() {
        return this.hits.get();
    }

    /**
     * Hit this command adding one to the counter
     */
    public void addHit() {
        this.hits.incrementAndGet();
    }
}
