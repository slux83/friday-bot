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
public class RegisterEventsCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "register events";
    private static Logger LOG = LoggerFactory.getLogger(RegisterEventsCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public RegisterEventsCommand(LineMessagingClient messagingClient) {
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
            boolean registrationOutcome = eventsLogic.register(allGroups.get(senderId), senderId);

            if (!registrationOutcome) {
                return new TextMessage("This group is already registered to receive notifications");
            }

        } catch (Exception e) {
            LOG.error("Cannot register group '" + senderId + "' for events: " + e, e);
            return new TextMessage("Something went wrong: " + e);
        }

        return new TextMessage("From now on this group will receive MCoC event notifications");
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
            sb.append("Registers this group to receive MCOC event reminders like\n");
            sb.append("Donations, AQ and AW reminders, Cat Arena, 1-3 Days events, etc...");
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
