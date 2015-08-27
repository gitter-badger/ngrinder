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
package org.ngrinder.sitemon.messages;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.communication.Message;

/**
 * @author Gisoo Gwon
 */
public class SiteMonErrorCallbackMessage extends Thread implements Message {

	private static final long serialVersionUID = 1779469215568877875L;
	private static final Logger LOGGER = LoggerFactory.getLogger("site mon error callback");
	private final String callbackUrl;
	private final String siteMonId;
	private final String error;
	
	public SiteMonErrorCallbackMessage(String callbackUrl, String siteMonId, String error) {
		this.callbackUrl = callbackUrl;
		this.siteMonId = siteMonId;
		this.error = error;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(callbackUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("id=" + siteMonId + "&error=" + error);
			wr.flush();
			wr.close();
			if (con.getResponseCode() != 200) {
				throw new Exception("Response code is " + con.getResponseCode());
			}
		} catch (Exception e) {
			LOGGER.error("Error request error callback api. {}", e.getMessage());
		}
	}

}
