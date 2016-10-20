package com.hp.application.automation.tools.pipelineSteps;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.hp.application.automation.tools.model.*;
import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.SseBuilder;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

/**
 * A pipeline step that combined with ssebuilder and runresultrecorder.
 * Created by llu4 on 10/20/2016.
 */
public class SseBuildAndPublishStep extends AbstractStepImpl {

    private SseBuilder sseBuilder;
    private RunResultRecorder runResultRecorder;

    /**
     * Get the ssebuilder
     * @return sseBuilder
     */
    public SseBuilder getSseBuilder() {
        return sseBuilder;
    }

    /**
     * Get the runResultRecorder
     * @return runResultRecorder
     */
    public RunResultRecorder getRunResultRecorder() {
        return runResultRecorder;
    }

    @DataBoundConstructor
    public SseBuildAndPublishStep(String almServerName,
                      String almProject,
                      String credentialsId,
                      String almDomain,
                      String runType,
                      String almEntityId,
                      String timeslotDuration,
                      String archiveTestResultsMode) {

        sseBuilder = new SseBuilder(almServerName,
                almProject,
                credentialsId,
                almDomain,
                runType,
                almEntityId,
                timeslotDuration);

        runResultRecorder = new RunResultRecorder(true, archiveTestResultsMode);
    }

    @DataBoundSetter
    public void setDescription(String description) {
        sseBuilder.setDescription(description);
    }

    @DataBoundSetter
    public void setPostRunAction(String postRunAction) {
        sseBuilder.setPostRunAction(postRunAction);
    }

    @DataBoundSetter
    public void setEnvironmentConfigurationId(String environmentConfigurationId) {
        sseBuilder.setEnvironmentConfigurationId(environmentConfigurationId);
    }

    @DataBoundSetter
    public void setCdaDetails(CdaDetails cdaDetails) {
        sseBuilder.setCdaDetails(cdaDetails);
    }

    @DataBoundSetter
    public void setProxySettings(SseProxySettings proxySettings) {
        sseBuilder.setProxySettings(proxySettings);
    }

    // This indicates to Jenkins that this is an implementation of an extension point
    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            super(SseBuilderPublishResultStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "sseBuildAndPublish";
        }

        @Override
        public String getDisplayName() {
            return "Execute HP tests using HP ALM Lab Management and Publish HP tests result";
        }

        public boolean hasAlmServers() {

            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).hasAlmServers();
        }

        public AlmServerSettingsModel[] getAlmServers() {
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
        }

        public FormValidation doCheckTimeslotDuration(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Timeslot duration must be set");
            }

            String val1 = value.trim();

            if (!StringUtils.isNumeric(val1)) {
                return FormValidation.error("Timeslot duration must be a number");
            }

            if (Integer.valueOf(val1) < 30) {
                return FormValidation.error("Timeslot duration must be higher than 30");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckAlmDomain(@QueryParameter String value) {

            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Domain must be set");
            }

            return ret;
        }

        public FormValidation doCheckAlmProject(@QueryParameter String value) {

            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Project must be set");
            }

            return ret;
        }

        public FormValidation doCheckAlmEntityId(@QueryParameter String value) {

            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Entity ID must be set.");
            }

            return ret;
        }

        public List<EnumDescription> getRunTypes() {

            return SseModel.getRunTypes();
        }

        public List<EnumDescription> getPostRunActions() {

            return SseModel.getPostRunActions();
        }

        public List<EnumDescription> getDeploymentActions() {

            return CdaDetails.getDeploymentActions();
        }

        public static List<EnumDescription> getDeprovisioningActions() {

            return CdaDetails.getDeprovisioningActions();
        }

        /**
         * To fill in the credentials drop down list which's field is 'credentialsId'.
         * This method's name works with tag <c:select/>.
         */
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project,
                                                     @QueryParameter String credentialsId) {

            if (project == null || !project.hasPermission(Item.CONFIGURE)) {
                return new StandardUsernameListBoxModel().includeCurrentValue(credentialsId);
            }
            return new StandardUsernameListBoxModel()
                    .includeEmptyValue()
                    .includeAs(
                            project instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) project) : ACL.SYSTEM,
                            project,
                            StandardUsernamePasswordCredentials.class,
                            URIRequirementBuilder.create().build())
                    .includeCurrentValue(credentialsId);
        }

        /**
         * To fill in the credentials drop down list which's field is 'FsProxyCredentialsId'.
         */
        public ListBoxModel doFillFsProxyCredentialsIdItems(@AncestorInPath Item project,
                                                            @QueryParameter String credentialsId) {
            return doFillCredentialsIdItems(project, credentialsId);
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item project,
                                                   @QueryParameter String url,
                                                   @QueryParameter String value) {
            if (project == null || !project.hasPermission(Item.EXTENDED_READ)) {
                return FormValidation.ok();
            }

            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }

            url = Util.fixEmptyAndTrim(url);
            if (url == null)
            // not set, can't check
            {
                return FormValidation.ok();
            }

            if (url.indexOf('$') >= 0)
            // set by variable, can't check
            {
                return FormValidation.ok();
            }

            for (ListBoxModel.Option o : CredentialsProvider.listCredentials(
                    StandardUsernamePasswordCredentials.class,
                    project,
                    project instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) project) : ACL.SYSTEM,
                    URIRequirementBuilder.create().build(),
                    new IdMatcher(value))) {

                if (StringUtils.equals(value, o.value)) {
                    return FormValidation.ok();
                }
            }
            // no credentials available, can't check
            return FormValidation.warning("Cannot find any credentials with id " + value);
        }
    }
}
