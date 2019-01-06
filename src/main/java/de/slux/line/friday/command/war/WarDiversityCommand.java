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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.dao.exception.WarDaoUnregisteredException;
import de.slux.line.friday.data.war.WarDeath;
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

		try {
			StatsLogic sl = new StatsLogic(true);

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

			// XXX 
			// TODO: scan the map and guess the champ
			// TODO: build the diversity analysis and log mismatches between
			// deaths and summoner table if any (same node, different champ
			// guessed)

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
		sb.append(CMD_PREFIX + " verbose?\n");
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
