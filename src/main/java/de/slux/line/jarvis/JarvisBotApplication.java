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

@SpringBootApplication
@LineMessageHandler
public class JarvisBotApplication {

	private static JarvisBotApplication INSTANCE = null;

	public static synchronized JarvisBotApplication getInstance() {
		return INSTANCE;
	}

	public static synchronized void setInstance(JarvisBotApplication app) {
		INSTANCE = app;
	}

	@Autowired
	private LineMessagingClient lineMessagingClient;

	public static void main(String[] args) {
		SpringApplication.run(JarvisBotApplication.class, args);
	}

	@Autowired
	public JarvisBotApplication(ApplicationArguments args) {
		setInstance(this);

		System.out.println("STARTED UP");

	}

	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		System.out.println("event: " + event);
		System.out.println("event source USER-ID=" + event.getSource().getUserId());
		System.out.println("event source SENDER_ID=" + event.getSource().getSenderId());
		System.out.println("event timestamp=" + event.getTimestamp().getEpochSecond());
		System.out.println("event reply_token=" + event.getReplyToken());
		System.out.println("event message_text=" + event.getMessage().getText());

		String command = event.getMessage().getText().trim().toLowerCase();
		String userId = event.getSource().getUserId();
		if (userId == null)
			userId = event.getSource().getSenderId();

		if (userId == null) {
			System.err.println("User does not have the user ID nor the sender ID. Can't do much here!");
			return null;
		}

		if (event.getSource() instanceof GroupSource) {
			return handleGroupSource(command, userId, event, ((GroupSource) event.getSource()).getGroupId());
		}

		if (event.getSource() instanceof UserSource) {
			return handleUserSource(command, userId, event);
		}

		return null;
	}

	private TextMessage handleUserSource(String command, String userId, MessageEvent<TextMessageContent> event) {
		return new TextMessage("Hello private user");
	}

	private TextMessage handleGroupSource(String command, String userId, MessageEvent<TextMessageContent> event,
			final String groupId) {

		return new TextMessage("Hello group");
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
		System.out.println("event source USER-ID=" + event.getSource().getUserId());
		System.out.println("event source SENDER_ID=" + event.getSource().getSenderId());
		System.out.println("event timestamp=" + event.getTimestamp().getEpochSecond());
	}

	/**
	 * @return the {@link LineMessagingClient}
	 */
	public LineMessagingClient getLineMessagingClient() {
		return this.lineMessagingClient;
	}
}
