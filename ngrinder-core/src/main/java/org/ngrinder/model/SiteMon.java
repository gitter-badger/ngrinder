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
package org.ngrinder.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * SiteMon Entity
 * 
 * @author Gisoo Gwon
 */

@Entity
@Table(name = "sitemon")
public class SiteMon {

	@Id
	@Column(name = "id")
	private String id;
	
	@OneToOne
	@JoinColumn(name = "created_user")
	private User createdUser;
	
	@Column(name = "script_name")
	private String scriptName;
	
	@Column(name = "script_revision")
	private long scriptRevision;
	
	@Column(name = "target_hosts")
	private String targetHosts;
	
	@Column(name = "param")
	private String param;
	
	@Column(name = "agent_name")
	private String agentName;
	
	@Transient
	private boolean agentRunning;

	public SiteMon() {
		
	}
	
	public SiteMon(String id, User createdUser, String scriptName, long scriptRevision,
		String targetHosts, String param, String agentName) {
		this.id = id;
		this.createdUser = createdUser;
		this.scriptName = scriptName;
		this.scriptRevision = scriptRevision;
		this.targetHosts = targetHosts;
		this.param = param;
		this.agentName = agentName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(User createdUser) {
		this.createdUser = createdUser;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public long getScriptRevision() {
		return scriptRevision;
	}

	public void setScriptRevision(long scriptRevision) {
		this.scriptRevision = scriptRevision;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(String targetHosts) {
		this.targetHosts = targetHosts;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public boolean isAgentRunning() {
		return agentRunning;
	}

	public void setAgentRunning(boolean agentRunning) {
		this.agentRunning = agentRunning;
	}

}
