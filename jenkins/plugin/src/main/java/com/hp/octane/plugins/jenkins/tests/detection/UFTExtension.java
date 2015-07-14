package com.hp.octane.plugins.jenkins.tests.detection;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;

@Extension
public class UFTExtension extends ResultFieldsDetectionExtension {

    public static final String UFT = "UFT";

    private static final String RUN_FROM_FILE_BUILDER = "com.hp.application.automation.tools.run.RunFromFileBuilder";
    private static final String RUN_FROM_ALM_BUILDER = "com.hp.application.automation.tools.run.RunFromAlmBuilder";

    @Override
    public ResultFields detect(final AbstractBuild build) {

        if (build.getProject() instanceof FreeStyleProject) {
            for (Builder builder : ((FreeStyleProject) build.getProject()).getBuilders()) {
                if (builder.getClass().getName().equals(RUN_FROM_FILE_BUILDER) || builder.getClass().getName().equals(RUN_FROM_ALM_BUILDER)) {
                        return new ResultFields(UFT, UFT, null);
                }
            }
        }
        return null;
    }
}
