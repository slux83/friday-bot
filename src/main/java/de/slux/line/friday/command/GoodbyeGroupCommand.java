/**
 * 
 */
package de.slux.line.friday.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * This command is triggered when the user removes the BOT from a group (join
 * event)
 * 
 * @author slux
 */
public class GoodbyeGroupCommand extends AbstractCommand {
	private static Logger LOG = LoggerFactory.getLogger(GoodbyeGroupCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public GoodbyeGroupCommand(LineMessagingClient messagingClient) {
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
		return LeaveEvent.class.getSimpleName().equals(message);
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
			LOG.info("FRIDAY is leaving the group: " + senderId);

			WarDeathLogic warModel = new WarDeathLogic();
			warModel.updateGroupStatus(senderId, GroupStatus.GroupStatusInactive);
		} catch (Exception e) {
			LOG.error("Erro while setting the status of the group '" + senderId + "'" + this.getClass().getSimpleName(),
			        e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeEvent;
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
