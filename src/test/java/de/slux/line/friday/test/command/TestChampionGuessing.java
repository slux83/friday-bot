/**
 *
 */
package de.slux.line.friday.test.command;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.war.WarHistoryDao;
import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.logic.StatsLogic;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author slux
 */
public class TestChampionGuessing {

    /**
     * Reduce logging level
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) root;
        logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
    }

    @Test
    public void testGuessing() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

        StatsLogic sl = new StatsLogic(true);

        Connection conn = DbConnectionPool.getConnection();
        WarHistoryDao dao = new WarHistoryDao(conn);
        List<HistoryStats> rawStats = dao.getStatsData();

        Set<String> unknown = new HashSet<String>();
        Map<String, List<String>> guessed = new TreeMap<>();
        for (HistoryStats stat : rawStats) {
            String champ = stat.getChamp();

            Entry<Double, String> guess = StatsLogic.guessChampion(champ, sl.getChampions());

            if (guess.getKey() < StatsLogic.CHAMP_MATCHING_THRESHOLD) {
                unknown.add(champ.trim() + " {" + guess.getKey() + "}");
            } else {
                List<String> inputChamps = new ArrayList<>();
                if (guessed.containsKey(guess.getValue())) {
                    inputChamps = guessed.get(guess.getValue());
                } else {
                    guessed.put(guess.getValue(), inputChamps);
                }

                inputChamps.add(champ);
            }
        }

        for (String unk : unknown) {
            System.err.println(unk);
        }
        System.err.flush();
        for (Entry<String, List<String>> guess : guessed.entrySet()) {
            System.err.println(guess.getKey());
            for (String raw : guess.getValue()) {
                System.err.println("\t" + raw);
            }
        }
    }

}
