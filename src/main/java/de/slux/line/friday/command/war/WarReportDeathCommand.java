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

import java.util.Arrays;
import java.util.List;

/**
 * This command is triggered on the register command
 *
 * @author slux
 */
public class WarReportDeathCommand extends AbstractCommand {
    public static final String CMD_PREFIX = "death";
    private static Logger LOG = LoggerFactory.getLogger(WarReportDeathCommand.class);

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public WarReportDeathCommand(LineMessagingClient messagingClient, FridayBotApplication app) {
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
        List<String> args = super.extractArgs(message);

        // Clean up prefix
        args.remove(0);
        args.remove(0);

        if (args.size() < 3) {
            return new TextMessage("Incorrect syntax. Please use the following:\n" + AbstractCommand.ALL_CMD_PREFIX
                    + " " + CMD_PREFIX + " <deaths> <node> <champ>");
        }

        // Deal with multi-insert
        String content = String.join(" ", args);
        String[] commands = content.split(",");

        StringBuilder warnings = new StringBuilder();
        String lastSuccess = "";
        for (String command : commands) {
            args = super.extractArgs(command);

            int deaths = -1;
            try {
                deaths = Integer.parseInt(args.get(0).trim());
            } catch (NumberFormatException e) {
                warnings.append("- Number expected for <deaths> but found " + args.get(0).trim() + "\n");
                continue;
            }

            if (deaths < 0)
                deaths = 0;

            int node = -1;
            try {
                node = Integer.parseInt(args.get(1).trim());
            } catch (NumberFormatException e) {
                warnings.append("- Number expected for <node> but found " + args.get(1).trim() + "\n");
                continue;
            }

            args.remove(0);
            args.remove(0);
            String champName = String.join(" ", args.toArray(new String[]{}));
            String userName = super.getUserName(senderId, userId);

            try {
                WarDeathLogic warModel = new WarDeathLogic();
                warModel.addDeath(senderId, deaths, node, champName.trim(), userName);
                lastSuccess = warModel.getReport(senderId);
            } catch (WarDaoUnregisteredException e) {
                return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
                        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
            } catch (Exception e) {
                LOG.error("Unexpected error " + e, e);
                return new TextMessage("Unexpected error: " + e);
            }
        }

        if (warnings.length() > 0) {
            warnings.insert(0, "\n\nWarnings:\n");
            warnings.insert(0, lastSuccess);
            return new TextMessage(warnings.toString());
        }

        return new TextMessage(lastSuccess);

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
        sb.append(CMD_PREFIX + " <deaths> <node> <champ>, ...\n");
        if (verbose) {
            sb.append("Report new death(s) in AW\n");
            sb.append("Example '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " 2 45 Medusa'");
            sb.append("Example multi-insert '" + AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX
                    + " 2 45 Medusa, 1 55 Dormammu'");
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
