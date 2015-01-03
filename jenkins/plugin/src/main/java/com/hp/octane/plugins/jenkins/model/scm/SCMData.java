package com.hp.octane.plugins.jenkins.model.scm;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */
public class SCMData implements IJSONable {
	private ArrayList<SCMRepository> repositories;

	public SCMData(ArrayList<SCMRepository> repositories) {
		this.repositories = repositories;
	}

	public SCMData(JSONObject json) {
		this.fromJSON(json);
	}

	public ArrayList<SCMRepository> getRepositories() {
		return repositories;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = null;
		JSONArray tmp = new JSONArray();
		if (repositories.size() > 0) {
			r = new JSONObject();
			for (SCMRepository repository : repositories) {
				tmp.put(repository.toJSON());
			}
			r.put("repositories", tmp);
		}
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		JSONArray tmp;
		repositories = new ArrayList<SCMRepository>();
		if (json.has("repositories")) {
			tmp = json.getJSONArray("repositories");
			for (int i = 0; i < tmp.length(); i++) {
				repositories.add(new SCMRepository(tmp.getJSONObject(i)));
			}
		}
	}
}
