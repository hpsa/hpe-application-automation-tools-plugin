//package com.hp.octane.plugins.jetbrains.teamcity.factories;
//
//import com.hp.octane.plugins.jetbrains.teamcity.model.causes.*;
//import hudson.model.Cause;
//import hudson.triggers.SCMTrigger;
//import hudson.triggers.TimerTrigger;
//
//import java.util.List;
//
///**
// * Created with IntelliJ IDEA.
// * User: gullery
// * Date: 20/10/14
// * Time: 18:19
// * To change this template use File | Settings | File Templates.
// */
//
//public final class CIEventCausesFactory {
//	public static CIEventCauseBase[] processCauses(List<? extends Cause> causes) {
//		Cause tmpCause;
//		CIEventCauseBase[] result = null;
//		Cause.UserIdCause tmpUserCause;
//		Cause.UpstreamCause tmpUpstreamCause;
//
//		if (causes != null && causes.size() > 0) {
//			result = new CIEventCauseBase[causes.size()];
//			for (int i = 0; i < result.length; i++) {
//				tmpCause = causes.get(i);
//				if (tmpCause instanceof SCMTrigger.SCMTriggerCause) {
//					result[i] = new CIEventSCMCause();
//				} else if (tmpCause instanceof TimerTrigger.TimerTriggerCause) {
//					result[i] = new CIEventTimerCause();
//				} else if (tmpCause instanceof Cause.UserIdCause) {
//					tmpUserCause = (Cause.UserIdCause) tmpCause;
//					result[i] = new CIEventUserCause(tmpUserCause.getUserId(), tmpUserCause.getUserName());
//				} else if (tmpCause instanceof Cause.UpstreamCause) {
//					tmpUpstreamCause = (Cause.UpstreamCause) tmpCause;
//					result[i] = new CIEventUpstreamCause(
//							tmpUpstreamCause.getUpstreamProject(),
//							tmpUpstreamCause.getUpstreamBuild(),
//							processCauses(tmpUpstreamCause.getUpstreamCauses())
//					);
//				} else {
//					result[i] = new CIEventUndefinedCause();
//				}
//			}
//		}
//		return result;
//	}
//}
