/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.WarDaoUnregisteredException;
import de.slux.line.jarvis.war.WarReportModel;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarDeleteCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis delete war";
	private static Logger LOG = LoggerFactory.getLogger(WarDeleteCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarDeleteCommand(LineMessagingClient messagingClient) {
		super(messagingClient);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.jarvis.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		WarReportModel warModel = new WarReportModel();
		// Get the summary of a specific day
		List<String> argsAsList = super.extractArgs(message);

		if (argsAsList.size() < 5)
			return new TextMessage("Missing arguments, please specify the date and opponent alliance tag");

		String allyTag = "";
		String day = argsAsList.get(3).trim();
		try {
			Date warDate = WarReportModel.SDF.parse(day);

			// In case the ally tag has blanks
			StringBuilder allyTagBuilder = new StringBuilder();
			for (int i = 4; i < argsAsList.size(); ++i) {
				allyTagBuilder.append(argsAsList.get(i));
				allyTagBuilder.append(" ");
			}
			allyTag = allyTagBuilder.toString().trim();

			boolean outcome = warModel.deleteHistoryEntries(senderId, allyTag, warDate);

			if (outcome) {
				return new TextMessage(
						"War against '" + allyTag + "' on " + day + " has been deleted from the history");
			} else {
				return new TextMessage(
						"Could not find any war against '" + allyTag + "' on " + day + ". Please try again");
			}
		} catch (ParseException e) {
			return new TextMessage(
					"Incorrect date syntax.\nPlease use the following pattern: " + WarReportModel.SDF.toPattern());
		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
					+ "' for info on how to register your chat room");
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Unexpected error: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeWar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + " <date> <ally_tag>]\n");
		sb.append("Delete from the history a given alliance war for a given date.\n");
		sb.append("Date format is yyyy-MM-dd e.g. 2018-05-24\n");
		sb.append("Example " + CMD_PREFIX + " 2018-05-24 4Loki");

		return sb.toString();
	}
}
