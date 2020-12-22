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
import com.hp.octane.integrations.dto.scm.SCMData;
import com.microfocus.application.automation.tools.octane.model.processors.scm.SCMProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.scm.SCMUtils;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Run Listener that handles SCM CI events and dispatches notifications to the Octane server
 * Created by gullery on 10/07/2016.
 */

@Extension
public class SCMListenerOctaneImpl extends SCMListener {

    @Override
    public void onChangeLogParsed(Run<?, ?> run, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
        if (!OctaneSDK.hasClients()) {
            return;
        }
        super.onChangeLogParsed(run, scm, listener, changelog);

        String jobCiId = BuildHandlerUtils.getJobCiId(run);
        String buildCiId = BuildHandlerUtils.getBuildCiId(run);

        SCMData scmData = SCMUtils.extractSCMData(run, scm, SCMProcessors.getAppropriate(scm.getClass().getName()));


        if (scmData != null) {
            String parents = BuildHandlerUtils.getRootJobCiIds(run);
            SCMUtils.persistSCMData(run, jobCiId, buildCiId, scmData);
            OctaneSDK.getClients().forEach(octaneClient ->
                    octaneClient.getSCMDataService().enqueueSCMData(jobCiId, buildCiId, scmData, parents));
        }
    }
}
