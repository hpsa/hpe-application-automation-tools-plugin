package com.hp.octane.plugins.jetbrains.teamcity.model.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */


public class CIEventUserCause implements CIEventCauseBase {
	private CIEventCauseType type = CIEventCauseType.USER;
	private String userId = "";
	private String userName = "";
	public CIEventUserCause(String userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}

	@Override
	public CIEventCauseType getType() {
		return type;
	}


	public String getUserId() {
		return userId;
	}


	public String getUserName() {
		return userName;
	}
}
