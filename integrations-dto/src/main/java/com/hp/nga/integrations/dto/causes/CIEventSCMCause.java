package com.hp.nga.integrations.dto.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */

public class CIEventSCMCause implements CIEventCauseBase {
	public CIEventCauseType getType() {
		return CIEventCauseType.SCM;
	}
}
