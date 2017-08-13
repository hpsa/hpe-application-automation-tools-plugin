/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.events;

import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hpe.application.automation.tools.octane.model.CIEventCausesFactory;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class QueueListenerImpl extends QueueListener {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public void onEnterWaiting(Queue.WaitingItem wi) {
		if(!ConfigurationService.getServerConfiguration().isValid()){
			return;
		}

		AbstractProject project;
		if (wi.task instanceof AbstractProject) {
			project = (AbstractProject) wi.task;
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.QUEUED)
					.setProject(project.getName())
					.setProjectDisplayName(project.getName())
					.setCauses(CIEventCausesFactory.processCauses(wi.getCauses()));
			//  REMARK: temporary decided to not send QUEUED event
			//EventsDispatcher.getExtensionInstance().dispatchEvent(event);
		}
	}
}
