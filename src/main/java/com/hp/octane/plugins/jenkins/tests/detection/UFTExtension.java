package com.hp.octane.plugins.jenkins.tests.detection;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.tasks.Builder;

@Extension
public class UFTExtension extends ResultFieldsDetectionExtension {

    public static final String UFT = "UFT";

    public static final String RUN_FROM_FILE_BUILDER = "com.hp.application.automation.tools.run.RunFromFileBuilder";
    public static final String RUN_FROM_ALM_BUILDER = "com.hp.application.automation.tools.run.RunFromAlmBuilder";

    @Override
    public ResultFields detect(final Run build) {

        if (build.getParent() instanceof FreeStyleProject) {
            for (Builder builder : ((FreeStyleProject) build.getParent()).getBuilders()) {
                if (builder.getClass().getName().equals(RUN_FROM_FILE_BUILDER) || builder.getClass().getName().equals(RUN_FROM_ALM_BUILDER)) {
                        return new ResultFields(UFT, UFT, null);
                }
            }
        }
        return null;
    }
}
