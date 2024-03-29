/**
 * 
 */
package de.slux.line.friday.command.war;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.csv.PastebinUtil;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.logic.war.WarDeathLogic;
import de.slux.line.friday.logic.war.WarPlacementLogic;

/**
 * This command is triggered on the register command
 * 
 * @author slux
 */
public class WarHistoryCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "history";
	public static final String CMD_ARG_EXPORT = "export";
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
		WarDeathLogic warModel = new WarDeathLogic();
		if (message.equalsIgnoreCase(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX)) {
			// Get all history
			try {
				List<String> history = warModel.getHistoryText(senderId);
				return super.pushMultipleMessages(senderId, "", history);
			} catch (WarDaoUnregisteredException e) {
				return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
				        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
			} catch (Exception e) {
				LOG.error("Unexpected error: " + e, e);
				return new TextMessage("Unexpected error: " + e);
			}
		} else {
			// Get the summary of a specific day (export in case)
			List<String> argsAsList = super.extractArgs(message);
			boolean export = argsAsList.stream().anyMatch(s -> s.equalsIgnoreCase(CMD_ARG_EXPORT));

			// Cleanup
			argsAsList.removeIf(s -> s.equalsIgnoreCase(CMD_ARG_EXPORT));

			String day = argsAsList.get(2);
			try {
				Date warDate = WarDeathLogic.SDF.parse(day.trim());

				Map<String, WarGroup> historyDeaths = warModel.getHistorySummaryForDeaths(senderId, warDate);
				Map<String, Map<Integer, WarSummoner>> historyPlacement = warModel.getHistorySummaryForReports(senderId,
				        warDate);

				if (!export) {
					if (historyDeaths.isEmpty()) {
						PushMessage pushMessage = new PushMessage(senderId,
						        new TextMessage("No death reports found for " + day));
						super.messagingClient.pushMessage(pushMessage).get();
					}

					if (historyPlacement.isEmpty()) {
						PushMessage pushMessage = new PushMessage(senderId,
						        new TextMessage("No placement reports found for " + day));
						super.messagingClient.pushMessage(pushMessage).get();
					}

					for (Entry<String, WarGroup> historyEntry : historyDeaths.entrySet()) {
						List<String> summaryText = historyEntry.getValue().getSummaryText();
						super.pushMultipleMessages(senderId,
						        "*** " + day.trim() + " - " + historyEntry.getKey() + " ***\n\n", summaryText, true);
					}

					for (Entry<String, Map<Integer, WarSummoner>> placements : historyPlacement.entrySet()) {
						String allyTag = placements.getKey();
						Map<Integer, WarSummoner> placementTable = placements.getValue();
						List<String> text = WarPlacementLogic.getSummonersText(placementTable);
						super.pushMultipleMessages(senderId, "*** " + day.trim() + " - " + allyTag + " ***\n\n", text,
						        true);
					}
				} else {
					// Export data
					super.pushMultipleMessages(senderId, "", Arrays.asList("Exporting data..."), true);

					Map<String, String> postedUrls = new HashMap<>();
					for (Entry<String, WarGroup> historyEntry : historyDeaths.entrySet()) {
						String summaryTextCsv = historyEntry.getValue().getSummaryTextCsv();
						String title = day.trim() + " - " + historyEntry.getKey() + " (deaths)";

						String url = PastebinUtil.pasteData(title, summaryTextCsv);
						postedUrls.put(title, url);
					}

					for (Entry<String, Map<Integer, WarSummoner>> placements : historyPlacement.entrySet()) {
						String allyTag = placements.getKey();
						Map<Integer, WarSummoner> placementTable = placements.getValue();
						String text = WarPlacementLogic.getSummonersTextCsv(placementTable);
						String title = day.trim() + " - " + allyTag + " (placements)";

						String url = PastebinUtil.pasteData(title, text);
						postedUrls.put(title, url);
					}

					StringBuilder sb = new StringBuilder(
					        "The following files have been created as CSV export (expires in 10 min):\n");
					if (postedUrls.isEmpty())
						sb.append("None");

					for (Map.Entry<String, String> entry : postedUrls.entrySet()) {
						sb.append(entry.getKey());
						sb.append(": ");
						sb.append(entry.getValue());
						sb.append("\n");
					}

					return new TextMessage(sb.toString());
				}

			} catch (ParseException e) {
				return new TextMessage("Incorrect date syntax.\nPlease use the following date pattern: "
				        + WarDeathLogic.SDF.toPattern());
			} catch (WarDaoUnregisteredException e) {
				return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
				        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
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
		sb.append(CMD_PREFIX + " <" + CMD_ARG_EXPORT + "?> <date?>\n");
		if (verbose) {
			sb.append("Prints all the saved wars or a specific one, if <date> is provided.\n");
			sb.append("Use " + CMD_ARG_EXPORT + " argument to export as CVS\n");
			sb.append("Date format is yyyy-MM-dd e.g. 2018-05-24");
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
