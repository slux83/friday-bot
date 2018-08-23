/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package de.slux.line.jarvis;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.command.DefaultCommand;
import de.slux.line.jarvis.command.HelloGroupCommand;
import de.slux.line.jarvis.command.HelloUserCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.command.war.WarRegisterCommand;
import de.slux.line.jarvis.command.war.WarReportDeathCommand;

@SpringBootApplication
@LineMessageHandler
public class JarvisBotApplication {
	public static final int MAX_LINE_MESSAGE_SIZE = 1500;

	private static Logger LOG = LoggerFactory.getLogger(JarvisBotApplication.class);
	private static JarvisBotApplication INSTANCE = null;

	public static synchronized JarvisBotApplication getInstance() {
		return INSTANCE;
	}

	public static synchronized void setInstance(JarvisBotApplication app) {
		INSTANCE = app;
	}

	@Autowired
	private LineMessagingClient lineMessagingClient;

	private List<AbstractCommand> commands;

	public static void main(String[] args) {
		SpringApplication.run(JarvisBotApplication.class, args);
	}

	@Autowired
	public JarvisBotApplication(ApplicationArguments args) {
		LOG.info("Jarvis BOT - APP Initialization completed");
	}

	@PostConstruct
	public void postConstruct() {
		// Save the instance
		setInstance(this);

		// Initialise all commands (the order is important for the help)
		this.commands = new ArrayList<>();

		// Event based commands (not part of the help)
		this.commands.add(new HelloUserCommand(this.lineMessagingClient));
		this.commands.add(new HelloGroupCommand(this.lineMessagingClient));

		// Utility commands
		this.commands.add(new HelpCommand(this.lineMessagingClient));

		// War commands
		this.commands.add(new WarRegisterCommand(this.lineMessagingClient));
		this.commands.add(new WarReportDeathCommand(this.lineMessagingClient));

		LOG.info("Commands initialized. Total command(s): " + this.commands.size());
	}

	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		LOG.info("event: " + event);
		LOG.info("event source USER-ID: " + event.getSource().getUserId());
		LOG.info("event source SENDER_ID: " + event.getSource().getSenderId());
		LOG.info("event message text: " + event.getMessage().getText());

		String message = event.getMessage().getText().trim();
		String userId = event.getSource().getUserId();
		if (userId == null)
			userId = event.getSource().getSenderId();

		if (event.getSource() instanceof GroupSource) {
			return handleGroupSource(message, userId, event, ((GroupSource) event.getSource()).getGroupId());
		}

		if (event.getSource() instanceof UserSource) {
			return handleUserSource(message, userId, event);
		}

		return null;
	}

	private TextMessage handleUserSource(String command, String userId, MessageEvent<TextMessageContent> event) {
		return new TextMessage("Hello private user");
	}

	private TextMessage handleGroupSource(String message, String userId, MessageEvent<TextMessageContent> event,
			final String groupId) {

		AbstractCommand command = getCommand(message);

		return command.execute(userId, groupId, message);
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		LOG.info("event: " + event);
		LOG.info("event source USER-ID=" + event.getSource().getUserId());
		LOG.info("event source SENDER_ID=" + event.getSource().getSenderId());

		AbstractCommand command = getCommand(event.getClass().getSimpleName());

		command.execute(event.getSource().getUserId(), event.getSource().getSenderId(), null);
	}

	/**
	 * Select the command that matches the text
	 * 
	 * @param text
	 * @return the command or {@link DefaultCommand}
	 */
	private AbstractCommand getCommand(String text) {

		for (AbstractCommand command : this.commands) {
			if (command.canTrigger(text.trim()))
				return command;
		}

		return new DefaultCommand(this.lineMessagingClient);
	}

	/**
	 * @return the {@link LineMessagingClient}
	 */
	public LineMessagingClient getLineMessagingClient() {
		return this.lineMessagingClient;
	}

	/**
	 * Get the commands
	 * 
	 * @return the commands
	 */
	public List<AbstractCommand> getCommands() {
		return this.commands;
	}

}
