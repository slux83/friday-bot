/**
 *
 */
package de.slux.line.friday.test.command;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.war.*;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @author slux
 */
public class TestDiversityCommand {

    @Test
    public void testWarDiversityCommand() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

        String groupId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // Register command
        MessageEvent<TextMessageContent> registerCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarRegisterCommand.CMD_PREFIX + " group1");

        // Report death command with multi-insert
        MessageEvent<TextMessageContent> deathMultiInsertCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
                userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarReportDeathCommand.CMD_PREFIX
                        + " 2 55 5* Dormammu someSummoner,"
                        + " 1 24 6* Sparky slux,"
                        + "4 28 5* Dorma blasto55    , "
                        + " 1 11 5* Domino slux83,\n\t"
                        + " hello, "
                        + " 2 14 5* aquaman,"
                        + " 2 15 5* badone, "
                        + " 0 [ test broken");

        // Summoner placement command
        MessageEvent<TextMessageContent> summonersAddCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
                userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX
                        + " slux83, John Doe, Fool, Jones, Samantha");

        // Summoner placement command
        MessageEvent<TextMessageContent> summonersCompactSummaryCmd = MessageEventUtil.createMessageEventGroupSource(
                groupId, userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarAddSummonersCommand.CMD_PREFIX + " compact");

        // Summoner node
        MessageEvent<TextMessageContent> summonerNodesCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
                userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarSummonerNodeCommand.CMD_PREFIX
                        + " 1a 28 5* Dormammu,"
                        + " 1b 55 5* dupe IMIW,"
                        + " 1c 54 5* Abomination,"
                        + " 1d 36 5* Dr Voodoo,"
                        + " 2a 24 5* dupe Storm,"
                        + " 2b 15 5* dupe Scarlet,"
                        + " 3a 35 anotherfake");

        // Diversity command
        MessageEvent<TextMessageContent> diversityCmd = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                AbstractCommand.ALL_CMD_PREFIX + " " + WarDiversityCommand.CMD_PREFIX);

        MessageEvent<TextMessageContent> diversityVerboseCmd = MessageEventUtil.createMessageEventGroupSource(groupId,
                userId, AbstractCommand.ALL_CMD_PREFIX + " " + WarDiversityCommand.CMD_PREFIX + " verbose");

        /* Start the workflow */
        TextMessage response = friday.handleTextMessageEvent(registerCmd);
        assertTrue(response.getText().contains("successfully registered using the name group1"));

        response = friday.handleTextMessageEvent(diversityVerboseCmd);
        assertTrue(response.getText().contains("nothing found"));

        response = friday.handleTextMessageEvent(deathMultiInsertCmd);
        assertTrue(response.getText().contains("880"));
        assertTrue(response.getText().contains("12"));
        assertTrue(response.getText().contains("6/55"));
        assertTrue(response.getText().contains("Warnings"));
        assertTrue(response.getText().contains("found hello"));
        assertTrue(response.getText().contains("found ["));

        response = friday.handleTextMessageEvent(summonersAddCmd);
        assertTrue(response.getText().contains("5 new"));

        response = friday.handleTextMessageEvent(summonerNodesCmd);
        assertTrue(response.getText().contains("slux83"));
        assertTrue(response.getText().contains("5* dupe Scarlet (15)"));

        response = friday.handleTextMessageEvent(diversityCmd);
        assertTrue(response.getText().contains("7/9"));

        response = friday.handleTextMessageEvent(diversityVerboseCmd);
        System.err.println(response);
        assertTrue(response.getText().contains("Abomination : [54]"));
        assertTrue(response.getText().contains("Domino : [11]"));
        assertTrue(response.getText().contains("aquaman : [14]"));
        assertTrue(response.getText().contains("anotherfake : [35]"));

        response = friday.handleTextMessageEvent(summonersCompactSummaryCmd);
        System.err.println(response.getText());
        assertTrue(callback.takeAllMessages().isEmpty());
        assertTrue(response.getText().contains("slux83"));
        assertTrue(response.getText().contains("Samantha"));
        assertTrue(response.getText().contains("15. 5* dupe Scarlet"));
        assertTrue(response.getText().contains("35. anotherfake"));

    }

}
