package de.slux.line.friday.test.util;

import de.slux.line.friday.FridayBotApplication;

import java.util.Calendar;

/**
 * Utility class to wait startup and scheduled activities to be done
 *
 * @author slux
 */
public final class PostConstructHolder {

    /**
     * Private ctor
     */
    private PostConstructHolder() {

    }

    /**
     * Blocking call to wait for callback to be called by scheduled activities
     *
     * @param callback
     * @throws Exception
     */
    public static String waitForPostConstruct(MessagingClientCallbackImpl callback) throws Exception {

        StringBuilder allMsg = new StringBuilder();

        Calendar c = Calendar.getInstance();
        int messagesToBeFound = 1;

        if (!FridayBotApplication.getInstance().getIsOperational().get())
            messagesToBeFound = 1;

        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            messagesToBeFound = 0;

        if (c.get(Calendar.HOUR_OF_DAY) >= 10 && c.get(Calendar.HOUR_OF_DAY) < 11)
            messagesToBeFound = 1;

        for (int i = 0; i < 10; i++) {
            if (messagesToBeFound == 0) break;
            System.out.println("Waiting for stats to become available...");
            String messages = callback.takeAllMessages();

            //if (messages.contains("Groups Inactivity Report"))
            //    messagesToBeFound--;

            if (messages.contains("War node stats"))
                messagesToBeFound--;

            allMsg.append(messages);
            Thread.sleep(1000);
        }

        if (messagesToBeFound > 0)
            System.err.println("given up waiting for the initialization: " + messagesToBeFound);
        else
            System.out.println("Callbacks received");

        return allMsg.toString();
    }
}
