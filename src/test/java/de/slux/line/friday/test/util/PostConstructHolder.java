package de.slux.line.friday.test.util;

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

		int messagesFound = 0;
		while (messagesFound != 2) {
			System.out.println("Waiting for stats to become available...");
			String messages = callback.takeAllMessages();

			if (messages.contains("Groups Inactivity Report"))
				messagesFound++;

			if (messages.contains("War node stats"))
				messagesFound++;

			allMsg.append(messages);
			Thread.sleep(1000);
		}

		System.out.println("Callbacks received");

		return allMsg.toString();
	}
}
