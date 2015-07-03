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
package org.ngrinder.sitemonitor.messages;

import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;
import net.grinder.messages.console.AgentAddress;

/**
 * Reload sitemonitoring script and setting.
 * 
 * @author Gisoo Gwon
 */
public class SitemonitoringReloadMessage implements AddressAwareMessage {

	private static final long serialVersionUID = 4302753336694439739L;
	private AgentAddress agentAddress;

	@Override
	public void setAddress(Address agentAddress) throws CommunicationException {
		try {
			this.agentAddress = (AgentAddress) agentAddress;
		} catch (ClassCastException e) {
			throw new CommunicationException("Not an agent process address", e);
		}
	}

	public AgentAddress getAgentAddress() {
		return agentAddress;
	}

}
