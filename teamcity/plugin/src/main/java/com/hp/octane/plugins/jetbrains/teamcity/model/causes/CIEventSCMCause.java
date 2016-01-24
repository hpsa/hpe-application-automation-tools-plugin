package com.hp.octane.plugins.jetbrains.teamcity.model.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */

public class CIEventSCMCause implements CIEventCauseBase {
	private CIEventCauseType type = CIEventCauseType.SCM;

	@Override
	public CIEventCauseType getType() {
		return type;
	}
}
