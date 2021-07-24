/**
 *
 */
package de.slux.line.friday.command.admin;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This command can notify a text to all the friends/groups
 *
 * @author slux
 */
public class AdminNotificationCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "notification";
    private static Logger LOG = LoggerFactory.getLogger(AdminNotificationCommand.class);

    /**
     * The source type enumeration
     */
    public enum SourceType {
        Group,
        User,
        Room,
        Unknown
    }

    private AtomicReference<String> latestNotificationMessage;
    private ConcurrentHashMap<String, SourceType> notifiedIDs;

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public AdminNotificationCommand(LineMessagingClient messagingClient, FridayBotApplication app) {
        super(messagingClient, app);
        this.latestNotificationMessage = new AtomicReference<>();
        this.notifiedIDs = new ConcurrentHashMap<>();
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
        List<String> args = super.extractArgs(message);

        if (args.size() == 2) {
            // We send back only the status of the notification if any.
            StringBuilder sb = new StringBuilder();

            if (this.latestNotificationMessage.get() == null)
                sb.append("No notification found.\n");
            else {
                sb.append("The notification:\n");
                sb.append(this.latestNotificationMessage.get());
                sb.append("\n\nHas been notified so far to:\n");
                sb.append(countIdsBySource(SourceType.User));
                sb.append(" user(s)\n");
                sb.append(countIdsBySource(SourceType.Group));
                sb.append(" group(s)\n");
                sb.append(countIdsBySource(SourceType.Room));
                sb.append(" room(s)\n");
                sb.append(countIdsBySource(SourceType.Unknown));
                sb.append(" unknown(s)");
            }

            return new TextMessage(sb.toString());
        }

        if (args.size() < 3)
            return new TextMessage("Please provide a message to push");

        // Remove the command prefix
        String prefixPos0 = args.get(0);
        String prefixPos1 = args.get(1);
        String notificationMessage = message.replaceFirst(prefixPos0, "");
        notificationMessage = notificationMessage.replaceFirst(prefixPos1, "");
        notificationMessage = notificationMessage.trim();

        this.latestNotificationMessage.set(notificationMessage);
        this.notifiedIDs.clear();

        return new TextMessage("Notification saved\n");
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
     * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
     */
    @Override
    public String getHelp(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        sb.append(CMD_PREFIX + " <message?>\n");
        if (verbose) {
            sb.append("Add the notification to all the chats at the next command received.\n");
            sb.append("Without the <message> argument, shows the current status of the notification");
        }

        return sb.toString();
    }

    /**
     * Counts the elements in notifiedIDs that has the matching type
     * @param type
     * @return the count by {@link SourceType}
     */
    private long countIdsBySource(SourceType type) {
        return this.notifiedIDs.entrySet().stream().filter(e -> e.getValue().equals(type)).count();
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

    /**
     * Get the latest message and also updates the list of already notified sources
     *
     * @param type the source type
     * @param sourceId the source of the event
     * @return the latestNotificationMessage already formatted
     */
    public String getLatestNotificationMessage(String sourceId, SourceType type) {
        if (this.latestNotificationMessage.get() == null) return null;

        if (this.notifiedIDs.containsKey(sourceId)) {
            // User already notified
            return null;
        }
        this.notifiedIDs.put(sourceId, type);
        LOG.info("Notifying SOURCE_ID=" + sourceId + " TYPE=" + type.name() + " with the notification " +
                this.latestNotificationMessage.get());

        String notificationIcon = new String(Character.toChars(0x100035));

        return notificationIcon + " Notification:\n" + this.latestNotificationMessage.get() + "\n\n";
    }
}
