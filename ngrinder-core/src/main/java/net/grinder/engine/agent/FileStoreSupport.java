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
package net.grinder.engine.agent;

import java.io.File;

import org.slf4j.Logger;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchSender;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.engine.common.EngineException;
import net.grinder.messages.agent.ClearCacheMessage;

/**
 * Wrapping {@link FileStore} for public access. 
 * @author Gisoo Gwon
 */
public class FileStoreSupport {
	private final FileStore fileStore;
	private final MessageDispatchSender messageDispatcherSender;

	public FileStoreSupport(File directory, MessageDispatchSender messageDispatcherSender,
		Logger LOGGER) throws EngineException {
		this.messageDispatcherSender = messageDispatcherSender;
		fileStore = new FileStore(directory, LOGGER);
		fileStore.registerMessageHandlers(messageDispatcherSender);
	}

	public void ignoreClearCacheMessage() {
		messageDispatcherSender.set(ClearCacheMessage.class,
			new AbstractHandler<ClearCacheMessage>() {
				public void handle(ClearCacheMessage message) throws CommunicationException {
					// ignore clear cache message
				}
			});
	}
}
