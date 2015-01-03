package com.hp.octane.plugins.jenkins.model.causes;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */
public class CIEventUpstreamCause extends CIEventCauseBase implements IJSONable {
	public final CIEventCauseType type = CIEventCauseType.UPSTREAM;
	public String project;
	public int number;
	public CIEventCauseBase cause;

	public CIEventUpstreamCause(String project, int number, CIEventCauseBase cause) {
		this.project = project;
		this.number = number;
		this.cause = cause;
	}

	public CIEventUpstreamCause(JSONObject json) {
		fromJSON(json);
	}

	@Override
	CIEventCauseType getType() {
		return type;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = super.toJSON();
		r.put("project", project);
		r.put("number", number);
		if (cause != null) r.put("cause", cause.toJSON());
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		JSONObject causeJson;
		CIEventCauseType causeType;
		project = json.getString("project");
		number = json.getInt("number");
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
