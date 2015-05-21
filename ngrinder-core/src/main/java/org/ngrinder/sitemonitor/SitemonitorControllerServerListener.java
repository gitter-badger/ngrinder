/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sitemonitor;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.ngrinder.sitemonitor.messages.CreateGroupMessage;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.sitemonitor.messages.ShutdownServerMessage;
import org.ngrinder.sitemonitor.messages.UnregistScheduleMessage;
import org.slf4j.Logger;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.Message;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.communication.MessageDispatchSender;
import net.grinder.util.thread.Condition;

/**
 * Receive message from controller server.
 * Message keeping in message queue and send to process. 
 * 
 * @author Gisoo Gwon
 */
public class SitemonitorControllerServerListener {
	private final Logger LOGGER;
	private final Condition eventSyncCondition;
	private ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();
	
	private Handler<Message> messageHandler = new Handler<Message>() {
		@Override
		public void handle(Message message) throws CommunicationException {
			synchronized (eventSyncCondition) {
				messages.add(message);
				eventSyncCondition.notifyAll();
			}
		}

		@Override
		public void shutdown() {
			synchronized (eventSyncCondition) {
				messages.add(new ShutdownServerMessage());
				eventSyncCondition.notifyAll();
			}
		}
	};

	public SitemonitorControllerServerListener(Condition eventSyncCondition, Logger LOGGER) {
		this.eventSyncCondition = eventSyncCondition;
		this.LOGGER = LOGGER;
	}

	public void registerMessageHandlers(MessageDispatchSender messageDispatcher) {
		messageDispatcher.set(RegistScheduleMessage.class, messageHandler);
		messageDispatcher.set(UnregistScheduleMessage.class, messageHandler);
		messageDispatcher.set(CreateGroupMessage.class, messageHandler);
	}
	
	public void shutdown() {
		if (messages.size() == 0) {
			return;
		}
		
		StringBuilder sb = new StringBuilder("remain messages info");
		while (messages.size() > 0) {
			sb.append("\t");
			sb.append(messages.poll().toString());
			sb.append(System.getProperty("line.separator"));
		}
		LOGGER.info(sb.toString());
	}

	public Message waitForMessage() {
		while (true) {
			synchronized (eventSyncCondition) {
				eventSyncCondition.waitNoInterrruptException();
				if (messages.size() > 0) {
					return messages.poll();
				}
			}
		}
	}
}
