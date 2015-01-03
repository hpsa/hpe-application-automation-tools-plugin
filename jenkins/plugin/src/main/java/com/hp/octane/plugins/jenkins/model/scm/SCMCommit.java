package com.hp.octane.plugins.jenkins.model.scm;

import com.hp.octane.plugins.jenkins.apis.IJSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */
public class SCMCommit implements IJSONable {

	class User implements IJSONable {
		private String nickName;
		private String fullName;
		private String email;

		User(String nickName, String fullName, String email) {
			this.nickName = nickName;
			this.fullName = fullName;
			this.email = email;
		}

		User(JSONObject json) {
			this.fromJSON(json);
		}

		@Override
		public JSONObject toJSON() {
			JSONObject r = new JSONObject();
			r.put("nickName", nickName);
			r.put("fullName", fullName);
			r.put("email", email);
			return r;
		}

		@Override
		public void fromJSON(JSONObject json) {
			this.nickName = json.getString("nickName");
			this.fullName = json.getString("fullName");
			this.email = json.getString("email");
		}
	}

	class Change implements IJSONable {
		private String type;
		private String file;

		Change(String type, String file) {
			this.type = type;
			this.file = file;
		}

		Change(JSONObject json) {
			this.fromJSON(json);
		}

		@Override
		public JSONObject toJSON() {
			JSONObject r = new JSONObject();
			r.put("type", type);
			r.put("file", file);
			return r;
		}

		@Override
		public void fromJSON(JSONObject json) {
			this.type = json.getString("type");
			this.file = json.getString("file");
		}
	}

	private String id;
	private String comment;
	private long time;
	private User user;
	private ArrayList<Change> changes;

	public SCMCommit(String id, String comment, long time) {
		this.id = id;
		this.comment = comment;
		this.time = time;
		this.user = null;
		this.changes = new ArrayList<Change>();
	}

	public SCMCommit(JSONObject json) {
		this.fromJSON(json);
	}

	public void setUser(String nickName, String fullName, String email) {
		user = new User(nickName, fullName, email);
	}

	public void addChange(String type, String file) {
		changes.add(new Change(type, file));
	}

	@Override
	public JSONObject toJSON() {
		JSONObject r = new JSONObject();
		JSONArray tmp = new JSONArray();
		r.put("id", id);
		r.put("comment", comment);
		r.put("time", time);
		r.put("user", user.toJSON());
		for (Change change : changes) {
			tmp.put(change.toJSON());
		}
		r.put("changes", tmp);
		return r;
	}

	@Override
	public void fromJSON(JSONObject json) {
		JSONArray tmp;
		this.id = json.getString("id");
		this.comment = json.getString("comment");
		this.time = json.getLong("time");
		this.user = new User(json.getJSONObject("user"));
		changes = new ArrayList<Change>();
		tmp = json.getJSONArray("changes");
		for (int i = 0; i < tmp.length(); i++) {
			changes.add(new Change(tmp.getJSONObject(i)));
		}
	}
}
