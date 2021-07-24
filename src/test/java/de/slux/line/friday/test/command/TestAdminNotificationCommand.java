package de.slux.line.friday.test.command;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.admin.AdminNotificationCommand;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestAdminNotificationCommand {

    @Test
    public void testNotificationCommand() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

        // Admin notification command
        MessageEvent<TextMessageContent> adminNotificationWithMessage1Cmd = MessageEventUtil.createMessageEventUserSource(
                friday.getBotOwnerLineId(),
                AbstractCommand.ALL_CMD_PREFIX + " " + AdminNotificationCommand.CMD_PREFIX + " This is a nice notification");
        MessageEvent<TextMessageContent> adminNotificationWithNoMessageCmd = MessageEventUtil.createMessageEventUserSource(
                friday.getBotOwnerLineId(),
                AbstractCommand.ALL_CMD_PREFIX + " " + AdminNotificationCommand.CMD_PREFIX);

        // User messages
        List<MessageEvent<TextMessageContent>> userMessages = generateUserMessages();

        // User commands
        MessageEvent<TextMessageContent> helpCommandUserCmd = MessageEventUtil.createMessageEventUserSource(
                "U_" + UUID.randomUUID().toString(),
                AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

        MessageEvent<TextMessageContent> helpCommandGroupCmd = MessageEventUtil.createMessageEventGroupSource(
                "G_" + UUID.randomUUID().toString(),
                "U_" + UUID.randomUUID().toString(),
                AbstractCommand.ALL_CMD_PREFIX + " " + HelpCommand.CMD_PREFIX);

        /* Begin */
        TextMessage response = friday.handleTextMessageEvent(adminNotificationWithNoMessageCmd);
        assertNotNull(response);
        assertEquals("No notification found.\n", response.getText());
        assertTrue(callback.takeAllMessages().isEmpty());

        // Fire a set of random messages
        for (MessageEvent<TextMessageContent> userMessage : userMessages) {
            response = friday.handleTextMessageEvent(userMessage);
            assertNull(response);
        }

        // Fire admin command
        response = friday.handleTextMessageEvent(adminNotificationWithNoMessageCmd);
        assertNotNull(response);
        assertEquals("No notification found.\n", response.getText());
        assertTrue(callback.takeAllMessages().isEmpty());

        // Fire first notification
        response = friday.handleTextMessageEvent(adminNotificationWithMessage1Cmd);
        assertNotNull(response);
        assertTrue(response.getText().contains("Notification saved\n"));
        assertTrue(callback.takeAllMessages().isEmpty());

        // Fire a set of random messages
        int notificationCounterForUsers = 0;
        int notificationCounterForGroups = 0;
        int notificationCounterForRooms = 0;
        for (MessageEvent<TextMessageContent> userMessage : userMessages) {
            response = friday.handleTextMessageEvent(userMessage);
            System.out.println("MESSAGE=" + userMessage.getMessage().getText() + " RESP=" + response);

            if (response != null && userMessage.getMessage().getText().contains("group")) {
                // Handle group message
                notificationCounterForGroups++;
            } else if (response != null && userMessage.getMessage().getText().contains("room")) {
                // Handle room message
                notificationCounterForRooms++;
            } else if (response != null && userMessage.getMessage().getText().contains("private")) {
                // Handle user message
                notificationCounterForUsers++;
            } else {
                assertNull(response);
            }
        }

        assertEquals(2, notificationCounterForUsers); // Despite 4 messages to the bot
        assertEquals(2, notificationCounterForGroups); // 2 groups with 2 users
        assertEquals(1, notificationCounterForRooms); // Just one room with 2 users

        // Fire friday messages with friday commands (private)
        response = friday.handleTextMessageEvent(helpCommandUserCmd);
        assertNotNull(response);
        assertTrue(response.getText().contains("This is a nice notification\n"));
        assertTrue(response.getText().contains("F.R.I.D.A.Y. HELP"));
        assertTrue(callback.takeAllMessages().isEmpty());
        response = friday.handleTextMessageEvent(helpCommandUserCmd);
        assertTrue(response.getText().contains("F.R.I.D.A.Y. HELP"));
        assertFalse(response.getText().contains("This is a nice notification\n"));
        assertTrue(callback.takeAllMessages().isEmpty());

        // Fire friday messages with friday commands (group)
        response = friday.handleTextMessageEvent(helpCommandGroupCmd);
        assertNotNull(response);
        assertTrue(response.getText().contains("This is a nice notification\n"));
        assertTrue(response.getText().contains("F.R.I.D.A.Y. HELP"));
        assertTrue(callback.takeAllMessages().isEmpty());
        response = friday.handleTextMessageEvent(helpCommandGroupCmd);
        assertTrue(response.getText().contains("F.R.I.D.A.Y. HELP"));
        assertFalse(response.getText().contains("This is a nice notification\n"));
        assertTrue(callback.takeAllMessages().isEmpty());

        // Fire admin command
        response = friday.handleTextMessageEvent(adminNotificationWithNoMessageCmd);
        assertNotNull(response);
        assertTrue(response.getText().contains("This is a nice notification"));
        assertTrue(response.getText().contains("4 user(s)"));
        assertTrue(response.getText().contains("3 group(s)"));
        assertTrue(response.getText().contains("1 room(s)"));
        assertTrue(response.getText().contains("0 unknown(s)"));
        assertTrue(callback.takeAllMessages().isEmpty());
    }

    private List<MessageEvent<TextMessageContent>> generateUserMessages() {
        String group1Id = "G_" + UUID.randomUUID().toString();
        String user1Id = "U_" + UUID.randomUUID().toString();
        String room1Id = "R_" + UUID.randomUUID().toString();
        String group2Id = "G_" + UUID.randomUUID().toString();
        String user2Id = "U_" + UUID.randomUUID().toString();

        List<MessageEvent<TextMessageContent>> messages = new ArrayList<>();

        messages.add(MessageEventUtil.createMessageEventUserSource(user1Id, "Hello World from private 1.A"));
        messages.add(MessageEventUtil.createMessageEventUserSource(user1Id, "Hello World from private 1.B"));
        messages.add(MessageEventUtil.createMessageEventUserSource(user2Id, "Hello World from private 2.A"));
        messages.add(MessageEventUtil.createMessageEventUserSource(user2Id, "Hello World from private 2.B"));
        messages.add(MessageEventUtil.createMessageEventGroupSource(group1Id, user1Id, "Hello World from 1 in the group 1"));
        messages.add(MessageEventUtil.createMessageEventGroupSource(group1Id, user2Id, "Hello World from 2 in the group 1"));
        messages.add(MessageEventUtil.createMessageEventGroupSource(group2Id, user1Id, "Hello World from 1 in the group 2"));
        messages.add(MessageEventUtil.createMessageEventGroupSource(group2Id, user2Id, "Hello World from 2 in the group 2"));
        messages.add(MessageEventUtil.createMessageEventRoomSource(room1Id, user1Id, "Hello World from 1 in the room 1"));
        messages.add(MessageEventUtil.createMessageEventRoomSource(room1Id, user2Id, "Hello World from 2 in the room 1"));

        return messages;
    }
}
