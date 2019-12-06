package de.slux.line.friday.test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.test.util.LineMessagingClientMock;
import de.slux.line.friday.test.util.MessageEventUtil;
import de.slux.line.friday.test.util.MessagingClientCallbackImpl;
import de.slux.line.friday.test.util.PostConstructHolder;
import org.junit.Test;

import java.util.UUID;

/**
 * @author slux
 */
public class CommandDistanceTest {

    @Test
    public void testDistance() throws Exception {
        MessagingClientCallbackImpl callback = new MessagingClientCallbackImpl();
        FridayBotApplication friday = new FridayBotApplication(null);
        friday.setLineMessagingClient(new LineMessagingClientMock(callback));
        friday.postConstruct();

        PostConstructHolder.waitForPostConstruct(callback);

        String groupId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // some commands with typos and mistakes
        MessageEvent<TextMessageContent> cmd1 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday register group1");
        MessageEvent<TextMessageContent> cmd2 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday dead 2 55 5* dupe Dormammu");
        MessageEvent<TextMessageContent> cmd3 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday kill 1 24 6* NC");
        MessageEvent<TextMessageContent> cmd4 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday deat 4 28 5* dupe KP");
        MessageEvent<TextMessageContent> cmd5 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday summorer slux83, John Doe, Nemesis The Best, Tony 88");
        MessageEvent<TextMessageContent> cmd6 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday undo");
        MessageEvent<TextMessageContent> cmd7 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday war save 4Loki");
        MessageEvent<TextMessageContent> cmd8 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday war 4Loki");
        MessageEvent<TextMessageContent> cmd9 = MessageEventUtil.createMessageEventGroupSource(groupId, userId,
                "friday save 4Loki");

        TextMessage response = friday.handleTextMessageEvent(cmd1);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd2);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd3);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd4);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd5);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd6);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd7);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd8);
        System.out.println(response);

        response = friday.handleTextMessageEvent(cmd9);
        System.out.println(response);

    }
}
