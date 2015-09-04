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
package org.ngrinder.sitemon.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SiteMonResult log Entity
 * 
 * @author Gisoo Gwon
 */
@Entity
@Table(name = "sitemon_result_log")
public class SiteMonResultLog implements Serializable {

	private static final long serialVersionUID = 578993685171691501L;

	@EmbeddedId
	private PK pk;
	
	@Column(name = "log")
	private String log;
	
	public SiteMonResultLog() {
		pk = new PK();
	}
	
	public SiteMonResultLog(String siteMonId, Date timestamp, String log) {
		pk = new PK(siteMonId, timestamp);
		this.log = log;
	}
	
	public String getSiteMonId() {
		return pk.getSiteMonId();
	}
	
	public void setSiteMonId(String siteMonId) {
		pk.setSiteMonId(siteMonId);		
	}
	
	public Date getTimestamp() {
		return pk.getTimestamp();
	}
	
	public void setTimestamp(Date timestamp) {
		pk.setTimestamp(timestamp);
	}
	
	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Embeddable
	public static class PK implements Serializable {

		private static final long serialVersionUID = -9095583269105054924L;
		
		@Column(name = "sitemon_id")
		private String siteMonId;
		
		@Column(name = "time_stamp")
		private Date timestamp;
		
		public PK() {
			
		}
		
		public PK(String siteMonId, Date timestamp) {
			this.siteMonId = siteMonId;
			this.timestamp = timestamp;
		}
		
		@Override
		public int hashCode() {
			int result = siteMonId.hashCode();
			result = 31 * result + timestamp.hashCode();
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (this == null || getClass() != obj.getClass()) {
				return false;
			}
			PK pk = (PK) obj;
			return pk.getSiteMonId().equals(siteMonId) && pk.getTimestamp().equals(timestamp);
		}

		public String getSiteMonId() {
			return siteMonId;
		}

		public void setSiteMonId(String siteMonId) {
			this.siteMonId = siteMonId;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		
	}
	
}
