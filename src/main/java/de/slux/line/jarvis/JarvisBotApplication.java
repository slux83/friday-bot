/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.slux.line.jarvis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

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
import de.slux.line.jarvis.command.AbstractCommand.CommandType;
import de.slux.line.jarvis.command.DefaultCommand;
import de.slux.line.jarvis.command.HelloGroupCommand;
import de.slux.line.jarvis.command.HelloUserCommand;
import de.slux.line.jarvis.command.HelpCommand;
import de.slux.line.jarvis.command.InfoCommand;
import de.slux.line.jarvis.command.admin.AdminBroadcastCommand;
import de.slux.line.jarvis.command.admin.AdminHelpCommand;
import de.slux.line.jarvis.command.admin.AdminStatusCommand;
import de.slux.line.jarvis.command.war.WarAddSummonersCommand;
import de.slux.line.jarvis.command.war.WarDeleteCommand;
import de.slux.line.jarvis.command.war.WarHistoryCommand;
import de.slux.line.jarvis.command.war.WarRegisterCommand;
import de.slux.line.jarvis.command.war.WarReportDeathCommand;
import de.slux.line.jarvis.command.war.WarResetCommand;
import de.slux.line.jarvis.command.war.WarSaveCommand;
import de.slux.line.jarvis.command.war.WarSummaryDeathCommand;
import de.slux.line.jarvis.command.war.WarSummonerNodeCommand;
import de.slux.line.jarvis.command.war.WarSummonerRenameCommand;
import de.slux.line.jarvis.command.war.WarUndoDeathCommand;

@SpringBootApplication
@LineMessageHandler
public class JarvisBotApplication {
	public static String JARVIS_VERSION = "0.0.1-beta2";
	public static final int MAX_LINE_MESSAGE_SIZE = 1500;

	private static Logger LOG = LoggerFactory.getLogger(JarvisBotApplication.class);
	private static JarvisBotApplication INSTANCE = null;
	public static final String SLUX_ID = "Ufea80d366e42a0e4b7e3d228ed133e89";

	public static synchronized JarvisBotApplication getInstance() {
		return INSTANCE;
	}

	public static synchronized void setInstance(JarvisBotApplication app) {
		INSTANCE = app;
	}

	@Autowired
	private LineMessagingClient lineMessagingClient;
	private Date startup;
	private AtomicLong incomingMsgCounter;
	private AtomicBoolean isOperational;
	private List<AbstractCommand> commands;

	public static void main(String[] args) {
		SpringApplication.run(JarvisBotApplication.class, args);
	}

	@Autowired
	public JarvisBotApplication(ApplicationArguments args) {
		LOG.info("Jarvis BOT - APP starting up...");
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startupCompleted() {
		LOG.info("*** JARVIS v" + JARVIS_VERSION + " startup completed ***");
	}

	@PostConstruct
	public void postConstruct() {
		// Save the instance
		setInstance(this);
		this.startup = new Date();
		this.incomingMsgCounter = new AtomicLong();
		this.isOperational = new AtomicBoolean(true);

		// Initialise all commands (the order is important for the help)
		this.commands = new ArrayList<>();

		// Event based commands (not part of the help)
		this.commands.add(new HelloUserCommand(this.lineMessagingClient));
		this.commands.add(new HelloGroupCommand(this.lineMessagingClient));

		// Utility commands
		this.commands.add(new HelpCommand(this.lineMessagingClient));
		this.commands.add(new InfoCommand(this.lineMessagingClient));

		// War commands
		this.commands.add(new WarRegisterCommand(this.lineMessagingClient));
		this.commands.add(new WarReportDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarUndoDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarSummaryDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarSaveCommand(this.lineMessagingClient));
		this.commands.add(new WarHistoryCommand(this.lineMessagingClient));
		this.commands.add(new WarDeleteCommand(this.lineMessagingClient));
		this.commands.add(new WarResetCommand(this.lineMessagingClient));
		this.commands.add(new WarAddSummonersCommand(this.lineMessagingClient));
		this.commands.add(new WarSummonerNodeCommand(this.lineMessagingClient));
		this.commands.add(new WarSummonerRenameCommand(this.lineMessagingClient));

		// Admin commands
		this.commands.add(new AdminHelpCommand(this.lineMessagingClient));
		this.commands.add(new AdminBroadcastCommand(this.lineMessagingClient));
		this.commands.add(new AdminStatusCommand(this.lineMessagingClient));

		LOG.info("Commands initialized. Total command(s): " + this.commands.size());

	}

	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		LOG.info("event: " + event);
		LOG.info("event source USER-ID: " + event.getSource().getUserId());
		LOG.info("event source SENDER_ID: " + event.getSource().getSenderId());
		LOG.info("event message text: " + event.getMessage().getText());
		this.incomingMsgCounter.incrementAndGet();

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

	/**
	 * Handle a private message
	 * 
	 * @param message
	 * @param userId
	 * @param event
	 * @return the text to send back to the user
	 */
	private TextMessage handleUserSource(String message, String userId, MessageEvent<TextMessageContent> event) {

		if (SLUX_ID.equals(userId)) {
			AbstractCommand command = getAdminCommand(message);

			return command.execute(userId, null, message);
		}

		return null;
	}

	/**
	 * Handle a group command
	 * 
	 * @param message
	 * @param userId
	 * @param event
	 * @param groupId
	 * @return the text to send back to the user
	 */
	private TextMessage handleGroupSource(String message, String userId, MessageEvent<TextMessageContent> event,
	        final String groupId) {

		AbstractCommand command = getCommand(message);

		if (!this.isOperational.get() && !(command instanceof DefaultCommand)) {
			return new TextMessage("Sorry, JARVIS is currently in standby for scheduled maintenance.");
		}

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
	 * Select the admin command that matches the text
	 * 
	 * @param text
	 * @return the command or {@link DefaultCommand}
	 */
	private AbstractCommand getAdminCommand(String text) {

		for (AbstractCommand command : this.commands) {
			if (command.getType().equals(CommandType.CommandTypeAdmin) && command.canTrigger(text.trim()))
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

	/**
	 * @return the startup
	 */
	public Date getStartup() {
		return startup;
	}

	/**
	 * @return the incomingMsgCounter as long
	 */
	public AtomicLong getIncomingMsgCounter() {
		return incomingMsgCounter;
	}

	/**
	 * @return the isOperational
	 */
	public AtomicBoolean getIsOperational() {
		return isOperational;
	}

}
