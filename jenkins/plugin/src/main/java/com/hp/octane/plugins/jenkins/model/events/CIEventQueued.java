package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
public class CIEventQueued extends CIEventBase implements IJSONable {
	public final CIEventType eventType = CIEventType.QUEUED;

	public CIEventQueued(CIServerType serverType, String serverURL, String project, CIEventCauseBase cause) {
		super(serverType, serverURL, project, cause);
	}

	public CIEventQueued(JSONObject json) {
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
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
	}
}
