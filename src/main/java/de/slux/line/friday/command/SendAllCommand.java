/**
 *
 */
package de.slux.line.friday.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This command is triggered to register the gorup receiving the schedule events
 *
 * @author slux
 */
public class SendAllCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "send all";
    private static Logger LOG = LoggerFactory.getLogger(SendAllCommand.class);

    // key=groupId+userId, value=triggering date/time
    private Map<String, Date> spammersLog;

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public SendAllCommand(LineMessagingClient messagingClient, FridayBotApplication app) {
        super(messagingClient, app);
        this.spammersLog = new ConcurrentHashMap<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
     */
    @Override
    public boolean canTrigger(String message) {
        return message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
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
        int reachedUsers = 0;
        try {
            List<String> args = super.extractArgs(message);

            if (args.size() < 4)
                return new TextMessage("Please provide a message to send to all the members of this chat group");

            // Check for spammers
            Date lastSentMusticast = this.spammersLog.get(senderId + userId);
            if (lastSentMusticast != null) {
                Date now = new Date(FridayBotApplication.getInstance().getClockReference().millis());
                LocalDateTime nowLocal = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
                LocalDateTime lastSentMulticastLocal = LocalDateTime.ofInstant(lastSentMusticast.toInstant(), ZoneId.systemDefault());

                Duration durationBetweenTwoDates = Duration.between(nowLocal, lastSentMulticastLocal);

                // Can't spam multicast messages
                if (durationBetweenTwoDates.abs().toMinutes() < 60) {
                    return new TextMessage("Sorry, you can't spam multicast messages. You will be able to send another multicast message in " +
                            (60 - durationBetweenTwoDates.abs().toMinutes()) + " minute(s)");
                }
            }

            // Remove the command prefix
            String prefixPos0 = args.get(0);
            String prefixPos1 = args.get(1);
            String prefixPos2 = args.get(2);
            String msgToSend = message.replaceFirst(prefixPos0, "");
            msgToSend = msgToSend.replaceFirst(prefixPos1, "");
            msgToSend = msgToSend.replaceFirst(prefixPos2, "");
            msgToSend = msgToSend.trim();

            String userName = super.getUserName(senderId, userId);

            if (userName.startsWith("unknown_"))
                return new TextMessage("Cannot get user profile. Your Line user is not registered properly " +
                        "(maybe you did not accept the Line rules). " +
                        "Sorry but you can't use this command until you register your user properly.");

            Set<String> userIds = FridayBotApplication.getInstance().getGroupUserIds(senderId);
            userIds.remove(userId);    // Remove the caller
            if (userIds.isEmpty())
                return new TextMessage("It looks like you are the only one in this room. Operation aborted.");

            LOG.info("Sending private (multicast) messages to " + userIds.size() + " users");

            TextMessage lineMessage = new TextMessage(userName + " sent this message from one of the group chats:\n\n" + msgToSend);
            reachedUsers = FridayBotApplication.getInstance().sendMessageToUsers(userIds.stream().collect(Collectors.toList()), lineMessage);

            this.spammersLog.put(senderId + userId, new Date(FridayBotApplication.getInstance().getClockReference().millis()));
        } catch (Exception e) {
            LOG.error("Send the message to all '" + senderId + "' " + e, e);
            return new TextMessage("Something went wrong: " + e);
        }

        return new TextMessage("Message sent privately to " + reachedUsers + " user(s)");
    }

    /*
     * (non-Javadoc)
     *
     * @see de.slux.line.friday.command.AbstractCommand#getType()
     */
    @Override
    public CommandType getType() {
        return CommandType.CommandTypeUtility;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
     */
    @Override
    public String getHelp(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        sb.append(CMD_PREFIX + "\n");
        if (verbose) {
            sb.append("(beta) Sends a message to all the members of this chat group, but as private message.");
        }

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
