package com.hp.application.automation.tools.pipelineSteps;

import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.application.automation.tools.run.SseBuilder;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import java.util.HashMap;

/**
 * Execution for SseBuildAndPublish
 * Created by Roy Lu on 10/20/2016.
 */
public class SseBuilderPublishResultStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient SseBuildAndPublishStep step;

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    @Override
    protected Void run() throws Exception {
        listener.getLogger().println("Execute HP tests using HP ALM Lab Management");

        SseBuilder sseBuilder = step.getSseBuilder();
        RunResultRecorder runResultRecorder = step.getRunResultRecorder();

        String archiveTestResultsMode = runResultRecorder.getResultsPublisherModel().getArchiveTestResultsMode();

        sseBuilder.perform(build, ws, launcher, listener);

        if (StringUtils.isNotBlank(archiveTestResultsMode)) {
            listener.getLogger().println("Publish HP tests result");

            HashMap<String, String> resultFilename = new HashMap<String, String>(0);
            resultFilename.put(RunFromFileBuilder.class.getName(), sseBuilder.getRunResultsFileName());

            listener.getLogger().println("Publish HP tests result");
            runResultRecorder.pipelinePerform(build, ws, launcher, listener, resultFilename);
        }
        return null;
    }
}
