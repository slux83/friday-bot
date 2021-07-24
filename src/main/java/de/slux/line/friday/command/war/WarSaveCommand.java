/**
 *
 */
package de.slux.line.friday.command.war;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.WarDaoDuplicatedAllianceTagException;
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
public class WarSaveCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "save war";
    private static Logger LOG = LoggerFactory.getLogger(WarSaveCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public WarSaveCommand(LineMessagingClient messagingClient, FridayBotApplication app) {
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
        String allyTag = "";
        try {
            // Get the summary of a specific day
            WarDeathLogic warModel = new WarDeathLogic();
            List<String> args = super.extractArgs(message);

            if (args.size() < 4)
                return new TextMessage("Missing argument. Please specify the opponent alliance tag");

            args.remove(0);
            args.remove(0);
            args.remove(0);
            allyTag = String.join(" ", args);

            warModel.saveWar(senderId, allyTag);
            return new TextMessage("War reports against '" + allyTag + "' saved successfully");
        } catch (WarDaoUnregisteredException e) {
            return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
                    + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
        } catch (WarDaoDuplicatedAllianceTagException e) {
            return new TextMessage("Error: the alliance '" + allyTag
                    + "' has been already registered today. Use the command '" + AbstractCommand.ALL_CMD_PREFIX + " "
                    + WarDeleteCommand.CMD_PREFIX + "' to delete the previeus one");
        } catch (Exception e) {
            LOG.error("Unexpected exception: " + e, e);
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
        sb.append(CMD_PREFIX + " <ally_tag>\n");
        if (verbose) {
            sb.append("Saves the current reports in the archive for future reference.\n");
            sb.append("Use this command only after the war has ended");
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
