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

import org.hibernate.annotations.Type;


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
	
	@Column(name = "name")
	private String name;
	
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
	
	@Column(name = "run_state")
	@Type(type = "true_false")
	private boolean runState;
	
	@Column(name = "error_callback")
	private String errorCallback;
	
	@Transient
	private boolean agentRunning;

	public SiteMon() {
		
	}
	
	public SiteMon(String id, String name, User createdUser, String scriptName, long scriptRevision,
		String targetHosts, String param, String agentName, boolean runState, String errorCallback) {
		this.id = id;
		this.name = name;
		this.createdUser = createdUser;
		this.scriptName = scriptName;
		this.scriptRevision = scriptRevision;
		this.targetHosts = targetHosts;
		this.param = param;
		this.agentName = agentName;
		this.runState = runState;
		this.errorCallback = errorCallback;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean isRunState() {
		return runState;
	}

	public void setRunState(boolean runState) {
		this.runState = runState;
	}

	public String getErrorCallback() {
		return errorCallback;
	}

	public void setErrorCallback(String errorCallback) {
		this.errorCallback = errorCallback;
	}

	/**
	 * AgentRunning is not repository property.
	 * {@link Transient} value.
	 * @return
	 */
	public boolean isAgentRunning() {
		return agentRunning;
	}

	public void setAgentRunning(boolean agentRunning) {
		this.agentRunning = agentRunning;
	}

}
