/**
 * 
 */
package de.slux.line.jarvis.test.command;

import java.time.Instant;

import org.junit.Test;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.command.InfoCommand;
import de.slux.line.jarvis.command.admin.AdminStatusCommand;

/**
 * @author slux
 */
public class TestCommand {

	@Test
	public void testAdminStatusCommand() throws Exception {
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.postConstruct();

		Source source = new UserSource(JarvisBotApplication.SLUX_ID);
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", AdminStatusCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		jarvis.getIncomingMsgCounter().set(10);
		Thread.sleep(1000);
		TextMessage response = jarvis.handleTextMessageEvent(event);

		System.out.println(response);

		message = new TextMessageContent("001", AdminStatusCommand.CMD_PREFIX + " fake foo bar");
		event = new MessageEvent<TextMessageContent>("reply-token", source, message, timestamp);
		response = jarvis.handleTextMessageEvent(event);

		System.out.println(response);

		// 16 secs day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 16));

		// 1 day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 60 * 60 * 24));

		// 10 day
		System.out.println(AdminStatusCommand.calculateUptime(1000 * 60 * 60 * 24 * 10));

		// 10 day and 1h
		System.out.println(AdminStatusCommand.calculateUptime((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60));

		// 10 day and 1h and 12 min
		System.out.println(
		        AdminStatusCommand.calculateUptime(((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60) + 1000 * 60 * 12));

		// 10 day and 1h and 12 min and 40 secs
		System.out.println(AdminStatusCommand
		        .calculateUptime((((1000 * 60 * 60 * 24 * 10) + 1000 * 60 * 60) + 1000 * 60 * 12) + 1000 * 40));

	}

	@Test
	public void testInfoCommand() throws Exception {
		JarvisBotApplication jarvis = new JarvisBotApplication(null);
		jarvis.postConstruct();

		Source source = new GroupSource("group-id", "user-id");
		Instant timestamp = Instant.now();
		TextMessageContent message = new TextMessageContent("001", InfoCommand.CMD_PREFIX);
		MessageEvent<TextMessageContent> event = new MessageEvent<TextMessageContent>("reply-token", source, message,
		        timestamp);

		TextMessage response = jarvis.handleTextMessageEvent(event);

		System.out.println(response);
	}
}