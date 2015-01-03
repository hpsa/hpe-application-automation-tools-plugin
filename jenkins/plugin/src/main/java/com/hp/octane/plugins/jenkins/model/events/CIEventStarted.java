package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class CIEventStarted extends CIEventBase implements IJSONable {
	public final CIEventType eventType = CIEventType.STARTED;
	public int number;
	public long startTime;
	public long estimatedDuration;

	public CIEventStarted(CIServerType serverType, String serverURL, String project, int number, long startTime, long estimatedDuration, CIEventCauseBase cause) {
		super(serverType, serverURL, project, cause);
		this.number = number;
		this.startTime = startTime;
		this.estimatedDuration = estimatedDuration;
	}

	public CIEventStarted(JSONObject json) {
		super(CIServerType.getByValue(json.getString("serverType")), json.getString("serverURL"));
		fromJSON(json);
	}

	@Override
	public CIEventType getEventType() {
		return eventType;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = super.toJSON();
		r.put("number", number);
		r.put("startTime", startTime);
		r.put("estimatedDuration", estimatedDuration);
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		number = json.getInt("number");
		startTime = json.getLong("startTime");
		estimatedDuration = json.getLong("estimatedDuration");
	}
}
