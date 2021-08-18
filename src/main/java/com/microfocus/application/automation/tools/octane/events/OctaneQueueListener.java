package com.microfocus.application.automation.tools.octane.events;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import org.apache.logging.log4j.Logger;

@Extension
public class OctaneQueueListener extends QueueListener {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(OctaneQueueListener.class);

    @Override
    public void onLeft(Queue.LeftItem li) {
        if (!OctaneSDK.hasClients()) {
            return;
        }

        if (li.isCancelled()) {
            if (li.task instanceof AbstractProject) {
                try {
                    ParametersAction paramActions = li.getAction(ParametersAction.class);
                    AbstractProject project = (AbstractProject) li.task;
                    CIEvent event = DTOFactory.getInstance().newDTO(CIEvent.class)
                            .setEventType(CIEventType.REMOVED_FROM_QUEUE)
                            .setProject(JobProcessorFactory.getFlowProcessor(project).getTranslatedJobName())
                            .setBuildCiId("-1")
                            .setParameters(ParameterProcessors.getInstances(paramActions));
                    CIJenkinsServicesImpl.publishEventToRelevantClients(event);
                } catch (Exception e) {
                    logger.error("Failed to set REMOVED_FROM_QUEUE event :" + e.getMessage() + "(" + li.task.getName() + ")");
                }
            } else {
                logger.error("Job is cancelled in queue but it isn't AbstractProject :" + li.task.getFullDisplayName() + "(" + li.task.getClass().getName() + ")");
            }
        }
    }
}
