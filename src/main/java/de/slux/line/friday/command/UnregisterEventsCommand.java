/**
 *
 */
package de.slux.line.friday.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.logic.ScheduleEventsLogic;
import de.slux.line.friday.logic.war.WarDeathLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This command is triggered to register the gorup receiving the schedule events
 *
 * @author slux
 */
public class UnregisterEventsCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "unregister events";
    private static Logger LOG = LoggerFactory.getLogger(UnregisterEventsCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public UnregisterEventsCommand(LineMessagingClient messagingClient) {
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
        return message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX);
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

        try {
            WarDeathLogic warLogic = new WarDeathLogic();
            ScheduleEventsLogic eventsLogic = new ScheduleEventsLogic();

            Map<String, WarGroup> allGroups = warLogic.getAllGroups();
            boolean registrationOutcome = eventsLogic.unregister(allGroups.get(senderId), senderId);

            if (!registrationOutcome) {
                return new TextMessage("This group was never registered to receive notifications");
            }

        } catch (Exception e) {
            LOG.error("Cannot unregister group '" + senderId + "' for events: " + e, e);
            return new TextMessage("Something went wrong: " + e);
        }

        return new TextMessage("The notifications for MCoC events have been disabled");
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
            sb.append("Unregisters this group in order to stop receiving MCOC events");
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
