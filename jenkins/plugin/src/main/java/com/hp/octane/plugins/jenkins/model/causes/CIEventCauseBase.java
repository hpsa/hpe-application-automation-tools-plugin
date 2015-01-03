package com.hp.octane.plugins.jenkins.model.causes;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class CIEventCauseBase implements IJSONable {
	abstract CIEventCauseType getType();

	public JSONObject toJSON() {
		JSONObject r = new JSONObject();
		r.put("type", getType().toString());
		return r;
	}
}
