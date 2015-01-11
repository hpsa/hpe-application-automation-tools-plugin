package com.hp.octane.plugins.jenkins.model.scm;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */
public class SCMCommit {

	class User {
		private String nickName;
		private String fullName;
		private String email;

		User(String nickName, String fullName, String email) {
			this.nickName = nickName;
			this.fullName = fullName;
			this.email = email;
		}
	}

	class Change {
		private String type;
		private String file;

		Change(String type, String file) {
			this.type = type;
			this.file = file;
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

	public void setUser(String nickName, String fullName, String email) {
		user = new User(nickName, fullName, email);
	}

	public void addChange(String type, String file) {
		changes.add(new Change(type, file));
	}
}
