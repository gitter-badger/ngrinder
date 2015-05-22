package org.ngrinder.sitemonitor.messages;

import net.grinder.communication.Message;

/**
 * @author Gisoo Gwon
 */
public class RemoveScriptMessage implements Message {
	private static final long serialVersionUID = -4346271974348645977L;
	private final String scriptpath;

	public RemoveScriptMessage(String scriptpath) {
		this.scriptpath = scriptpath;
	}

	public String getScriptpath() {
		return scriptpath;
	}
}
