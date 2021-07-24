package de.slux.line.friday.test.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import de.slux.line.friday.command.AbstractCommand;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AbstractCommandTest {

    @Test
    public void testLongMessageTruncate() throws Exception {
        MyCommand mc = new MyCommand(null);

        Assert.assertTrue(mc.execute("", "", "ABCDEFGHILMNO").getText().endsWith("..."));

        Assert.assertFalse(mc.execute("", "", "A").getText().endsWith("..."));
    }

    public class MyCommand extends AbstractCommand {

        public MyCommand(LineMessagingClient messagingClient) {
            super(messagingClient, null);
        }

        @Override
        public boolean canTrigger(String message) {
            return false;
        }

        @Override
        public String getCommandPrefix() {
            return null;
        }

        @Override
        public TextMessage execute(String userId, String senderId, String message) {
            List<String> messages = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                messages.add(message + " " + i + "\n");
            }

            try {
                return new TextMessage(super.pushMultipleMessages2(senderId, "hello", messages));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
