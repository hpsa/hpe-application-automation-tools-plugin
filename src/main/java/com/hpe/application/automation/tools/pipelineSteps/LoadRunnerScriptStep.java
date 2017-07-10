package com.hpe.application.automation.tools.pipelineSteps;

import com.hpe.application.automation.tools.run.RunLoadRunnerScript;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Logger;


public class LoadRunnerScriptStep extends AbstractStepImpl {
    private final RunLoadRunnerScript runLoadRunnerScript;

    @DataBoundConstructor
    public LoadRunnerScriptStep(String scriptPath) {
        this.runLoadRunnerScript = new RunLoadRunnerScript(scriptPath);
    }

    public String getScriptPath() {
        return this.runLoadRunnerScript.getScriptsPath();
    }

    /**
     * Gets run from file builder.
     *
     * @return the run from file builder
     */
    public RunLoadRunnerScript getRunLoadRunnerScript() {
        return this.runLoadRunnerScript;
    }

    /**
     * The type DescriptorImpl.
     */
    @Extension
    @Symbol("runLoadRunnerScript")
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        /**
         * Instantiates a new DescriptorImpl.
         */
        public DescriptorImpl() {
            super(LoadRunnerScriptStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "runLoadRunnerScript";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Run LoadRunner script";
        }

    }


    private static class LoadRunnerScriptStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        /**
         * Logger.
         */
        private static final Logger logger = Logger
                .getLogger(LoadRunnerScriptStepExecution.class.getName());
        private static final long serialVersionUID = 1L;
        @Inject
        @SuppressWarnings("squid:S3306")
        private transient LoadRunnerScriptStep step;
        @StepContextParameter
        private transient TaskListener listener;
        @StepContextParameter
        private transient FilePath ws;
        @StepContextParameter
        private transient Run build;
        @StepContextParameter
        private transient Launcher launcher;
        @StepContextParameter
        private transient EnvVars env;
        @StepContextParameter
        private transient Computer computer;
        @StepContextParameter
        private transient Node node;

        public LoadRunnerScriptStepExecution() {
            //No need for constructor
        }

        /**
         * Meat of the execution.
         * <p>
         * When this method returns, a step execution is over.
         */
        @Override
        protected Void run() throws Exception {

            listener.getLogger().println("Running LoadRunner Script Runner step");

            try {
                step.getRunLoadRunnerScript().perform(build, ws, launcher, listener, env);
            } catch (IOException e) {
                listener.fatalError("LoadRunner script runner stage encountered an IOException " + e);
                build.setResult(Result.FAILURE);
                return null;
            }

            return null;
        }

    }
}
