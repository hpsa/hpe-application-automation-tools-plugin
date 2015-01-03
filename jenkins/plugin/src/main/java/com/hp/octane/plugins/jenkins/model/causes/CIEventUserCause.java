package com.hp.octane.plugins.jenkins.model.causes;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class CIEventUserCause extends CIEventCauseBase implements IJSONable {
	public final CIEventCauseType type = CIEventCauseType.USER;
	public String userId = "";
	public String userName = "";

	public CIEventUserCause(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	public CIEventUserCause(JSONObject json) {
		fromJSON(json);
	}

	@Override
	CIEventCauseType getType() {
		return type;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = super.toJSON();
		r.put("userId", userId);
		r.put("userName", userName);
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		if (json.has("userId")) userId = json.getString("userId");
		if (json.has("userName")) userName = json.getString("userName");
	}
}
