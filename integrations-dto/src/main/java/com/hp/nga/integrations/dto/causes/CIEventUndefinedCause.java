package com.hp.nga.integrations.dto.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 21/05/15
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */

public class CIEventUndefinedCause implements CIEventCauseBase {
	public CIEventCauseType getType() {
		return CIEventCauseType.UNDEFINED;
	}
}
