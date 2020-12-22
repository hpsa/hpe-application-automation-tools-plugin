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

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.octane.configuration.FodConfigUtil;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.configuration.SSCServerConfigUtil;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.lang.reflect.InvocationTargetException;

/**
 * Jenkins events life cycle listener for processing vulnerabilities scan results on build completed
 */

@Extension
public class VulnerabilitiesWorkflowListener implements GraphListener {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(VulnerabilitiesWorkflowListener.class);

    @Override
    public void onNewHead(FlowNode flowNode) {
        if (!OctaneSDK.hasClients()) {
            return;
        }
        try {
            if (BuildHandlerUtils.isWorkflowEndNode(flowNode)) {
                sendPipelineFinishedEvent((FlowEndNode) flowNode);
            }
        } catch (Exception e) {
            logger.error("failed to build and/or dispatch STARTED/FINISHED event for " + flowNode, e);
        }
    }

    protected void sendPipelineFinishedEvent(FlowEndNode flowEndNode) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        WorkflowRun parentRun = BuildHandlerUtils.extractParentRun(flowEndNode);

        if (!OctaneSDK.hasClients()) {
            return;
        }

        SSCServerConfigUtil.SSCProjectVersionPair projectVersionPair = SSCServerConfigUtil.getProjectConfigurationFromWorkflowRun(parentRun);
        if (!VulnerabilitiesUtils.insertQueueItem(parentRun, projectVersionPair)) return;

        Long release = FodConfigUtil.getFODReleaseFromRun(parentRun);
        if (release != null) {
            logger.info("FOD configuration was found in " + parentRun);
            VulnerabilitiesUtils.insertFODQueueItem(parentRun, release);
        }
        if (projectVersionPair == null && release == null) {
            logger.debug("No Security Scan integration configuration was found " + parentRun);
        }
    }
}
