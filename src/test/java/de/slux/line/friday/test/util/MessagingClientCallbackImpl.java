/**
 * 
 */
package de.slux.line.friday.test.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author slux
 */
public class MessagingClientCallbackImpl implements MessagingClientCallback {

	private List<String> messages;

	public MessagingClientCallbackImpl() {
		this.messages = new CopyOnWriteArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.test.util.MessagingClientCallback#
	 * pushMessageGenerated(java.lang.String)
	 */
	@Override
	public void pushMessageGenerated(String message) {
		this.messages.add(message);
	}

	/**
	 * Returns all the messages and clear them up
	 * 
	 * @return all messages
	 */
	public String takeAllMessages() {
		String allMessages = String.join("/n", this.messages);
		this.messages.clear();

		return allMessages;
	}
}
