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
package net.grinder.console.communication;

import net.grinder.communication.Address;
import net.grinder.messages.agent.CacheHighWaterMark;
import net.grinder.util.FileContents;

/**
 * {@link FileDistributionImplementation} distribute all agent, so can't choose send target agent.
 * <p>{@link DistributionControlImplementationAddressDecorator} change the send 
 * target address to replaceAddress.</p>
 * 
 * @author Gisoo Gwon
 */
public class DistributionControlImplementationAddressDecorator extends
	DistributionControlImplementation {

	private final Address replaceAddress;

	/**
	 * 
	 * @param consoleCommunication
	 * @param replaceAddress file distribute target agent address.
	 */
	public DistributionControlImplementationAddressDecorator(
		ConsoleCommunication consoleCommunication, Address replaceAddress) {
		super(consoleCommunication);
		this.replaceAddress = replaceAddress;
	}

	@Override
	public void clearFileCaches(Address address) {
		super.clearFileCaches(replaceAddress);
	}

	/**
	 * replace target agent address.
	 * @param address not used. use instead reaplaceAddress.
	 * @param fileContents
	 */
	@Override
	public void sendFile(Address address, FileContents fileContents) {
		super.sendFile(replaceAddress, fileContents);
	}

	@Override
	public void setHighWaterMark(Address address, CacheHighWaterMark highWaterMark) {
		super.setHighWaterMark(replaceAddress, highWaterMark);
	}

}
