package com.hp.octane.plugins.jenkins.model.causes;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class CIEventUserCause implements CIEventCauseBase {
	private final CIEventCauseType type = CIEventCauseType.USER;
	private String userId = "";
	private String userName = "";

	public CIEventUserCause(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	@Override
	@Exported(inline = true)
	public CIEventCauseType getType() {
		return type;
	}

	@Exported(inline = true)
	public String getUserId() {
		return userId;
	}

	@Exported(inline = true)
	public String getUserName() {
		return userName;
	}
}
