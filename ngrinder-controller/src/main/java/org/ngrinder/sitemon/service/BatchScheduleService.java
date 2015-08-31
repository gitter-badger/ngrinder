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
package org.ngrinder.sitemon.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

import org.ngrinder.common.util.DateUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gisoo Gwon
 */
@Component
public class BatchScheduleService {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(BatchScheduleService.class);
	
	@Autowired
	private Config config;
	
	@Autowired
	private SiteMonResultRepository siteMonResultRepository;
	
	/**
	 * It operate two o'clock every day for delete the old sitemon result.
	 */
	@Scheduled(cron="0 0 2 * * *")
	@Transactional
	public void deleteOldSiteMonResult() {
		if (isBatchServer()) {
			Date beforeMaxHistory = DateUtils.addDay(new Date(), -config.getSiteMonResultMaxHistory());
			siteMonResultRepository.deleteBeforeTimestamp(beforeMaxHistory);
		}
	}
	
	private boolean isBatchServer() {
		Enumeration<NetworkInterface> netInterfaceEnum;
		try {
			netInterfaceEnum = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			LOGGER.error("Error occurs while get network interface : {}", e);
			return false;
		}

		while (netInterfaceEnum.hasMoreElements()) {
			NetworkInterface netInterface = netInterfaceEnum.nextElement();
			Enumeration<InetAddress> addressEnum = netInterface.getInetAddresses();
			
			while (addressEnum.hasMoreElements()) {
				InetAddress address = addressEnum.nextElement();
				
				if (address instanceof Inet4Address) {
					if (address.getHostAddress().equals(config.getBatchServerIp())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
