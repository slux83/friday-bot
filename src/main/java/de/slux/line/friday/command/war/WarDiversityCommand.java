/**
 * 
 */
package de.slux.line.friday.command.war;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.data.war.WarDeath;
import de.slux.line.friday.data.war.WarDiversity;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.data.war.WarSummonerPlacement;
import de.slux.line.friday.logic.StatsLogic;
import de.slux.line.friday.logic.war.WarDeathLogic;
import de.slux.line.friday.logic.war.WarPlacementLogic;

/**
 * This command is triggered on diversity
 * 
 * @author slux
 */
public class WarDiversityCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "diversity";
	public static final String ARG = "verbose";
	private static Logger LOG = LoggerFactory.getLogger(WarDiversityCommand.class);

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public WarDiversityCommand(LineMessagingClient messagingClient) {
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
		WarPlacementLogic warPlacementModel = new WarPlacementLogic();

		List<String> commandArgs = extractArgs(message);

		// clear up prefix
		commandArgs.remove(0);
		commandArgs.remove(0);

		boolean verbose = (!commandArgs.isEmpty() && commandArgs.get(0).trim().equalsIgnoreCase(ARG));

		try {
			// Needed to create the champion list
			new StatsLogic(true);

			Map<Integer, WarSummoner> placementTable = warPlacementModel.getSummoners(senderId);
			WarGroup deathTable = warModel.getSummaryModel(senderId);

			// build the map of node -> champs
			Map<Integer, List<String>> allReports = new HashMap<>();
			for (Entry<Integer, WarSummoner> entry : placementTable.entrySet()) {
				for (Entry<Character, WarSummonerPlacement> placement : entry.getValue().getPlacements().entrySet()) {
					if (!allReports.containsKey(placement.getValue().getNode())) {
						allReports.put(placement.getValue().getNode(), new ArrayList<>());
					}
					allReports.get(placement.getValue().getNode()).add(placement.getValue().getChampion());
				}
			}

			for (WarDeath deathReport : deathTable.getDeathReports()) {
				if (!allReports.containsKey(deathReport.getNodeNumber())) {
					allReports.put(deathReport.getNodeNumber(), new ArrayList<>());
				}
				allReports.get(deathReport.getNodeNumber()).add(deathReport.getChampName());
			}

			Map<String, String> championsList = FridayBotApplication.getInstance().getChampionsData();
			Map<Integer, WarDiversity> diversityList = new HashMap<>();
			for (Entry<Integer, List<String>> report : allReports.entrySet()) {
				for (String champ : report.getValue()) {
					Entry<Double, String> guess = StatsLogic.guessChampion(champ, championsList);

					String guessedChamp = null;
					if (guess.getKey() >= StatsLogic.CHAMP_MATCHING_THRESHOLD) {
						guessedChamp = guess.getValue();
					}

					if (!diversityList.containsKey(report.getKey())) {
						WarDiversity wd = new WarDiversity(report.getKey(), champ, guessedChamp);
						diversityList.put(report.getKey(), wd);
					} else {
						WarDiversity wd = diversityList.get(report.getKey());

						if (wd.isUnknown()) {
							wd.setNormalizedChampionName(guessedChamp);
						}
					}
				}
			}

			long uniqueChamps = diversityList.values().stream().distinct().count();

			// We exclude the Unknown ones
			if (diversityList.values().contains(new WarDiversity(-1, "", null)))
				uniqueChamps--;

			StringBuilder response = new StringBuilder(
			        "I have calculated " + uniqueChamps + " unique champion(s) placed by the opponent BG.");

			// Add unknown results
			long unknownElements = diversityList.values().stream().filter(WarDiversity::isUnknown).count();
			if (unknownElements > 0) {
				response.append("\nI could not guess the champion name on ");
				response.append(unknownElements);
				response.append(" inserted element(s)");
			}

			// print nice details ChampName : [12,33,45]
			if (verbose) {
				LOG.info("Verbose analysis requested");
				response.append("\n\nDetails:");
				Map<String, ArrayList<Integer>> details = new TreeMap<>();
				for (Entry<Integer, WarDiversity> report : diversityList.entrySet()) {

					// Unknown are processed here
					if (report.getValue().isUnknown()) {
						if (!details.containsKey(report.getValue().getRawInputData())) {
							ArrayList<Integer> nodes = new ArrayList<>();
							nodes.add(report.getKey());
							details.put(report.getValue().getRawInputData(), nodes);
						} else {
							details.get(report.getValue().getRawInputData()).add(report.getKey());
						}
					} else {
						if (!details.containsKey(report.getValue().getNormalizedChampionName())) {
							ArrayList<Integer> nodes = new ArrayList<>();
							nodes.add(report.getKey());
							details.put(report.getValue().getNormalizedChampionName(), nodes);
						} else {
							details.get(report.getValue().getNormalizedChampionName()).add(report.getKey());
						}
					}
				}

				for (Entry<String, ArrayList<Integer>> detail : details.entrySet()) {
					response.append("\n");
					response.append(detail.getKey());
					response.append(" : ");
					response.append(detail.getValue());
				}

				return new TextMessage(response.toString());

			} else {
				return new TextMessage(response.toString() + "\n\nUse '" + ARG + "' argument for more details");
			}

		} catch (WarDaoUnregisteredException e) {
			return new TextMessage("This group is unregistered! Please use '" + AbstractCommand.ALL_CMD_PREFIX + " "
			        + HelpCommand.CMD_PREFIX + "' for info on how to register your chat room");
		} catch (IOException e) {
			LOG.error("Unexpected error: " + e, e);
			return new TextMessage("Sorry, I cannot fetch the champions list: " + e);
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
		sb.append(CMD_PREFIX + " " + ARG + "?\n");
		if (verbose) {
			sb.append("Prints the opponent group diversity analysis based on the input. ");
			sb.append("The 'verbose' argument is optional to get details.\n");
			sb.append("E.g.:\n");
			sb.append(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + "\n");
			sb.append(AbstractCommand.ALL_CMD_PREFIX + " " + CMD_PREFIX + " verbose");
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
