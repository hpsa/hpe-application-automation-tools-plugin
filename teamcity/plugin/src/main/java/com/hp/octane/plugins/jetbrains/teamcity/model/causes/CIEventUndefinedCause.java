package com.hp.octane.plugins.jetbrains.teamcity.model.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 21/05/15
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */


public class CIEventUndefinedCause implements CIEventCauseBase {

	@Override
	public CIEventCauseType getType() {
		return CIEventCauseType.UNDEFINED;
	}
}
