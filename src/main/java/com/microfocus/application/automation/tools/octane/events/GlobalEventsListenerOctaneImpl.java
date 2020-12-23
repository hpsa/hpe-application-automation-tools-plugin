/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.events;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.ItemType;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.executor.UftTestDiscoveryDispatcher;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import com.microfocus.application.automation.tools.settings.OctaneServerSettingsGlobalConfiguration;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

/**
 * Run Listener that handles basic CI item events and dispatches notifications to the Octane server
 * User: shitritn
 * Date: 12/06/18
 * Time: 09:33
 */

@Extension
public class GlobalEventsListenerOctaneImpl extends ItemListener {
	private static final Logger logger = SDKBasedLoggerProvider.getLogger(GlobalEventsListenerOctaneImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public void onLoaded() {
		logger.info("**********************************************************************");
		logger.info("********************STARTING JENKINS *********************************");
		logger.info("**********************************************************************");
		logger.info("Jenkins version " + Jenkins.getVersion());
		logger.info("Plugin version " + ConfigurationService.getPluginVersion());
		logger.info("CI SDK version " + OctaneSDK.SDK_VERSION);

		OctaneServerSettingsGlobalConfiguration.getInstance().initOctaneClients();
	}

	@Override
	public void onDeleted(Item item) {
		if(!OctaneSDK.hasClients()){
			return;
		}
		try {
			CIEvent event;
			if (item.getParent() != null && item.getParent().getClass().getName().equalsIgnoreCase(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
				event = dtoFactory.newDTO(CIEvent.class)
						.setEventType(CIEventType.DELETED)
						.setProject(JobProcessorFactory.getFlowProcessor((WorkflowJob) item).getTranslatedJobName());

				OctaneSDK.getClients().forEach(client -> client.getEventsService().publishEvent(event));
			}
		} catch (Throwable throwable) {
			logger.error("failed to build and/or dispatch DELETED event for " + item, throwable);
		}
	}

	@Override
	public void onBeforeShutdown() {
		OctaneSDK.getClients().forEach(OctaneSDK::removeClient);
		UftTestDiscoveryDispatcher dispatcher = Jenkins.get().getExtensionList(UftTestDiscoveryDispatcher.class).get(0);
		dispatcher.close();
	}

	@Override
	public void onLocationChanged(Item item, String oldFullName, String newFullName) {

		if (!OctaneSDK.hasClients()) {
			return;
		}

		boolean skip = JobProcessorFactory.isFolder(item) || JobProcessorFactory.isMultibranchChild(item);//for MultibranchChild - there is a logic in Octane that handle child on parent event
		logger.info("onLocationChanged '" + oldFullName + "' to '" + newFullName + "'" + (skip ? ". Skipped." : ""));
		if (skip) {
			return;
		}

		try {
			CIEvent event = dtoFactory.newDTO(CIEvent.class).setEventType(CIEventType.RENAMED);
			if (JobProcessorFactory.isJob(item)) {
				event.setItemType(ItemType.JOB);
			} else if (JobProcessorFactory.isMultibranch(item)) {
				event.setItemType(ItemType.MULTI_BRANCH);
			} else {
				logger.info("Cannot handle onLocationChanged for " + item.getClass().getName());
				return;
			}

			event.setProject(BuildHandlerUtils.translateFolderJobName(newFullName))
					.setProjectDisplayName(newFullName)
					.setPreviousProject(BuildHandlerUtils.translateFolderJobName(oldFullName))
					.setPreviousProjectDisplayName(oldFullName);

			CIJenkinsServicesImpl.publishEventToRelevantClients(event);
			OctaneSDK.getClients().forEach(c -> {
				if (c.getConfigurationService().removeFromOctaneRoots(event.getPreviousProject())) {
					c.getConfigurationService().addToOctaneRootsCache(event.getProject());
				}
			});
		} catch (Throwable throwable) {
			logger.error("failed to build and/or dispatch RENAMED event for " + item, throwable);
		}
	}
}
