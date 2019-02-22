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

package de.slux.line.friday;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import de.slux.line.friday.command.AbstractCommand;
import de.slux.line.friday.command.AbstractCommand.CommandType;
import de.slux.line.friday.command.DefaultCommand;
import de.slux.line.friday.command.EventInfoCommand;
import de.slux.line.friday.command.GoodbyeGroupCommand;
import de.slux.line.friday.command.HelloGroupCommand;
import de.slux.line.friday.command.HelloUserCommand;
import de.slux.line.friday.command.HelpCommand;
import de.slux.line.friday.command.InfoCommand;
import de.slux.line.friday.command.RegisterEventsCommand;
import de.slux.line.friday.command.UnregisterEventsCommand;
import de.slux.line.friday.command.WarStatsCommand;
import de.slux.line.friday.command.admin.AdminBroadcastCommand;
import de.slux.line.friday.command.admin.AdminPushNotificationCommand;
import de.slux.line.friday.command.admin.AdminStatusCommand;
import de.slux.line.friday.command.war.WarAddSummonersCommand;
import de.slux.line.friday.command.war.WarDeleteCommand;
import de.slux.line.friday.command.war.WarDeleteNodeCommand;
import de.slux.line.friday.command.war.WarDiversityCommand;
import de.slux.line.friday.command.war.WarHistoryCommand;
import de.slux.line.friday.command.war.WarRegisterCommand;
import de.slux.line.friday.command.war.WarReportDeathCommand;
import de.slux.line.friday.command.war.WarResetCommand;
import de.slux.line.friday.command.war.WarSaveCommand;
import de.slux.line.friday.command.war.WarSummaryDeathCommand;
import de.slux.line.friday.command.war.WarSummonerNodeCommand;
import de.slux.line.friday.command.war.WarSummonerRenameCommand;
import de.slux.line.friday.command.war.WarUndoDeathCommand;
import de.slux.line.friday.data.stats.HistoryStats;
import de.slux.line.friday.data.war.WarGroup;
import de.slux.line.friday.scheduler.EventScheduler;
import de.slux.line.friday.scheduler.McocSchedulerImporter;

@SpringBootApplication
@LineMessageHandler
public class FridayBotApplication {
	public static String FRIDAY_VERSION = "0.1.1-p3";
	public static final int MAX_LINE_MESSAGE_SIZE = 1500;

	private static Logger LOG = LoggerFactory.getLogger(FridayBotApplication.class);
	private static FridayBotApplication INSTANCE = null;
	public static final String SLUX_ID = "Ufea80d366e42a0e4b7e3d228ed133e89";

	// If we need to start under maintenance
	public static final String FRIDAY_MAINTENANCE_KEY = "friday.maintenance";

	public static final int MAX_MESSAGE_BURST = 50;
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static synchronized FridayBotApplication getInstance() {
		return INSTANCE;
	}

	public static synchronized void setInstance(FridayBotApplication app) {
		INSTANCE = app;
	}

	@Autowired
	private LineMessagingClient lineMessagingClient;
	private Date startup;
	private AtomicLong commandIncomingMsgCounter;
	private AtomicLong totalIncomingMsgCounter;
	private AtomicBoolean isOperational;
	private List<AbstractCommand> commands;
	private McocSchedulerImporter scheduler;
	private EventScheduler eventScheduler;
	private LinkedList<String> lastPushedMessages;
	private Clock clockReference;
	private Map<Integer, List<HistoryStats>> warNodeStatistics;
	private Map<String, List<HistoryStats>> warChampStatistics;
	private Map<String, String> championsData;
	private Set<String> groupActivities;

	public static void main(String[] args) {
		SpringApplication.run(FridayBotApplication.class, args);
	}

	@Autowired
	public FridayBotApplication(ApplicationArguments args) {
		LOG.info("FRIDAY BOT - APP starting up...");
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startupCompleted() {
		LOG.info("*** FRIDAY v" + FRIDAY_VERSION + " startup completed ***");
	}

	@PostConstruct
	public void postConstruct() {
		// Save the instance
		setInstance(this);
		if (clockReference == null)
			this.clockReference = Clock.systemDefaultZone();

		this.groupActivities = ConcurrentHashMap.newKeySet();
		this.startup = new Date(this.clockReference.millis());
		this.commandIncomingMsgCounter = new AtomicLong();
		this.totalIncomingMsgCounter = new AtomicLong();
		this.lastPushedMessages = new LinkedList<>();

		// Check if we are starting in maintenance directly
		this.isOperational = new AtomicBoolean(System.getProperty(FRIDAY_MAINTENANCE_KEY) == null);

		// Initialize scheduler
		try {
			this.scheduler = new McocSchedulerImporter();
		} catch (Exception e) {
			LOG.error("Cannot initialize MCOC Scheduler Importer: " + e, e);
			throw new RuntimeException(e);
		}

		try {
			this.eventScheduler = new EventScheduler(this.scheduler);
		} catch (Exception e) {
			LOG.error("Cannot initialize MCOC Event Scheduler: " + e, e);
			throw new RuntimeException(e);
		}

		// Initialize all commands (the order is important for the help)
		this.commands = new ArrayList<>();

		// Event based commands (not part of the help)
		this.commands.add(new HelloUserCommand(this.lineMessagingClient));
		this.commands.add(new HelloGroupCommand(this.lineMessagingClient));
		this.commands.add(new GoodbyeGroupCommand(this.lineMessagingClient));

		// Utility commands
		this.commands.add(new HelpCommand(this.lineMessagingClient));
		this.commands.add(new InfoCommand(this.lineMessagingClient));
		this.commands.add(new EventInfoCommand(this.lineMessagingClient, this.scheduler));
		this.commands.add(new RegisterEventsCommand(this.lineMessagingClient));
		this.commands.add(new UnregisterEventsCommand(this.lineMessagingClient));
		this.commands.add(new WarStatsCommand(this.lineMessagingClient));

		// War commands
		this.commands.add(new WarRegisterCommand(this.lineMessagingClient));
		this.commands.add(new WarReportDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarUndoDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarDeleteNodeCommand(this.lineMessagingClient));
		this.commands.add(new WarSummaryDeathCommand(this.lineMessagingClient));
		this.commands.add(new WarSaveCommand(this.lineMessagingClient));
		this.commands.add(new WarHistoryCommand(this.lineMessagingClient));
		this.commands.add(new WarDeleteCommand(this.lineMessagingClient));
		this.commands.add(new WarResetCommand(this.lineMessagingClient));
		this.commands.add(new WarAddSummonersCommand(this.lineMessagingClient));
		this.commands.add(new WarSummonerNodeCommand(this.lineMessagingClient));
		this.commands.add(new WarSummonerRenameCommand(this.lineMessagingClient));
		this.commands.add(new WarDiversityCommand(this.lineMessagingClient));

		// Admin commands
		this.commands.add(new AdminBroadcastCommand(this.lineMessagingClient));
		this.commands.add(new AdminPushNotificationCommand(this.lineMessagingClient));
		this.commands.add(new AdminStatusCommand(this.lineMessagingClient));

		LOG.info("Commands initialized. Total command(s): " + this.commands.size());

	}

	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		LOG.info("event: " + event);
		LOG.info("event source USER-ID: " + event.getSource().getUserId());
		LOG.info("event source SENDER_ID: " + event.getSource().getSenderId());
		LOG.info("event message text: " + event.getMessage().getText());
		this.totalIncomingMsgCounter.incrementAndGet();

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
		AbstractCommand command = null;

		if (SLUX_ID.equals(userId)) {
			// Admin commands
			command = getAdminCommand(message);
		} else {
			// Normal user command
			command = getUserCommand(message);
		}

		if (!this.isOperational.get() && !SLUX_ID.equals(userId)) {
			return new TextMessage("Sorry, FRIDAY is currently in standby for scheduled maintenance.");
		}

		if (!(command instanceof DefaultCommand)) {
			this.commandIncomingMsgCounter.incrementAndGet();
		} else if (message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX)) {
			// Try to see if the user was close to one of the existing commands
			List<CommandType> adminExcludedCommands = Arrays.asList(CommandType.CommandTypeUtility,
			        CommandType.CommandTypeWar);
			List<CommandType> userExcludedCommands = Arrays.asList(CommandType.CommandTypeUtility,
			        CommandType.CommandTypeAdmin, CommandType.CommandTypeWar);

			return getClosestCommandSuggestion(message,
			        SLUX_ID.equals(userId) ? adminExcludedCommands : userExcludedCommands);
		}

		return command.execute(userId, null, message);

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

		this.groupActivities.add(groupId);

		AbstractCommand command = getGroupCommand(message);

		if (!this.isOperational.get() && message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX)
		        && !SLUX_ID.equals(userId)) {
			return new TextMessage("Sorry, FRIDAY is currently in standby for scheduled maintenance.");
		}

		if (!(command instanceof DefaultCommand)) {
			this.commandIncomingMsgCounter.incrementAndGet();
		} else if (message.toLowerCase().startsWith(AbstractCommand.ALL_CMD_PREFIX)) {
			// Try to see if the user was close to one of the existing commands
			return getClosestCommandSuggestion(message,
			        Arrays.asList(CommandType.CommandTypeAdmin, CommandType.CommandTypeUser));
		}

		return command.execute(userId, groupId, message);
	}

	/**
	 * Get the closest group command suggestion if any
	 * 
	 * @param message
	 * @param command
	 *            types to exclude
	 * @return
	 */
	private TextMessage getClosestCommandSuggestion(String message, List<CommandType> exclude) {
		double bestDistance = 0;
		String bestPrefix = null;
		for (AbstractCommand command : this.commands) {
			if (command.getCommandPrefix() != null && !exclude.contains(command.getType())) {
				String prefix = AbstractCommand.ALL_CMD_PREFIX + " " + command.getCommandPrefix();
				int prefixChunks = prefix.split(" ").length;
				List<String> messageChunks = Arrays.asList(message.toLowerCase().split(" "));

				String messageCommand = null;
				if (messageChunks.size() <= prefixChunks) {
					// we take it all
					messageCommand = String.join(" ", messageChunks);
				} else {
					// we only consider up to prefixChunks messageChunks
					messageCommand = String.join(" ", messageChunks.subList(0, prefixChunks));
				}

				JaroWinklerDistance distance = new JaroWinklerDistance();
				double cmdDistance = distance.apply(prefix, messageCommand);
				if (bestDistance < cmdDistance) {
					bestDistance = cmdDistance;
					bestPrefix = prefix;
					// LOG.info("Distance between '" + prefix + "' and '" +
					// messageCommand + "' is " + cmdDistance);
				}
			}
		}

		if (bestDistance > 0.95d) {
			return new TextMessage("Sorry, perhaps you mean '" + bestPrefix + "'");
		}

		return new TextMessage("Sorry, I didn't understand. Try with " + AbstractCommand.ALL_CMD_PREFIX + " "
		        + HelpCommand.CMD_PREFIX);

	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		LOG.info("event: " + event);
		LOG.info("event source USER-ID=" + event.getSource().getUserId());
		LOG.info("event source SENDER_ID=" + event.getSource().getSenderId());

		AbstractCommand command = getGroupCommand(event.getClass().getSimpleName());

		command.execute(event.getSource().getUserId(), event.getSource().getSenderId(), null);
	}

	/**
	 * Select the command that matches the text
	 * 
	 * @param text
	 * @return the command or {@link DefaultCommand}
	 */
	private AbstractCommand getGroupCommand(String text) {

		for (AbstractCommand command : this.commands) {
			if (!command.getType().equals(CommandType.CommandTypeAdmin)
			        && !command.getType().equals(CommandType.CommandTypeUser) && command.canTrigger(text.trim()))
				return command;
		}

		return new DefaultCommand(this.lineMessagingClient);
	}

	/**
	 * Select the command that matches the text
	 * 
	 * @param text
	 * @return the command or {@link DefaultCommand}
	 */
	private AbstractCommand getUserCommand(String text) {

		for (AbstractCommand command : this.commands) {
			if ((command.getType().equals(CommandType.CommandTypeShared)
			        || command.getType().equals(CommandType.CommandTypeUser)) && command.canTrigger(text.trim()))
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
			if ((command.getType().equals(CommandType.CommandTypeAdmin)
			        || command.getType().equals(CommandType.CommandTypeShared)
			        || command.getType().equals(CommandType.CommandTypeUser)) && command.canTrigger(text.trim()))
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
	 * @param client
	 */
	public void setLineMessagingClient(LineMessagingClient client) {
		this.lineMessagingClient = client;
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
	 * @return the commandIncomingMsgCounter
	 */
	public AtomicLong getCommandIncomingMsgCounter() {
		return commandIncomingMsgCounter;
	}

	/**
	 * @return the totalIncomingMsgCounter
	 */
	public AtomicLong getTotalIncomingMsgCounter() {
		return totalIncomingMsgCounter;
	}

	/**
	 * @return the isOperational
	 */
	public AtomicBoolean getIsOperational() {
		return isOperational;
	}

	/**
	 * @return the eventScheduler
	 */
	public EventScheduler getEventScheduler() {
		return this.eventScheduler;
	}

	/**
	 * Push a message to the administrator {@link FridayBotApplication#SLUX_ID}
	 * 
	 * @param message
	 */
	public void pushMessageToAdmin(String message) {
		WarGroup wg = new WarGroup();
		wg.setGroupId(SLUX_ID);
		wg.setGroupName("SLUX");

		pushMultiMessages(Arrays.asList(wg), message);
	}

	/**
	 * Push the message to all the groups
	 * 
	 * @param groups
	 * @param message
	 * @return the statistic string
	 */
	public String pushMultiMessages(Collection<WarGroup> groups, String message) {
		List<CompletableFuture<BotApiResponse>> asyncMessages = new ArrayList<>();
		int pushedCounter = 0;
		int totalSent = 0;
		Date now = new Date(this.clockReference.millis());

		for (WarGroup group : groups) {
			pushedCounter++;
			PushMessage pushMessage = new PushMessage(group.getGroupId(), new TextMessage(message));

			if (LOG.isDebugEnabled()) {
				LOG.debug("Broadcasting message '" + message + "' to " + group);
			}

			if (asyncMessages.size() > MAX_MESSAGE_BURST) {
				// We need to consume the ones sent so far
				for (CompletableFuture<BotApiResponse> resp : asyncMessages) {
					try {
						resp.get();
						totalSent++;
					} catch (Exception e) {
						LOG.warn("Cannot push message to group. Reason: " + e, e);
					}
				}
				asyncMessages.clear();
			}

			// Push the message
			asyncMessages.add(this.lineMessagingClient.pushMessage(pushMessage));
		}

		// We wait the confirmation of the remaining ones
		if (!asyncMessages.isEmpty()) {
			for (CompletableFuture<BotApiResponse> resp : asyncMessages) {
				try {
					resp.get();
					totalSent++;
				} catch (Exception e) {
					LOG.warn("Cannot push message to group. Reason: " + e, e);
				}
			}
		}

		return storePushStatistics(now, totalSent, pushedCounter, groups.size());
	}

	/**
	 * Save the statistics of the broadcast push
	 * 
	 * @param time
	 * @param totalSent
	 * @param pushedCounter
	 * @param numGroups
	 * @return the statistics string
	 */
	private String storePushStatistics(Date time, int totalSent, int pushedCounter, int numGroups) {
		StringBuilder sb = new StringBuilder();

		sb.append(SDF.format(time));
		sb.append(": ");
		sb.append("pushed(" + pushedCounter + ") ");
		sb.append("sent(" + totalSent + ") ");
		sb.append("active_groups(" + numGroups + ")");

		this.lastPushedMessages.addFirst(sb.toString());

		while (this.lastPushedMessages.size() > 5) {
			this.lastPushedMessages.removeLast();
		}

		return sb.toString();
	}

	/**
	 * Return the last pushed messages (statistics)
	 * 
	 * @return
	 */
	public LinkedList<String> getPushStatistics() {
		return this.lastPushedMessages;
	}

	/**
	 * Set the clock reference. Call this before calling the
	 * {@link FridayBotApplication#postConstruct()}. This is mainly used for
	 * testing
	 * 
	 * @param clock
	 */
	public void setClockReference(Clock clock) {
		this.clockReference = clock;
	}

	/**
	 * Get the clock reference
	 * 
	 * @return clockReference
	 */
	public Clock getClockReference() {
		return this.clockReference;
	}

	/**
	 * @return the warNodeStatistics
	 */
	public synchronized Map<Integer, List<HistoryStats>> getWarNodeStatistics() {
		return warNodeStatistics;
	}

	/**
	 * @param warNodeStatistics
	 *            the warNodeStatistics to set
	 */
	public synchronized void setWarNodeStatistics(Map<Integer, List<HistoryStats>> warNodeStatistics) {
		this.warNodeStatistics = warNodeStatistics;
	}

	/**
	 * @return the championsData
	 */
	public synchronized Map<String, String> getChampionsData() {
		return championsData;
	}

	/**
	 * @param championsData
	 *            the championsData to set
	 */
	public synchronized void setChampionsData(Map<String, String> championsData) {
		this.championsData = championsData;
	}

	/**
	 * @return the warChampStatistics
	 */
	public synchronized Map<String, List<HistoryStats>> getWarChampStatistics() {
		return warChampStatistics;
	}

	/**
	 * @param warChampStatistics
	 *            the warChampStatistics to set
	 */
	public synchronized void setWarChampStatistics(Map<String, List<HistoryStats>> warChampStatistics) {
		this.warChampStatistics = warChampStatistics;
	}

	/**
	 * @return the groupActivities
	 */
	public Set<String> getGroupActivities() {
		return groupActivities;
	}
}
