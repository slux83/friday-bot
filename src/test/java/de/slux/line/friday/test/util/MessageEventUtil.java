/**
 *
 */
package de.slux.line.friday.test.util;

import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;

import java.time.Instant;
import java.util.UUID;

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
     * Create a message event with {@link GroupSource}
     *
     * @param groupId
     * @param userId
     * @param message
     * @return message event of type {@link TextMessageContent}
     */
    public static MessageEvent<TextMessageContent> createMessageEventGroupSource(String groupId, String userId,
                                                                                 String message) {

        Source source = new GroupSource(groupId, userId);
        Instant timestamp = Instant.now();

        TextMessageContent messageText = new TextMessageContent(UUID.randomUUID().toString(), message);
        return new MessageEvent<TextMessageContent>(UUID.randomUUID().toString(), source, messageText, timestamp);
    }

    /**
     * Create a message event with {@link com.linecorp.bot.model.event.source.RoomSource}
     *
     * @param roomId
     * @param userId
     * @param message
     * @return message event of type {@link TextMessageContent}
     */
    public static MessageEvent<TextMessageContent> createMessageEventRoomSource(String roomId, String userId,
                                                                                String message) {

        Source source = new RoomSource(userId, roomId);
        Instant timestamp = Instant.now();

        TextMessageContent messageText = new TextMessageContent(UUID.randomUUID().toString(), message);
        return new MessageEvent<TextMessageContent>(UUID.randomUUID().toString(), source, messageText, timestamp);
    }

    /**
     * Create a message event with {@link UserSource}
     *
     * @param groupId
     * @param userId
     * @param message
     * @return message event of type {@link TextMessageContent}
     */
    public static MessageEvent<TextMessageContent> createMessageEventUserSource(String userId, String message) {

        Source source = new UserSource(userId);
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

    /**
     * Create a follow event
     *
     * @param userId
     * @return the event of type {@link FollowEvent}
     */
    public static FollowEvent createFollowEvent(String userId) {
        Instant timestamp = Instant.now();
        Source source = new UserSource(userId);
        return new FollowEvent(UUID.randomUUID().toString(), source, timestamp);
    }

    /**
     * Create a leave event
     *
     * @param groupId
     * @param userId
     * @return the event of type {@link LeaveEvent}
     */
    public static LeaveEvent createLeaveEvent(String groupId, String userId) {
        Instant timestamp = Instant.now();
        Source source = new GroupSource(groupId, userId);
        return new LeaveEvent(source, timestamp);
    }

}
