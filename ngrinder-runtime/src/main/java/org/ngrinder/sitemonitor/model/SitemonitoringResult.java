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
package org.ngrinder.sitemonitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Sitemonitoring execute result.
 * 
 * @author Gisoo Gwon
 */
@Entity
@Table(name = "sitemonitoring_result")
public class SitemonitoringResult implements Serializable {

	private static final long serialVersionUID = 1563353273200632007L;
	
	@EmbeddedId
	private SitemonitoringResultPK sitemonitoringResultPK;
	
	@Column(name = "success")
	private long success;
	
	@Column(name = "error")
	private long error;
	
	@Column(name = "test_time")
	private long testTime;
	
	public SitemonitoringResult() {
	}

	public SitemonitoringResult(String sitemonitoringId, int testNumber, long success, long error,
		long testTime, long timestamp) {
		this.sitemonitoringResultPK = new SitemonitoringResultPK(sitemonitoringId, testNumber, timestamp);
		this.success = success;
		this.error = error;
		this.testTime = testTime;
	}

	public String getSitemonitoringId() {
		return sitemonitoringResultPK.getSitemonitoringId();
	}

	public int getTestNumber() {
		return sitemonitoringResultPK.getTestNumber();
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

	public long getTimestamp() {
		return sitemonitoringResultPK.getTimestamp();
	}
	
	@Embeddable
	public static class SitemonitoringResultPK implements Serializable {

		private static final long serialVersionUID = 1087902971278611435L;
		
		@Column(name = "sitemonitoring_id")
		private String sitemonitoringId;
		@Column(name = "test_number")
		private int testNumber;
		@Column(name = "time_stamp")
		private long timestamp;
		
		public SitemonitoringResultPK() {
		}

		public SitemonitoringResultPK(String sitemonitoringId, int testNumber, long timestamp) {
			this.sitemonitoringId = sitemonitoringId;
			this.testNumber = testNumber;
			this.timestamp = timestamp;
		}

		@Override
		public int hashCode() {
			int result = sitemonitoringId.hashCode();
			result = 31 * result + testNumber;
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
			SitemonitoringResultPK pk = (SitemonitoringResultPK) obj;
			return pk.getSitemonitoringId().equals(sitemonitoringId)
				&& pk.getTestNumber() == testNumber;
		}

		public String getSitemonitoringId() {
			return sitemonitoringId;
		}

		public void setSitemonitoringId(String sitemonitoringId) {
			this.sitemonitoringId = sitemonitoringId;
		}

		public int getTestNumber() {
			return testNumber;
		}

		public void setTestNumber(int testNumber) {
			this.testNumber = testNumber;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		
	}

}
