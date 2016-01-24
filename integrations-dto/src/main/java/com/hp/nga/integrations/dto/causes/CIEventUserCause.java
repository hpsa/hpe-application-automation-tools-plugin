package com.hp.nga.integrations.dto.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */

public class CIEventUserCause implements CIEventCauseBase {
	private String userId = "";
	private String userName = "";

	public CIEventUserCause(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	public CIEventCauseType getType() {
		return CIEventCauseType.USER;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}
}
