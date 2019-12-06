/**
 *
 */
package de.slux.line.friday.command.war;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.SummonerNotFoundException;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.logic.war.WarPlacementLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This command adds summoners of the current war
 *
 * @author slux
 */
public class WarSummonerRenameCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "rename summoner";
    private static Logger LOG = LoggerFactory.getLogger(WarSummonerRenameCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public WarSummonerRenameCommand(LineMessagingClient messagingClient) {
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
            // we can use this command only to print the last version
            WarPlacementLogic logic = new WarPlacementLogic();

            List<String> args = extractArgs(message);

            // clear up prefix
            args.remove(0);
            args.remove(0);
            args.remove(0);

            if (args.size() < 2) {
                return new TextMessage("Missing arguments, please use " + AbstractCommand.ALL_CMD_PREFIX + " "
                        + HelpCommand.CMD_PREFIX + " to see the list of commands and arguments");
            }

            String arg1 = args.remove(0).trim();

            int summonerPosition = -1;

            try {
                summonerPosition = Integer.parseInt(arg1);
            } catch (NumberFormatException e) {
                return new TextMessage(
                        "Invalid argument. Expected the position number of the summoner (1 to 10), got " + arg1);
            }

            logic.renameSummoner(senderId, summonerPosition, String.join(" ", args));
            return new TextMessage("Summoner renamed");
        } catch (WarDaoUnregisteredException e) {
            return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
                    + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
        } catch (SummonerNotFoundException e) {
            return new TextMessage(e.getMessage());
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
     * @see de.slux.line.friday.command.AbstractCommand#getHelp(verbose)
     */
    @Override
    public String getHelp(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        sb.append(CMD_PREFIX + " <pos> <name>\n");
        if (verbose) {
            sb.append("Rename the summoner with <name> at position <pos>\n");
            sb.append("Example '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " 6 John Doe'");
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
