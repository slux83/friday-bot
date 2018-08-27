/**
 * 
 */
package de.slux.line.friday.test.util;

import java.time.Instant;
import java.util.UUID;

import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;

/**
 * @author slux
 */
public class MessageEventUtil {

	/**
	 * Private ctor
	 */
	private MessageEventUtil() {
	}

	/**
	 * Create a message event
	 * 
	 * @param groupId
	 * @param userId
	 * @param message
	 * @return message event of type {@link TextMessageContent}
	 */
	public static MessageEvent<TextMessageContent> createMessageEvent(String groupId, String userId, String message) {

		Source source = new GroupSource(groupId, userId);
		Instant timestamp = Instant.now();

		TextMessageContent messageText = new TextMessageContent(UUID.randomUUID().toString(), message);
		return new MessageEvent<TextMessageContent>(UUID.randomUUID().toString(), source, messageText, timestamp);
	}

	/**
	 * Create a join event
	 * 
	 * @param groupId
	 * @param userId
	 * @return the event of type {@link JoinEvent}
	 */
	public static JoinEvent createJoinEvent(String groupId, String userId) {
		Instant timestamp = Instant.now();
		Source source = new GroupSource(groupId, userId);
		return new JoinEvent(UUID.randomUUID().toString(), source, timestamp);
	}

}
