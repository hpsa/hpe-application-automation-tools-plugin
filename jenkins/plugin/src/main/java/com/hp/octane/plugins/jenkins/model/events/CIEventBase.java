package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class CIEventBase implements IJSONable {
	public abstract CIEventType getEventType();

	public CIServerType serverType;
	public String serverURL;
	public String project;
	public CIEventCauseBase cause;

	public CIEventBase(CIServerType serverType, String serverURL, String project, CIEventCauseBase cause) {
		this.serverType = serverType;
		this.serverURL = serverURL;
		this.project = project;
		this.cause = cause;
	}

	public CIEventBase(CIServerType serverType, String serverURL) {
		this.serverType = serverType;
		this.serverURL = serverURL;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = new JSONObject();
		JSONArray tmp = new JSONArray();
		r.put("serverType", serverType.toString());
		r.put("serverURL", serverURL);
		r.put("eventType", getEventType().toString());
		r.put("project", project);
		if (cause != null) r.put("cause", cause.toJSON());
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		JSONArray tmp;
		JSONObject causeJson;
		CIEventCauseType causeType;
		serverType = CIServerType.getByValue(json.getString("serverType"));
		serverURL = json.getString("serverURL");
		project = json.getString("project");
		if (json.has("cause")) {
			causeJson = json.getJSONObject("cause");
			causeType = CIEventCauseType.getByValue(causeJson.getString("type"));
			if (causeType == CIEventCauseType.SCM) {
				cause = new CIEventSCMCause(causeJson);
			} else if (causeType == CIEventCauseType.USER) {
				cause = new CIEventUserCause(causeJson);
			} else if (causeType == CIEventCauseType.UPSTREAM) {
				cause = new CIEventUpstreamCause(causeJson);
			}
		}
	}
}