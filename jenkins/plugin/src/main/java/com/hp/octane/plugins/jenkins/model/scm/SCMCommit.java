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

	private final Long time;
	private final String user;
	private final String revId;
	private final String parentRevId;
	private final String comment;
	private final ArrayList<Change> changes;

	public SCMCommit(Long time, String user, String revId, String parentRevId, String comment) {
		this.time = time;
		this.user = user;
		this.revId = revId;
		this.parentRevId = parentRevId;
		this.comment = comment;
		this.changes = new ArrayList<Change>();
	}

	public void addChange(String type, String file) {
		changes.add(new Change(type, file));
	}

	@Exported(inline = true)
	public Long getTime() {
		return time;
	}

	@Exported(inline = true)
	public String getUser() {
		return user;
	}

	@Exported(inline = true)
	public String getRevId() {
		return revId;
	}

	@Exported(inline = true)
	public String getParentRevId() {
		return parentRevId;
	}

	@Exported(inline = true)
	public String getComment() {
		return comment;
	}

	@Exported(inline = true)
	public Change[] getChanges() {
		return changes.toArray(new Change[changes.size()]);
	}
}
