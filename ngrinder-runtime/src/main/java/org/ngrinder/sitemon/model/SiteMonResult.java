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
 * SiteMon execute result.
 * 
 * @author Gisoo Gwon
 */
@Entity
@Table(name = "sitemon_result")
public class SiteMonResult implements Serializable {

	private static final long serialVersionUID = 1563353273200632007L;
	
	@EmbeddedId
	private SiteMonResultPK siteMonResultPK;
	
	@Column(name = "success")
	private long success;
	
	@Column(name = "error")
	private long error;
	
	@Column(name = "test_time")
	private long testTime;
	
	public SiteMonResult() {
	}

	public SiteMonResult(String siteMonId, int testNumber, long success, long error,
		long testTime, Date timestamp) {
		this.siteMonResultPK = new SiteMonResultPK(siteMonId, testNumber, timestamp);
		this.success = success;
		this.error = error;
		this.testTime = testTime;
	}

	public String getSiteMonId() {
		return siteMonResultPK.getSiteMonId();
	}

	public int getTestNumber() {
		return siteMonResultPK.getTestNumber();
	}

	public long getSuccess() {
		return success;
	}

	public long getError() {
		return error;
	}

	public long getTestTime() {
		return testTime;
	}

	public Date getTimestamp() {
		return siteMonResultPK.getTimestamp();
	}
	
	@Embeddable
	public static class SiteMonResultPK implements Serializable {

		private static final long serialVersionUID = 1087902971278611435L;
		
		@Column(name = "sitemon_id")
		private String siteMonId;
		@Column(name = "test_number")
		private int testNumber;
		@Column(name = "time_stamp")
		private Date timestamp;
		
		public SiteMonResultPK() {
		}

		public SiteMonResultPK(String siteMonId, int testNumber, Date timestamp) {
			this.siteMonId = siteMonId;
			this.testNumber = testNumber;
			this.timestamp = timestamp;
		}

		@Override
		public int hashCode() {
			int result = siteMonId.hashCode();
			result = 31 * result + testNumber;
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
			SiteMonResultPK pk = (SiteMonResultPK) obj;
			return pk.getSiteMonId().equals(siteMonId)
				&& pk.getTestNumber() == testNumber && pk.getTimestamp().equals(timestamp);
		}

		public String getSiteMonId() {
			return siteMonId;
		}

		public void setSiteMonId(String siteMonId) {
			this.siteMonId = siteMonId;
		}

		public int getTestNumber() {
			return testNumber;
		}

		public void setTestNumber(int testNumber) {
			this.testNumber = testNumber;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		
	}

}
