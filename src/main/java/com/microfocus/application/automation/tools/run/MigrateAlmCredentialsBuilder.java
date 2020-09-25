package com.microfocus.application.automation.tools.run;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microfocus.application.automation.tools.model.*;
import com.microfocus.application.automation.tools.settings.AlmServerSettingsBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.boon.collections.MultiMap;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class MigrateAlmCredentialsBuilder extends Recorder implements Serializable, SimpleBuildStep {



    @DataBoundConstructor
    public MigrateAlmCredentialsBuilder() {}

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        scanJobs(taskListener);
    }

    public static void scanJobs(TaskListener listener) {
        List<FreeStyleProject> jobs = Jenkins.getInstanceOrNull().getAllItems(FreeStyleProject.class);

        List<AlmServerSettingsModel> models = Arrays.asList(Jenkins.getInstanceOrNull().getDescriptorByType(AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations());

        Multimap<String , String> serverUsernames = ArrayListMultimap.create();
        Multimap<String , String> serverClientIds = ArrayListMultimap.create();

        for (FreeStyleProject job : jobs) {
            List<Builder> builders = job.getBuilders();
            if (builders != null) {
                for (Builder builder : builders) {
                    if (builder instanceof RunFromAlmBuilder) {
                        listener.getLogger().println("Migrating credentials from task " + job.getDisplayName());
                        RunFromAlmBuilder almBuilder = (RunFromAlmBuilder) builder;
                        String almUsername = almBuilder.getAlmUserName();
                        String almPassword = almBuilder.getAlmPassword();
                        String almClientID = almBuilder.getAlmClientID();
                        String almApiKeySecret = almBuilder.getAlmApiKey();

                        for(AlmServerSettingsModel model : models){
                            if (model.getAlmServerName().equals(almBuilder.getAlmServerName())) {
                                if(almUsername != null && almUsername != "" && !serverUsernames.get(model.getAlmServerName()).contains(almUsername)) {
                                    serverUsernames.put(model.getAlmServerName(), almUsername);
                                    model.set_almCredentials(Arrays.asList(new CredentialsModel(almUsername, almPassword)));
                                    listener.getLogger().println("Migrating username " + almUsername  +" for " + model.getAlmServerName());
                                }

                                if(almClientID != null && almClientID != "" && !serverClientIds.get(model.getAlmServerName()).contains(almClientID)) {
                                    serverClientIds.put(model.getAlmServerName(), almClientID);
                                    model.set_almSSOCredentials(Arrays.asList(new SSOCredentialsModel(almClientID, almApiKeySecret)));
                                    listener.getLogger().println("Migrating client ID " + almClientID  +" for " + model.getAlmServerName());
                                }

                                try {
                                    job.save();
                                } catch (IOException e) {
                                    listener.getLogger().println("Job not saved.");
                                }

                                listener.getLogger().println("------------------------------");
                            }
                        }
                    }
                }
            }
        }
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {

            load();
        }

        @Override
        public String getDisplayName() {

            return "Migrate ALM Credentials";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {

            return true;
        }

    }
}
