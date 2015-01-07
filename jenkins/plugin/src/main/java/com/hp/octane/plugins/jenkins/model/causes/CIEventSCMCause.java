package com.hp.octane.plugins.jenkins.model.causes;

import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */
public class CIEventSCMCause extends CIEventCauseBase {
	public final CIEventCauseType type = CIEventCauseType.SCM;

	public CIEventSCMCause() {
	}

	public CIEventSCMCause(JSONObject json) {
		fromJSON(json);
	}

	@Override
	CIEventCauseType getType() {
		return type;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = super.toJSON();
		return r;
	}

	public void fromJSON(JSONObject json) {
	}
}
