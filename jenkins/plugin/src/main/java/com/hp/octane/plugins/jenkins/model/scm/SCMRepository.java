package com.hp.octane.plugins.jenkins.model.scm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public class SCMRepository {
	private SCMType type;
	private String uri;
	private String builtBranch;
	private String builtCommitRev;
	private ArrayList<SCMCommit> commits;

	public SCMRepository(SCMType type, String uri, String builtCommitRev, String builtBranch) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = builtBranch;
		this.builtCommitRev = builtCommitRev;
		this.commits = new ArrayList<SCMCommit>();
	}

	public SCMRepository(SCMType type, String uri) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = null;
		this.builtCommitRev = null;
		this.commits = new ArrayList<SCMCommit>();
	}

	public SCMRepository(JSONObject json) {
		this.fromJSON(json);
	}

	public void addCommit(SCMCommit commit) {
		commits.add(commit);
	}

	public SCMType getType() {
		return type;
	}

	public String getUri() {
		return uri;
	}

	public String getBuiltCommitRev() {
		return builtCommitRev;
	}

	public String getBuiltBranch() {
		return builtBranch;
	}

	public JSONObject toJSON() {
		JSONObject r = new JSONObject();
		JSONArray tmp = new JSONArray();
		r.put("type", type.toString());
		r.put("uri", uri);
		r.put("builtBranch", builtBranch);
		r.put("builtCommitRev", builtCommitRev);
		if (commits.size() > 0) {
			for (SCMCommit commit : commits) {
				tmp.put(commit.toJSON());
			}
			r.put("commits", tmp);
		}
		return r;
	}

	public void fromJSON(JSONObject json) {
		JSONArray tmp;
		this.type = SCMType.getByValue(json.getString("type"));
		this.uri = json.getString("uri");
		this.builtBranch = json.getString("builtBranch");
		this.builtCommitRev = json.getString("builtCommitRev");
		commits = new ArrayList<SCMCommit>();
		if (json.has("commits")) {
			tmp = json.getJSONArray("commits");
			for (int i = 0; i < tmp.length(); i++) {
				commits.add(new SCMCommit(tmp.getJSONObject(i)));
			}
		}
	}
}
