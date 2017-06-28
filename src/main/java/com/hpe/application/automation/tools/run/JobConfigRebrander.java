package com.hpe.application.automation.tools.run;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;


import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Created in order to fix previous builds that we're build with HP convention plugin and move them to HPE
 */
public class JobConfigRebrander  extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public JobConfigRebrander() {
    }

    /**
     * Run this step.
     *
     * @param run       a build this is running as a part of
     * @param workspace a workspace to use for any file operations
     * @param launcher  a way to start processes
     * @param listener  a place to send output
     * @throws InterruptedException if the step is interrupted
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        File root = Jenkins.getInstance().getRootDir();
        File projectsDir = new File(root,"jobs");
        File[] subdirs = projectsDir.listFiles();
        for (final File subdir : subdirs) {
            XmlFile xmlFile = new XmlFile(new File(subdir,"config.xml"));
            if (xmlFile.exists()) {
                convertHpToHpe(listener, xmlFile);
            }

            final File buildsFolder = new File(subdir, "builds");
            File[] builds = buildsFolder.listFiles();
            for(final File buildDir : builds){
                XmlFile buildXmlFile = new XmlFile(new File(buildDir,"build.xml"));
                if (buildXmlFile.exists()) {
                    convertHpToHpe(listener, buildXmlFile);
                }
            }

        }

    }

    private void convertHpToHpe(@Nonnull TaskListener listener, XmlFile confXmlFile) {
        try {
            String configuration = FileUtils.readFileToString(confXmlFile.getFile());
            String newConfiguration = StringUtils.replace(configuration, ".hp.", ".hpe.");
            FileUtils.writeStringToFile(confXmlFile.getFile(), newConfiguration);
        } catch (IOException e) {
            listener.error(
                    "failed to convert configuration 5.1 to new 5.2 format (hp to HPE): " + e.getMessage());
        }
    }


    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        public Descriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Fix old HPE Jenkins builds";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

    }

}
