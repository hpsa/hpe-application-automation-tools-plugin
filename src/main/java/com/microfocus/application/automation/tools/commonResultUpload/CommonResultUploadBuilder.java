/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microfocus.application.automation.tools.commonResultUpload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.microfocus.application.automation.tools.commonResultUpload.uploader.Uploader;
import com.microfocus.application.automation.tools.model.AlmServerSettingsModel;
import com.microfocus.application.automation.tools.settings.AlmServerSettingsBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.VariableResolver;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.microfocus.application.automation.tools.Messages.CommonResultUploadBuilderName;

public class CommonResultUploadBuilder extends Recorder implements SimpleBuildStep {

    @DataBoundConstructor
    public CommonResultUploadBuilder(
            String almServerName, String credentialsId, String almDomain,
            String clientType, String almProject, String almTestFolder,
            String almTestSetFolder, String testingResultFile,
            String runStatusMapping, String fieldMapping) {

        this.almServerName = almServerName;
        this.credentialsId = credentialsId;
        this.almDomain = almDomain;
        this.clientType = clientType;
        this.almProject = almProject;
        this.almTestFolder = almTestFolder;
        this.almTestSetFolder = almTestSetFolder;
        this.testingResultFile = testingResultFile;
        this.runStatusMapping = runStatusMapping;
        this.fieldMapping = fieldMapping;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener)
            throws InterruptedException, IOException {

        CommonUploadLogger logger = new CommonUploadLogger(taskListener.getLogger());
        UsernamePasswordCredentials credentials = getCredentialsById(credentialsId, run, logger);
        VariableResolver<String> varResolver = new VariableResolver.ByMap<String>(run.getEnvironment(taskListener));

        Map<String, String> params = new HashMap<String, String>();
        params.put("almServerName", almServerName);
        params.put("almServerUrl", getAlmServerUrl(almServerName));
        params.put("username", credentials.getUsername());
        params.put("password", credentials.getPassword().getPlainText());
        params.put("almDomain", Util.replaceMacro(almDomain, varResolver));
        params.put("clientType", clientType);
        params.put("almProject", Util.replaceMacro(almProject, varResolver));
        params.put("almTestFolder", almTestFolder);
        params.put("almTestSetFolder", almTestSetFolder);
        params.put("runStatusMapping", runStatusMapping);
        params.put("testingResultFile", testingResultFile);
        params.put("fieldMapping", fieldMapping);

        Uploader uploader = new Uploader(run, logger, params);
        uploader.upload();

        // Output failed uploaded entities.
        if (logger.getErrorCount() > 0) {
            if (!logger.getFailedEntityNames().isEmpty()) {
                logger.log("Failed upload summary:");
                for (String failedEntityName : logger.getFailedEntityNames()) {
                    logger.log("Upload (" + failedEntityName + ") failed.");
                }
            }
            run.setResult(Result.UNSTABLE);
        }
    }

    private String getAlmServerUrl(String almServerName) {
        AlmServerSettingsModel[] almServers = Hudson.getInstance().getDescriptorByType(
                AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
        if (almServers != null && almServers.length > 0) {
            for (AlmServerSettingsModel almServerModel: almServers) {
                if (almServerName.equalsIgnoreCase(almServerModel.getAlmServerName())) {
                    return almServerModel.getAlmServerUrl();
                }
            }
        }
        return "";
    }

    private UsernamePasswordCredentials getCredentialsById(String credentialsId, Run<?, ?> run,
                                                           CommonUploadLogger logger) {
        UsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(credentialsId,
                StandardUsernamePasswordCredentials.class,
                run,
                URIRequirementBuilder.create().build());

        if (credentials == null) {
            logger.warn("Can not find credentials with the credentialsId:" + credentialsId);
        }
        return credentials;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    // To expose this builder in the Snippet Generator.
    @Symbol("commonResultUploadBuilder")
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return CommonResultUploadBuilderName();
        }

        public boolean hasAlmServers() {
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).hasAlmServers();
        }

        public AlmServerSettingsModel[] getAlmServers() {
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
        }

        public FormValidation doCheckAlmUserName(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("User name must be set");
            }

            return FormValidation.ok();
        }
        public FormValidation doCheckAlmDomain(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Domain must be set");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckAlmProject(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Project must be set");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckAlmTestFolder(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("TestFolder are missing");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckAlmTestSetFolder(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("TestSetFolder are missing");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckTestingResultFile(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Testing result file must be set");
            }

            return FormValidation.ok();
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

        public FormValidation doCheckCredentialsId(@AncestorInPath Item project, @QueryParameter String value) {
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

        public String defaultFieldMapping() {
            return "testset:\n" +
                    "  root: \"x:/result/suites/suite\"\n" +
                    "  name: \"x:name\"\n" +
                    "  udf|duration: \"x:duration\"\n" +
                    "  udf|list: \"x:list\"\n" +
                    "  subtype-id: \"v:hp.qc.test-set.default\"\n" +
                    "  #subtype-id: \"v:hp.qc.test-set.default\"\n" +
                    "  #subtype-id: \"v:hp.qc.test-set.external\"\n" +
                    "  #subtype-id: \"v:hp.sse.test-set.process\"\n" +
                    "test:\n" +
                    "  root: \"x:cases/case\"\n" +
                    "  name: \"x:testName|v:_|x:testId\" # Use \"|\" to create a combined value, e.g.: <testName element value>_<testId element value>\n" +
                    "  subtype-id: \"v:MANUAL\"\n" +
                    "  #subtype-id: \"v:SERVICE-TEST\"\n" +
                    "  #subtype-id: \"v:SYSTEM-TEST\"\n" +
                    "  #subtype-id: \"v:VAPI-XP-TEST\"\n" +
                    "  #subtype-id: \"v:ALT-SCENARIO\"\n" +
                    "  #subtype-id: \"v:BUSINESS-PROCESS\"\n" +
                    "  #subtype-id: \"v:FLOW\"\n" +
                    "  #subtype-id: \"v:LEANFT-TEST\"\n" +
                    "  #subtype-id: \"v:LR-SCENARIO\"\n" +
                    "  #subtype-id: \"v:QAINSPECT-TEST\"\n" +
                    "run:\n" +
                    "  root: \"x:.\" # \".\" means the same XML object as defined above in 'test' entity, all elements defined below are searched from the same root element as 'test' root\n" +
                    "  #root: \"x:/result/suites/suite/cases/case\"\n" +
                    "  duration: \"x:duration\" # This system field is integer, if you want to save a float number, please create a string format UDF\n" +
                    "  status: \"x:status\"\n" +
                    "  udf|Run On Version: \"x:RunOnVersion\" # Create an UDF and set its label to \"run on version\", to trigger the versioning logic\n" +
                    "  udf|Date: \"2020-03-30\" # Date type field value should be in format \"yyyy-mm-dd\"\n";
        }

        public String defaultRunStatusMapping() {
            return "status:\n" +
                    "  True: \"Passed\" # If status attribute is \"True\" in report, the run in ALM will be marked as \"Passed\"\n" +
                    "  False: \"Failed\" # If status attribute is \"False\" in report, the run in ALM will be marked as \"Failed\"\n" +
                    "  1: \"Passed\" # If status attribute is \"1\" in report, the run in ALM will be marked as \"Passed\"\n" +
                    "  0: \"Failed\" # If status attribute is \"0\" in report, the run in ALM will be marked as \"Failed\"\n";
        }
    }

    private String almServerName;
    private String credentialsId;
    private String almDomain;
    private String clientType;
    private String almProject;
    private String almTestFolder;
    private String almTestSetFolder;
    private String testingResultFile;
    private String runStatusMapping;
    private String fieldMapping;

    public String getAlmServerName() {
        return almServerName;
    }

    public void setAlmServerName(String almServerName) {
        this.almServerName = almServerName;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getAlmDomain() {
        return almDomain;
    }

    public void setAlmDomain(String almDomain) {
        this.almDomain = almDomain;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getAlmProject() {
        return almProject;
    }

    public void setAlmProject(String almProject) {
        this.almProject = almProject;
    }

    public String getAlmTestFolder() {
        return almTestFolder;
    }

    public void setAlmTestFolder(String almTestFolder) {
        this.almTestFolder = almTestFolder;
    }

    public String getAlmTestSetFolder() {
        return almTestSetFolder;
    }

    public void setAlmTestSetFolder(String almTestSetFolder) {
        this.almTestSetFolder = almTestSetFolder;
    }

    public String getTestingResultFile() {
        return testingResultFile;
    }

    public void setTestingResultFile(String testingResultFile) {
        this.testingResultFile = testingResultFile;
    }

    public String getRunStatusMapping() {
        return runStatusMapping;
    }

    public void setRunStatusMapping(String runStatusMapping) {
        this.runStatusMapping = runStatusMapping;
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping;
    }
}
