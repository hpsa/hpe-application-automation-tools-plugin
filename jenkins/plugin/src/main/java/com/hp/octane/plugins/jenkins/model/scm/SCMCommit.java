package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SCMCommit {

	@ExportedBean
	public static class User {
		private String nickName;
		private String fullName;
		private String email;

		User(String nickName, String fullName, String email) {
			this.nickName = nickName;
			this.fullName = fullName;
			this.email = email;
		}

		@Exported(inline = true)
		public String getNickName() {
			return nickName;
		}

		@Exported(inline = true)
		public String getFullName() {
			return fullName;
		}

		@Exported(inline = true)
		public String getEmail() {
			return email;
		}
	}

	@ExportedBean
	public static class Change {
		private String type;
		private String file;

		Change(String type, String file) {
			this.type = type;
			this.file = file;
		}

		@Exported(inline = true)
		public String getType() {
			return type;
		}

		@Exported(inline = true)
		public String getFile() {
			return file;
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

	@Exported(inline = true)
	public String getId() {
		return id;
	}

	@Exported(inline = true)
	public String getComment() {
		return comment;
	}

	@Exported(inline = true)
	public long getTime() {
		return time;
	}

	@Exported(inline = true)
	public User getUser() {
		return user;
	}

	@Exported(inline = true)
	public Change[] getChanges() {
		return changes.toArray(new Change[changes.size()]);
	}
}
