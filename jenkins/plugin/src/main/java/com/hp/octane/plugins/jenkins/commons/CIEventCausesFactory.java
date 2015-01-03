package com.hp.octane.plugins.jenkins.commons;

import com.hp.octane.plugins.jenkins.model.causes.*;
import hudson.model.Cause;
import hudson.triggers.SCMTrigger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */
public final class CIEventCausesFactory {
	public static CIEventCauseBase convertCause(List<Cause> causes) {
		Cause tmpCause;
		CIEventCauseBase cause = null;
		Cause.UserIdCause tmpUserCause;
		Cause.UpstreamCause tmpUpstreamCause;

		//  IMPORTANT: my current decision is that only one (first) cause should be referred to,
		//      actually i've never seen more than one cause in the build's causes list
		//      but probably in future this assumption will require a revision.

		if (causes != null && causes.size() > 0) {
			tmpCause = causes.get(0);
			if (tmpCause instanceof SCMTrigger.SCMTriggerCause) {
				cause = new CIEventSCMCause();
			} else if (tmpCause instanceof Cause.UserIdCause) {
				tmpUserCause = (Cause.UserIdCause) tmpCause;
				cause = new CIEventUserCause(tmpUserCause.getUserId(), tmpUserCause.getUserName());
			} else if (tmpCause instanceof Cause.UpstreamCause) {
				tmpUpstreamCause = (Cause.UpstreamCause) tmpCause;
				cause = new CIEventUpstreamCause(
						tmpUpstreamCause.getUpstreamProject(),
						tmpUpstreamCause.getUpstreamBuild(),
						convertCause(tmpUpstreamCause.getUpstreamCauses())
				);
			}
		}
		return cause;
	}
}
