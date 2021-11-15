package com.microfocus.application.automation.tools.run;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.octane.executor.UftConstants;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.tests.HPRunnerType;
import hudson.model.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class UftOctaneUtils {
    /**
     * This step is important for integration with Octane when job is executed as workflow job.
     * Our plugin can recognize UFT build step when its executed in context of freeStyle job, but its not possible to do it
     * when this step executed in workflow job.
     * So , in this method we add parameter of RunnerType.UFT to sign this job as UFT runner.
     * @param build
     * @param listener
     */
    public static void setUFTRunnerTypeAsParameter(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener) {
        if (JobProcessorFactory.WORKFLOW_RUN_NAME.equals(build.getClass().getName()) && OctaneSDK.hasClients()) {
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
