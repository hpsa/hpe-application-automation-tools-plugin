package com.hp.octane.plugins.jenkins.apis;

import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 26/08/14
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public interface IJSONable {

	JSONObject toJSON();

	void fromJSON(JSONObject json);

}
