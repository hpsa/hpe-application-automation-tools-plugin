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

package com.microfocus.application.automation.tools.pipelineSteps;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.octane.executor.UftConstants;
import com.microfocus.application.automation.tools.octane.tests.HPRunnerType;
import com.microfocus.application.automation.tools.run.RunFromFileBuilder;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The UFT pipeline step execution.
 */
public  class UftScenarioLoadStepExecution extends SynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = 1L;


    private transient final UftScenarioLoadStep step;


    private transient TaskListener listener;


    private transient FilePath ws;


    private transient Run build;


    private transient Launcher launcher;

    protected UftScenarioLoadStepExecution(@Nonnull StepContext context, UftScenarioLoadStep step) {
        super(context);
        this.step = step;
    }


    @Override
    protected Void run() throws Exception{
        ws = getContext().get(FilePath.class);
        listener = getContext().get(TaskListener.class);
        build = getContext().get(Run.class);
        launcher = getContext().get(Launcher.class);

        listener.getLogger().println("Running UftScenarioLoadStepExecution");

        setRunnerTypeAsParameter();

        step.getRunFromFileBuilder().perform(build, ws, launcher, listener);

        HashMap<String, String> resultFilename = new HashMap<String, String>(0);
        resultFilename.put(RunFromFileBuilder.class.getName(), step.getRunFromFileBuilder().getRunResultsFileName());

        step.getRunResultRecorder().pipelinePerform(build, ws, launcher, listener, resultFilename);

        return null;
    }


    private void setRunnerTypeAsParameter() {
        if (OctaneSDK.hasClients()) {
            listener.getLogger().println("Set HPRunnerType = HPRunnerType.UFT");
            ParametersAction parameterAction = build.getAction(ParametersAction.class);
            List<ParameterValue> newParams = (parameterAction != null) ? new ArrayList<>(parameterAction.getAllParameters()) : new ArrayList<>();
            newParams.add(new StringParameterValue(HPRunnerType.class.getSimpleName(), HPRunnerType.UFT.name()));
            ParametersAction newParametersAction = new ParametersAction(newParams);
            build.addOrReplaceAction(newParametersAction);

            if (parameterAction == null || parameterAction.getParameter(UftConstants.UFT_CHECKOUT_FOLDER) == null) {
                listener.getLogger().println("NOTE : If you need to integrate test results with an ALM Octane pipeline, and tests are located outside of the job workspace, define a parameter  " + UftConstants.UFT_CHECKOUT_FOLDER +
                        " with the path to the repository root in the file system. This enables ALM Octane to display the test name, rather than the full path to your test.");
                listener.getLogger().println("");
            }
        }
    }
}
