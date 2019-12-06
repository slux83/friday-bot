/**
 *
 */
package de.slux.line.friday.logic.war;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarSummonerDao;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.data.war.WarSummonerPlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

/**
 * War placement logic to save the various nodes during war
 *
 * @author slux
 */
public class WarPlacementLogic {
    public static final int MAX_SUMMONERS = 10;
    private static Logger LOG = LoggerFactory.getLogger(WarPlacementLogic.class);

    /**
     * Ctor
     */
    public WarPlacementLogic() {
    }

    /**
     * Add a list of summoners in the current placement schema
     *
     * @param groupId
     * @param summoners
     * @throws Exception
     */
    public void addSummoners(String groupId, List<String> summoners) throws Exception {
        int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarSummonerDao dao = new WarSummonerDao(conn);

        dao.storeData(groupKey, summoners);
    }

    /**
     * Get all summoners
     *
     * @param groupId
     * @return the map of summoners
     * @throws Exception
     */
    public Map<Integer, WarSummoner> getSummoners(String groupId) throws Exception {
        int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarSummonerDao dao = new WarSummonerDao(conn);

        return dao.getAll(groupKey);
    }

    /**
     * Rename summoner
     *
     * @param groupId
     * @param summonerPos
     * @param name
     * @throws Exception
     */
    public void renameSummoner(String groupId, Integer summonerPos, String name) throws Exception {
        int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarSummonerDao dao = new WarSummonerDao(conn);

        dao.renameSummoner(groupKey, summonerPos, name);
    }

    /**
     * Edit a placement
     *
     * @param groupId
     * @param summonerPos
     * @param placementPos
     * @param node
     * @param champ
     * @throws Exception
     */
    public void editPlacement(String groupId, Integer summonerPos, Character placementPos, Integer node, String champ)
            throws Exception {
        int groupKey = WarDeathLogic.checkGroupRegistration(groupId);

        Connection conn = DbConnectionPool.getConnection();

        LOG.debug("Connection to the DB valid");

        WarSummonerDao dao = new WarSummonerDao(conn);

        dao.editPlacement(groupKey, summonerPos, placementPos, node, champ);
    }

    /**
     * Stringify the map of summoners for printing/posting.
     * <p>
     * Split the message in multiple ones if bigger than
     * {@link FridayBotApplication#MAX_LINE_MESSAGE_SIZE}
     * </p>
     *
     * @param summoners
     * @return the string(s) for the map in a compact view
     */
    public static List<String> getSummonersCompactText(Map<Integer, WarSummoner> summoners) {
        List<String> outcome = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Entry<Integer, WarSummoner> entry : summoners.entrySet()) {
            if (sb.length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
                outcome.add(sb.toString());
                sb.setLength(0);
            }

            sb.append(entry.getValue().getName());
            sb.append("\n");

            List<WarSummonerPlacement> placementPlan = new ArrayList<>(entry.getValue().getPlacements().values());

            // Sort summoner placement by node
            Collections.sort(placementPlan, new Comparator<WarSummonerPlacement>() {

                @Override
                public int compare(WarSummonerPlacement o1, WarSummonerPlacement o2) {
                    return o1.getNode() - o2.getNode();
                }
            });

            for (WarSummonerPlacement placement : placementPlan) {
                if (placement.getChampion() != null && placement.getNode() > 0) {
                    sb.append(" ");
                    sb.append(placement.getNode().toString());
                    sb.append(". ");
                    sb.append(placement.getChampion());
                    sb.append("\n");
                }
            }

            sb.append("\n");
        }

        if (summoners.isEmpty()) {
            sb.append("Nothing to report");
        } else {
            // Add the amount of reported nodes
            Set<Integer> reportedNodes = new HashSet<>();

            for (WarSummoner ws : summoners.values()) {
                for (WarSummonerPlacement placement : ws.getPlacements().values()) {
                    if (placement.getNode() > 0 && placement.getNode() <= WarGroup.TOTAL_AW_NODES)
                        reportedNodes.add(placement.getNode());
                }
            }

            sb.append("Reported Nodes: " + reportedNodes.size() + "/" + WarGroup.TOTAL_AW_NODES);

            if (reportedNodes.size() >= WarGroup.TOTAL_AW_NODES - 10
                    && reportedNodes.size() < WarGroup.TOTAL_AW_NODES) {
                // We show the missing nodes if the alliance goes hardcore
                List<Integer> missingNodes = new ArrayList<>();
                for (int i = 1; i <= WarGroup.TOTAL_AW_NODES; i++) {
                    if (!reportedNodes.contains(i))
                        missingNodes.add(i);
                }

                sb.append("\nNodes to report: " + missingNodes);
            }
        }

        outcome.add(sb.toString());

        return outcome;
    }

    /**
     * Stringify the map of summoners for printing/posting.
     * <p>
     * Split the message in multiple ones if bigger than
     * {@link FridayBotApplication#MAX_LINE_MESSAGE_SIZE}
     * </p>
     *
     * @param summoners
     * @return the string(s) for the map
     */
    public static List<String> getSummonersText(Map<Integer, WarSummoner> summoners) {
        List<String> outcome = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Entry<Integer, WarSummoner> entry : summoners.entrySet()) {
            if (sb.length() > FridayBotApplication.MAX_LINE_MESSAGE_SIZE) {
                outcome.add(sb.toString());
                sb.setLength(0);
            }

            sb.append(entry.getKey().toString());
            sb.append(". ");
            sb.append(entry.getValue().getName());
            sb.append("\n");

            for (Entry<Character, WarSummonerPlacement> placement : entry.getValue().getPlacements().entrySet()) {
                sb.append("  ");
                sb.append(placement.getKey().toString());
                sb.append(". ");
                if (placement.getValue().getChampion() != null && placement.getValue().getNode() > 0) {
                    sb.append(placement.getValue().getChampion());
                    sb.append(" (");
                    sb.append(placement.getValue().getNode().toString());
                    sb.append(")\n");
                } else {
                    sb.append("\n");
                }
            }

            sb.append("\n");
        }

        if (summoners.isEmpty()) {
            sb.append("Nothing to report");
        } else {
            // Add the amount of reported nodes
            Set<Integer> reportedNodes = new HashSet<>();

            for (WarSummoner ws : summoners.values()) {
                for (WarSummonerPlacement placement : ws.getPlacements().values()) {
                    if (placement.getNode() > 0 && placement.getNode() <= WarGroup.TOTAL_AW_NODES)
                        reportedNodes.add(placement.getNode());
                }
            }

            sb.append("Reported Nodes: " + reportedNodes.size() + "/" + WarGroup.TOTAL_AW_NODES);

            if (reportedNodes.size() >= WarGroup.TOTAL_AW_NODES - 10
                    && reportedNodes.size() < WarGroup.TOTAL_AW_NODES) {
                // We show the missing nodes if the alliance goes hardcore
                List<Integer> missingNodes = new ArrayList<>();
                for (int i = 1; i <= WarGroup.TOTAL_AW_NODES; i++) {
                    if (!reportedNodes.contains(i))
                        missingNodes.add(i);
                }

                sb.append("\nNodes to report: " + missingNodes);
            }
        }

        outcome.add(sb.toString());

        return outcome;
    }

    /**
     * CSV Export of the map of summoners for posting.
     *
     * @param summoners
     * @return the string of the export
     */
    public static String getSummonersTextCsv(Map<Integer, WarSummoner> summoners) {
        StringBuilder sb = new StringBuilder("Summoner,Node,Champion\n");
        for (Entry<Integer, WarSummoner> entry : summoners.entrySet()) {

            for (Entry<Character, WarSummonerPlacement> placement : entry.getValue().getPlacements().entrySet()) {
                sb.append(entry.getValue().getName());
                if (placement.getValue().getChampion() != null && placement.getValue().getNode() > 0) {
                    sb.append(",");
                    sb.append(placement.getValue().getNode().toString());
                    sb.append(",");
                    sb.append(placement.getValue().getChampion());
                    sb.append("\n");
                } else {
                    sb.append(",,\n");
                }
            }
        }

        return sb.toString();
    }

}
