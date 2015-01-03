package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import com.hp.octane.plugins.jenkins.model.pipeline.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */
public class CIEventFinished extends CIEventBase implements IJSONable {
	public final CIEventType eventType = CIEventType.FINISHED;
	public int number;
	public SnapshotResult result;
	public long duration;
	public SCMData scmData;

	public CIEventFinished(CIServerType serverType, String serverURL, String project, int number, SnapshotResult result, long duration, SCMData scmData, CIEventCauseBase cause) {
		super(serverType, serverURL, project, cause);
		this.number = number;
		this.result = result;
		this.duration = duration;
		this.scmData = scmData;
	}

	public CIEventFinished(JSONObject json) {
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
		r.put("result", result.toString());
		r.put("duration", duration);
		if (scmData != null) r.put("scmData", scmData.toJSON());
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		number = json.getInt("number");
		result = SnapshotResult.getByValue(json.getString("result"));
		duration = json.getLong("duration");
		scmData = json.has("scmData") ? new SCMData(json.getJSONObject("scmData")) : null;
	}
}
