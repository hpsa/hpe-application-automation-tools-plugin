package com.hp.octane.plugins.jenkins.model;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.*;
import hudson.model.Cause;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;

import java.util.LinkedList;
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

	public static List<CIEventCause> processCauses(List<? extends Cause> causes) {
		List<CIEventCause> result = new LinkedList<CIEventCause>();
		CIEventCause tmpResultCause;
		Cause tmpCause;
		Cause.UserIdCause tmpUserCause;
		Cause.UpstreamCause tmpUpstreamCause;

		if (causes != null) {
			for (int i = 0; i < result.size(); i++) {
				tmpCause = causes.get(i);
				tmpResultCause = dtoFactory.newDTO(CIEventCause.class);
				if (tmpCause instanceof SCMTrigger.SCMTriggerCause) {
					tmpResultCause.setType(CIEventCauseType.SCM);
				} else if (tmpCause instanceof TimerTrigger.TimerTriggerCause) {
					tmpResultCause.setType(CIEventCauseType.TIMER);
				} else if (tmpCause instanceof Cause.UserIdCause) {
					tmpUserCause = (Cause.UserIdCause) tmpCause;
					tmpResultCause.setType(CIEventCauseType.USER);
					tmpResultCause.setUser(tmpUserCause.getUserId());
				} else if (tmpCause instanceof Cause.UpstreamCause) {
					tmpUpstreamCause = (Cause.UpstreamCause) tmpCause;
					tmpResultCause.setType(CIEventCauseType.UPSTREAM);
					tmpResultCause.setProject(tmpUpstreamCause.getUpstreamProject());
					tmpResultCause.setBuildCiId(String.valueOf(tmpUpstreamCause.getUpstreamBuild()));
					tmpResultCause.setCauses(processCauses(tmpUpstreamCause.getUpstreamCauses()));
				} else {
					tmpResultCause.setType(CIEventCauseType.UNDEFINED);
				}
				result.add(tmpResultCause);
			}
		}
		return result;
	}
}
