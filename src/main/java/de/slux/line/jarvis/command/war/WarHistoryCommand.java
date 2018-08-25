/**
 * 
 */
package de.slux.line.jarvis.command.war;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.dao.WarDaoUnregisteredException;
import de.slux.line.jarvis.data.war.WarGroup;
import de.slux.line.jarvis.logic.war.WarReportModel;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarHistoryCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis history";
	private static Logger LOG = LoggerFactory.getLogger(WarHistoryCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarHistoryCommand(LineMessagingClient messagingClient) {
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
		if (message.equalsIgnoreCase(CMD_PREFIX)) {
			// Get all history
			try {
				List<String> history = warModel.getHistoryText(senderId);
				for (String h : history) {
					PushMessage pushMessage = new PushMessage(senderId, new TextMessage(h));
					super.messagingClient.pushMessage(pushMessage).get();
				}
			} catch (WarDaoUnregisteredException e) {
				return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
				        + "' for info on how to register your chat room");
			} catch (Exception e) {
				LOG.error("Unexpected error: " + e, e);
				return new TextMessage("Unexpected error: " + e);
			}
		} else {
			// Get the summary of a specific day
			List<String> argsAsList = super.extractArgs(message);

			String day = argsAsList.get(2);
			try {
				Date warDate = WarReportModel.SDF.parse(day.trim());

				Map<String, WarGroup> history = warModel.getHistorySummary(senderId, warDate);

				if (history.isEmpty())
					return new TextMessage("Nothing found for " + day);

				for (Entry<String, WarGroup> historyEntry : history.entrySet()) {
					List<String> summaryText = historyEntry.getValue().getSummaryText();
					// We gotta push multiple messages
					PushMessage pushMessage = new PushMessage(senderId, new TextMessage(
					        "*** " + day.trim() + " - " + historyEntry.getKey() + " ***\n\n" + summaryText.get(0)));
					CompletableFuture<BotApiResponse> outcome = super.messagingClient.pushMessage(pushMessage);
					outcome.get();

					for (int i = 1; i < summaryText.size(); i++) {
						pushMessage = new PushMessage(senderId, new TextMessage(summaryText.get(i)));
						super.messagingClient.pushMessage(pushMessage).get();
					}
				}

			} catch (ParseException e) {
				return new TextMessage("Incorrect date syntax.\nPlease use the following date pattern: "
				        + WarReportModel.SDF.toPattern());
			} catch (WarDaoUnregisteredException e) {
				return new TextMessage("This group is unregistered! Please use '" + HelpCommand.CMD_PREFIX
				        + "' for info on how to register your chat room");
			} catch (Exception e) {
				LOG.error("Unexpected error: " + e, e);
				return new TextMessage("Unexpected error: " + e);
			}
		}

		return null;
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
		sb.append("[" + CMD_PREFIX + " <date?>]\n");
		sb.append("Prints all the saved wars or a specific one, if <date> is provided.\n");
		sb.append("Date format is yyyy-MM-dd e.g. 2018-05-24");

		return sb.toString();
	}
}
