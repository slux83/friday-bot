/**
 *
 */
package de.slux.line.friday.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

/**
 * This command does nothing
 *
 * @author slux
 */
public class DefaultCommand extends AbstractCommand {

    /**
     * Ctor
     *
     * @param messagingClient
     */
    public DefaultCommand(LineMessagingClient messagingClient) {
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
        return false;
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
        return null;
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
