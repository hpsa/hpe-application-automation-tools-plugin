package com.hp.octane.plugins.jenkins.model.causes;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.*;
import hudson.model.Cause;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */

public final class CIEventCausesFactory {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public static CIEventCause[] processCauses(List<? extends Cause> causes) {
		Cause tmpCause;
		CIEventCause[] result = null;
		Cause.UserIdCause tmpUserCause;
		Cause.UpstreamCause tmpUpstreamCause;

		if (causes != null && causes.size() > 0) {
			result = new CIEventCause[causes.size()];
			for (int i = 0; i < result.length; i++) {
				tmpCause = causes.get(i);
				result[i] = dtoFactory.newDTO(CIEventCause.class);
				if (tmpCause instanceof SCMTrigger.SCMTriggerCause) {
					result[i].setType(CIEventCauseType.SCM);
				} else if (tmpCause instanceof TimerTrigger.TimerTriggerCause) {
					result[i].setType(CIEventCauseType.TIMER);
				} else if (tmpCause instanceof Cause.UserIdCause) {
					tmpUserCause = (Cause.UserIdCause) tmpCause;
					result[i].setType(CIEventCauseType.USER);
					result[i].setUser(tmpUserCause.getUserId());
				} else if (tmpCause instanceof Cause.UpstreamCause) {
					tmpUpstreamCause = (Cause.UpstreamCause) tmpCause;
					result[i].setType(CIEventCauseType.UPSTREAM);
					result[i].setProject(tmpUpstreamCause.getUpstreamProject());
					result[i].setNumber(tmpUpstreamCause.getUpstreamBuild());
					result[i].setCauses(processCauses(tmpUpstreamCause.getUpstreamCauses()));
				} else {
					result[i].setType(CIEventCauseType.UNDEFINED);
				}
			}
		}
		return result;
	}
}
