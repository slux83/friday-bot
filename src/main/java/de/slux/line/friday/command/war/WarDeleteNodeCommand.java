/**
 *
 */
package de.slux.line.friday.command.war;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.logic.war.WarDeathLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This command is triggered on the register command
 *
 * @author slux
 */
public class WarDeleteNodeCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "delete node";
    private static Logger LOG = LoggerFactory.getLogger(WarDeleteNodeCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public WarDeleteNodeCommand(LineMessagingClient messagingClient, FridayBotApplication app) {
        super(messagingClient, app);
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
        try {
            List<String> args = super.extractArgs(message);

            if (args.size() < 4)
                return new TextMessage("Missing argument. Please specify the node number");

            args.remove(0);
            args.remove(0);
            args.remove(0);
            String nodeNum = args.get(0).trim();

            int node = -1;
            try {
                node = Integer.parseInt(nodeNum);
            } catch (NumberFormatException e) {
                return new TextMessage("Expected integer value for <node> but got '" + nodeNum + "'");
            }

            WarDeathLogic warModel = new WarDeathLogic();
            int deletedEntries = warModel.deleteNode(senderId, node);
            return new TextMessage(
                    "Deleted " + deletedEntries + " entry(ies) for node " + node + "\n\n" + warModel.getReport(senderId));

        } catch (WarDaoUnregisteredException e) {
            return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
                    + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
        } catch (Exception e) {
            LOG.error("Unexpected error: " + e, e);
            return new TextMessage("Unexpected error: " + e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.slux.line.friday.command.AbstractCommand#getType()
     */
    @Override
    public CommandType getType() {
        return CommandType.CommandTypeWar;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.slux.line.friday.command.AbstractCommand#getHelp(boolean)
     */
    @Override
    public String getHelp(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        sb.append(CMD_PREFIX + " <node>\n");
        if (verbose) {
            sb.append("Delete all the entries for the given node");
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
