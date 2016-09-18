package com.hp.octane.plugins.bamboo.listener;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;

import java.util.ArrayList;
import java.util.List;

public class OctanePreChainAction extends BaseListener implements PreChainAction {

	public void execute(Chain chain, ChainExecution chainExecution) throws Exception {
		log.info("Executing chain " + chain.getName() + " build id "
				+ chainExecution.getBuildIdentifier().getBuildResultKey() + " build number "
				+ chainExecution.getBuildIdentifier().getBuildNumber());
		List<CIEventCause> causes = new ArrayList<CIEventCause>();
		CIEvent event = CONVERTER.getEventWithDetails(chainExecution.getPlanResultKey().getPlanKey().getKey(),
				chainExecution.getBuildIdentifier().getBuildResultKey(), chain.getName(), CIEventType.STARTED,
				chainExecution.getStartTime() != null ? chainExecution.getStartTime().getTime() : System.currentTimeMillis(),
				chainExecution.getAverageDuration(), causes,
				String.valueOf(chainExecution.getBuildIdentifier().getBuildNumber()));

		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}
}
