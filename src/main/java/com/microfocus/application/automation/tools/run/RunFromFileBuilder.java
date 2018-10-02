/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.run;

import com.microfocus.application.automation.tools.AlmToolsUtils;
import com.microfocus.application.automation.tools.lr.run.RunFromLrFileBuilder;
import com.microfocus.application.automation.tools.mc.JobConfigurationProxy;
import com.microfocus.application.automation.tools.mc.run.RunFromMcFileBuilder;
import com.microfocus.application.automation.tools.model.FileSystemTestSetModel;
import com.microfocus.application.automation.tools.model.MCServerSettingsModel;
import com.microfocus.application.automation.tools.model.ProxySettings;
import com.microfocus.application.automation.tools.model.RunFromFileSystemModel;
import com.microfocus.application.automation.tools.uft.run.RunFromUftFileBuilder;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * Describes a regular jenkins build step from UFT or LR
 */
public class RunFromFileBuilder extends Builder implements SimpleBuildStep {

    private static final String HP_TOOLS_LAUNCHER_EXE = "HpToolsLauncher.exe";
    private static final String LRANALYSIS_LAUNCHER_EXE = "LRAnalysisLauncher.exe";
    private final RunFromMcFileBuilder runMcFromFileBuilder;
    private String ResultFilename = "ApiResults.xml";
    private String ParamFileName = "ApiRun.txt";
    private RunFromFileSystemModel runFromFileModel;
    private FileSystemTestSetModel fileSystemTestSetModel;
    private RunFromUftFileBuilder runUftFromFileBuilder = new RunFromUftFileBuilder();
    private boolean isParallelRunnerEnabled;

    /**
     * Instantiates a new Run from file builder.
     *
     * @param fsTests the fs tests
     */
    @DataBoundConstructor
    public RunFromFileBuilder(String fsTests, boolean isParallelRunnerEnabled,
                              FileSystemTestSetModel fileSystemTestSetModel) {
        this.runFromFileModel = new RunFromFileSystemModel(fsTests);
        this.fileSystemTestSetModel = fileSystemTestSetModel;
        this.isParallelRunnerEnabled = isParallelRunnerEnabled;
        this.runUftFromFileBuilder = new RunFromUftFileBuilder(fileSystemTestSetModel);
        this.runMcFromFileBuilder = new RunFromMcFileBuilder(runFromFileModel);
    }

    /**
     * Instantiates a new Run from file builder.
     *
     * @param fsTests the fs tests
     */
    public RunFromFileBuilder(String fsTests) {
        runFromFileModel = new RunFromFileSystemModel(fsTests);
        this.runMcFromFileBuilder = new RunFromMcFileBuilder(runFromFileModel);
    }

    /**
     * Instantiates a new Run from file builder.
     *
     * @param runFromFileModel the run from file model
     */
    public RunFromFileBuilder(RunFromFileSystemModel runFromFileModel) {
        this.runFromFileModel = runFromFileModel;
        this.runMcFromFileBuilder = new RunFromMcFileBuilder(runFromFileModel);
    }

    /**
     * @param fsTests                   the fs tests
     * @param fsTimeout                 the fs timeout
     * @param controllerPollingInterval the controller polling interval
     * @param perScenarioTimeOut        the per scenario time out
     * @param ignoreErrorStrings        the ignore error strings
     * @param analysisTemplate          the analysis template
     * @param displayController         the display controller
     * @param mcServerName              the mc server name
     * @param fsUserName                the fs user name
     * @param fsPassword                the fs password
     * @param fsDeviceId                the fs device id
     * @param fsTargetLab               the fs target lab
     * @param fsManufacturerAndModel    the fs manufacturer and model
     * @param fsOs                      the fs os
     * @param fsAutActions              the fs aut actions
     * @param fsLaunchAppName           the fs launch app name
     * @param fsDevicesMetrics          the fs devices metrics
     * @param fsInstrumented            the fs instrumented
     * @param fsExtraApps               the fs extra apps
     * @param fsJobId                   the fs job id
     * @param proxySettings             the proxy settings
     * @param useSSL                    the use ssl
     * @deprecated Instantiates a new Run from file builder.
     */
    @SuppressWarnings("squid:S00107")
    @Deprecated
    public RunFromFileBuilder(String fsTests, String fsTimeout, String fsUftRunMode, String controllerPollingInterval,
                              String perScenarioTimeOut, String ignoreErrorStrings, String displayController,
                              String analysisTemplate, String mcServerName, String fsUserName, String fsPassword,
                              String mcTenantId, String fsDeviceId, String fsTargetLab, String fsManufacturerAndModel,
                              String fsOs, String fsAutActions, String fsLaunchAppName, String fsDevicesMetrics,
                              String fsInstrumented, String fsExtraApps, String fsJobId, ProxySettings proxySettings,
                              boolean useSSL, boolean isParallelRunnerEnabled) {
        this.isParallelRunnerEnabled = isParallelRunnerEnabled;
        runFromFileModel = new RunFromFileSystemModel(fsTests, fsTimeout, fsUftRunMode, controllerPollingInterval,
                perScenarioTimeOut, ignoreErrorStrings, displayController, analysisTemplate, mcServerName, fsUserName,
                fsPassword, mcTenantId, fsDeviceId, fsTargetLab, fsManufacturerAndModel, fsOs, fsAutActions,
                fsLaunchAppName, fsDevicesMetrics, fsInstrumented, fsExtraApps, fsJobId, proxySettings, useSSL);
        this.runMcFromFileBuilder = new RunFromMcFileBuilder(runFromFileModel);
    }

    public FileSystemTestSetModel getFileSystemTestSetModel() {
        return fileSystemTestSetModel;
    }

    /**
     * Gets the parallel runner flag.
     *
     * @return the current parallel runner flag state(enabled/disabled)
     */
    public boolean getIsParallelRunnerEnabled() {
        return isParallelRunnerEnabled;
    }

    /**
     * Sets the parallel runner flag
     *
     * @param isParallelRunnerEnabled the parallel runner flag
     */
    @DataBoundSetter
    private void setIsParallelRunnerEnabled(boolean isParallelRunnerEnabled) {
        this.isParallelRunnerEnabled = isParallelRunnerEnabled;
    }

    public String getAnalysisTemplate() {
        return runFromFileModel.getAnalysisTemplate();
    }

    /**
     * Sets analysis template.
     *
     * @param analysisTemplate the analysis template
     */
    @DataBoundSetter
    public void setAnalysisTemplate(String analysisTemplate) {
        runFromFileModel.setAnalysisTemplate(analysisTemplate);
    }

    public String getFsTimeout() {
        return runFromFileModel.getFsTimeout();
    }

    /**
     * Sets fs timeout.
     *
     * @param fsTimeout the fs timeout
     */
    @DataBoundSetter
    public void setFsTimeout(String fsTimeout) {
        runFromFileModel.setFsTimeout(fsTimeout);
    }

    public String getFsTests() {
        return runFromFileModel.getFsTests();
    }

    public void setFsTests(String fsTests) {
        runFromFileModel.setFsTests(fsTests);
    }

    public String getControllerPollingInterval() {
        return runFromFileModel.getControllerPollingInterval();
    }

    /**
     * Sets controller polling interval.
     *
     * @param controllerPollingInterval the controller polling interval
     */
    @DataBoundSetter
    public void setControllerPollingInterval(String controllerPollingInterval) {
        runFromFileModel.setControllerPollingInterval(controllerPollingInterval);
    }

    public String getPerScenarioTimeOut() {
        return runFromFileModel.getPerScenarioTimeOut();
    }

    /**
     * Sets per scenario time out.
     *
     * @param perScenarioTimeOut the per scenario time out
     */
    @DataBoundSetter
    public void setPerScenarioTimeOut(String perScenarioTimeOut) {
        runFromFileModel.setPerScenarioTimeOut(perScenarioTimeOut);
    }

    public String getDisplayController() {
        return runFromFileModel.getDisplayController();
    }

    /**
     * Sets display controller.
     *
     * @param displayController the display controller
     */
    @DataBoundSetter
    public void setDisplayController(String displayController) {
        runFromFileModel.setDisplayController(displayController);
    }

    public String getFsAutActions() {
        return runFromFileModel.getFsAutActions();
    }

    /**
     * Sets fs aut actions.
     *
     * @param fsAutActions the fs aut actions
     */
    @DataBoundSetter
    public void setFsAutActions(String fsAutActions) {
        runFromFileModel.setFsAutActions(fsAutActions);
    }

    public String getFsDeviceId() {
        return runFromFileModel.getFsDeviceId();
    }

    /**
     * Sets fs device id.
     *
     * @param fsDeviceId the fs device id
     */
    @DataBoundSetter
    public void setFsDeviceId(String fsDeviceId) {
        runFromFileModel.setFsDeviceId(fsDeviceId);
    }

    public String getFsDevicesMetrics() {
        return runFromFileModel.getFsDevicesMetrics();
    }

    /**
     * Sets fs devices metrics.
     *
     * @param fsDevicesMetrics the fs devices metrics
     */
    @DataBoundSetter
    public void setFsDevicesMetrics(String fsDevicesMetrics) {
        runFromFileModel.setFsDevicesMetrics(fsDevicesMetrics);
    }

    public String getFsExtraApps() {
        return runFromFileModel.getFsExtraApps();
    }

    /**
     * Sets fs extra apps.
     *
     * @param fsExtraApps the fs extra apps
     */
    @DataBoundSetter
    public void setFsExtraApps(String fsExtraApps) {
        runFromFileModel.setFsExtraApps(fsExtraApps);
    }

    public String getFsOs() {
        return runFromFileModel.getFsOs();
    }

    /**
     * Sets fs os.
     *
     * @param fsOs the fs os
     */
    @DataBoundSetter
    public void setFsOs(String fsOs) {
        runFromFileModel.setFsOs(fsOs);
    }

    public String getFsInstrumented() {
        return runFromFileModel.getFsInstrumented();
    }

    /**
     * Sets fs instrumented.
     *
     * @param fsInstrumented the fs instrumented
     */
    @DataBoundSetter
    public void setFsInstrumented(String fsInstrumented) {
        runFromFileModel.setFsInstrumented(fsInstrumented);
    }

    public String getFsJobId() {
        return runFromFileModel.getFsJobId();
    }

    /**
     * Sets fs job id.
     *
     * @param fsJobId the fs job id
     */
    @DataBoundSetter
    public void setFsJobId(String fsJobId) {
        runFromFileModel.setFsJobId(fsJobId);
    }

    public String getFsUftRunMode() {
        return runFromFileModel.getFsUftRunMode();
    }

    /**
     * Sets fs runMode.
     *
     * @param fsUftRunMode the fs runMode
     */
    @DataBoundSetter
    public void setFsUftRunMode(String fsUftRunMode) {
        runFromFileModel.setFsUftRunMode(fsUftRunMode);
    }

    public String getIgnoreErrorStrings() {
        return runFromFileModel.getIgnoreErrorStrings();
    }

    /**
     * Sets ignore error strings.
     *
     * @param ignoreErrorStrings the ignore error strings
     */
    @DataBoundSetter
    public void setIgnoreErrorStrings(String ignoreErrorStrings) {
        runFromFileModel.setIgnoreErrorStrings(ignoreErrorStrings);
    }

    public String getMcServerName() {
        return runFromFileModel.getMcServerName();
    }

    /**
     * Sets mc server name.
     *
     * @param mcServerName the mc server name
     */
    @DataBoundSetter
    public void setMcServerName(String mcServerName) {
        runFromFileModel.setMcServerName(mcServerName);
    }

    public String getFsManufacturerAndModel() {
        return runFromFileModel.getFsManufacturerAndModel();
    }

    /**
     * Sets fs manufacturer and model.
     *
     * @param fsManufacturerAndModel the fs manufacturer and model
     */
    @DataBoundSetter
    public void setFsManufacturerAndModel(String fsManufacturerAndModel) {
        runFromFileModel.setFsManufacturerAndModel(fsManufacturerAndModel);
    }

    public String getFsLaunchAppName() {
        return runFromFileModel.getFsLaunchAppName();
    }

    /**
     * Sets fs launch app name.
     *
     * @param fsLaunchAppName the fs launch app name
     */
    @DataBoundSetter
    public void setFsLaunchAppName(String fsLaunchAppName) {
        runFromFileModel.setFsLaunchAppName(fsLaunchAppName);
    }

    public ProxySettings getProxySettings() {
        return runFromFileModel.getProxySettings();
    }

    /**
     * Sets proxy settings.
     *
     * @param proxySettings the proxy settings
     */
    @DataBoundSetter
    public void setProxySettings(ProxySettings proxySettings) {
        runFromFileModel.setProxySettings(proxySettings);
    }

    public String getMcTenantId() {
        return runFromFileModel.getMcTenantId();
    }

    @DataBoundSetter
    public void setMcTenantId(String mcTenantId) {
        runFromFileModel.setMcTenantId(mcTenantId);
    }

    public String getFsPassword() {
        return runFromFileModel.getFsPassword();
    }

    /**
     * Sets fs password.
     *
     * @param fsPassword the fs password
     */
    @DataBoundSetter
    public void setFsPassword(String fsPassword) {
        runFromFileModel.setFsPassword(fsPassword);
    }

    public String getFsUserName() {
        return runFromFileModel.getFsUserName();
    }

    /**
     * Sets fs user name.
     *
     * @param fsUserName the fs user name
     */
    @DataBoundSetter
    public void setFsUserName(String fsUserName) {
        runFromFileModel.setFsUserName(fsUserName);
    }

    public String getFsTargetLab() {
        return runFromFileModel.getFsTargetLab();
    }

    /**
     * Sets fs target lab.
     *
     * @param fsTargetLab the fs target lab
     */
    @DataBoundSetter
    public void setFsTargetLab(String fsTargetLab) {
        runFromFileModel.setFsTargetLab(fsTargetLab);
    }

    public boolean getUseSSL() {
        return runFromFileModel.isUseSSL();
    }

    /**
     * Sets mc server name.
     *
     * @param useSSL the mc server name
     */
    @DataBoundSetter
    public void setuseSSL(boolean useSSL) {
        runFromFileModel.setUseSSL(useSSL);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener)
            throws IOException {

        // get the mc server settings
        MCServerSettingsModel mcServerSettingsModel = getMCServerSettingsModel();
        EnvVars env = getEnvironmentVariables(build, listener);

        // this is an unproper replacment to the build.getVariableResolver since workflow run won't support the
        // getBuildEnviroment() as written here:
        // https://github.com/jenkinsci/pipeline-plugin/blob/893e3484a25289c59567c6724f7ce19e3d23c6ee/DEVGUIDE.md#variable-substitutions
        // now merge them into one list
        Properties mergedProperties = new Properties();
        runMcFromFileBuilder.addMcProperties(build, listener, mcServerSettingsModel, mergedProperties, this);
        assertEnvironmentVariables(listener, env);
        addModelProperties(build, env, mergedProperties);
        addEnvironmentVariablesToProperties(env, mergedProperties);

        String time = getCurrentDate();
        // get a unique filename for the params file
        ParamFileName = "props" + time + ".txt";
        ResultFilename = "Results" + time + ".xml";

        mergedProperties.put("runType", AlmRunTypes.RunType.FileSystem.toString());
        mergedProperties.put("resultsFilename", ResultFilename);
        runUftFromFileBuilder.replaceTestWithMtbx(build, workspace, listener,
                env, mergedProperties, time, isParallelRunnerEnabled);

        // get properties serialized into a stream
        String propsSerialization = getPropsSerializationString(build, listener, mergedProperties);

        FilePath CmdLineExe;
        try (InputStream propsStream = IOUtils.toInputStream(propsSerialization)) {
            // Get the URL to the Script used to run the test, which is bundled
            // in the plugin
            @SuppressWarnings("squid:S2259")
            URL cmdExeUrl = Jenkins.getInstance().pluginManager.uberClassLoader.getResource(HP_TOOLS_LAUNCHER_EXE);
            if (cmdExeUrl == null) {
                listener.fatalError(HP_TOOLS_LAUNCHER_EXE + " not found in resources");
                return;
            }

            @SuppressWarnings("squid:S2259")
            URL cmdExe2Url = Jenkins.getInstance().pluginManager.uberClassLoader.getResource(LRANALYSIS_LAUNCHER_EXE);
            if (cmdExe2Url == null) {
                listener.fatalError(LRANALYSIS_LAUNCHER_EXE + "not found in resources");
                return;
            }

            FilePath propsFileName = workspace.child(ParamFileName);
            CmdLineExe = workspace.child(HP_TOOLS_LAUNCHER_EXE);
            FilePath CmdLineExe2 = workspace.child(LRANALYSIS_LAUNCHER_EXE);

            try {
                // create a file for the properties file, and save the properties
                propsFileName.copyFrom(propsStream);
                // Copy the script to the project workspace
                CmdLineExe.copyFrom(cmdExeUrl);
                CmdLineExe2.copyFrom(cmdExe2Url);
            } catch (IOException | InterruptedException e) {
                build.setResult(Result.FAILURE);
                listener.error("Copying executable files to executing node " + e);
            }
        }

        try {
            // Run the HpToolsLauncher.exe
            AlmToolsUtils.runOnBuildEnv(build, launcher, listener, CmdLineExe, ParamFileName);
            // Has the report been successfully generated?
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            build.setResult(Result.FAILURE);
            listener.error("Failed running HpToolsLauncher " + ioe);
            return;
        } catch (InterruptedException e) {
            build.setResult(Result.ABORTED);
            PrintStream out = listener.getLogger();
            listener.error("Failed running HpToolsLauncher - build aborted " + e);

            try {
                AlmToolsUtils.runHpToolsAborterOnBuildEnv(build, launcher, listener, ParamFileName, workspace);
            } catch (IOException e1) {
                Util.displayIOException(e1, listener);
                build.setResult(Result.FAILURE);
                return;
            } catch (InterruptedException e1) {
                listener.error("Failed running HpToolsAborter " + e1);
            }
            out.println("Operation Was aborted by user.");
        }
    }

    private String getPropsSerializationString(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener,
                                               Properties mergedProperties) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            mergedProperties.store(stream, "");
        } catch (IOException e) {
            listener.error("Storing run variable failed: " + e);
            build.setResult(Result.FAILURE);
        }
        return stream.toString();
    }

    private void addEnvironmentVariablesToProperties(EnvVars env, Properties mergedProperties) {
        int idx = 0;
        for (Iterator<String> iterator = env.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            idx++;
            mergedProperties.put("JenkinsEnv" + idx, key + ";" + env.get(key));
        }
    }

    private String getCurrentDate() {
        Date now = new Date();
        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        return formatter.format(now);
    }

    private void addModelProperties(@Nonnull Run<?, ?> build, EnvVars env, Properties mergedProperties) {
        if (build instanceof AbstractBuild) {
            VariableResolver<String> varResolver = ((AbstractBuild) build).getBuildVariableResolver();
            mergedProperties.putAll(runFromFileModel.getProperties(env, varResolver));
        } else {
            mergedProperties.putAll(runFromFileModel.getProperties(env));
        }
    }

    private void assertEnvironmentVariables(@Nonnull TaskListener listener, EnvVars env) throws IOException {
        if (env == null) {
            listener.fatalError("Environment not set");
            throw new IOException("Env Null - something went wrong with fetching jenkins build environment");
        }
    }

    private EnvVars getEnvironmentVariables(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener) {
        EnvVars env = null;
        try {
            env = build.getEnvironment(listener);

        } catch (IOException | InterruptedException e) {
            listener.error("Failed loading build environment " + e);
        }
        return env;
    }

    /**
     * Gets mc server settings model.
     *
     * @return the mc server settings model
     */
    public MCServerSettingsModel getMCServerSettingsModel() {
        return runMcFromFileBuilder.getMCServerSettingsModel();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Gets run from file model.
     *
     * @return the run from file model
     */
    public RunFromFileSystemModel getRunFromFileModel() {
        return runFromFileModel;
    }

    /**
     * Gets run results file name.
     *
     * @return the run results file name
     */
    public String getRunResultsFileName() {
        return ResultFilename;
    }

    /**
     * The type Descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * The Instance.
         */
        JobConfigurationProxy instance = JobConfigurationProxy.getInstance();
        private RunFromLrFileBuilder runFromLrFileBuilder = new RunFromLrFileBuilder();

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            load();
        }


        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Gets job id.
         * If there is already a job created by jenkins plugin, and exists then return this job id,
         * otherwise, create a new temp job and return the new job id.
         *
         * @param mcUrl         the mc url
         * @param mcUserName    the mc user name
         * @param mcPassword    the mc password
         * @param proxyAddress  the proxy address
         * @param proxyUserName the proxy user name
         * @param proxyPassword the proxy password
         * @param previousJobId the previous job id
         * @return the job id
         */
        @JavaScriptMethod
        public String getJobId(String mcUrl, String mcUserName, String mcPassword, String mcTenantId,
                               String proxyAddress, String proxyUserName, String proxyPassword, String previousJobId) {
            return RunFromMcFileBuilder.getJobId(mcUrl, mcUserName, mcPassword, mcTenantId, proxyAddress, proxyUserName,
                    proxyPassword, previousJobId);
        }

        /**
         * Populate app and device json object.
         *
         * @param mcUrl         the mc url
         * @param mcUserName    the mc user name
         * @param mcPassword    the mc password
         * @param proxyAddress  the proxy address
         * @param proxyUserName the proxy user name
         * @param proxyPassword the proxy password
         * @param jobId         the job id
         * @return the json object
         */
        @JavaScriptMethod
        public JSONObject populateAppAndDevice(String mcUrl, String mcUserName, String mcPassword, String mcTenantId,
                                               String proxyAddress, String proxyUserName,
                                               String proxyPassword, String jobId) {
            return instance.getJobJSONData(mcUrl, mcUserName, mcPassword, mcTenantId,
                    proxyAddress, proxyUserName, proxyPassword, jobId);
        }

        /**
         * Gets mc server url.
         *
         * @param serverName the server name
         * @return the mc server url
         */
        @SuppressWarnings("squid:S2259")
        @JavaScriptMethod
        public String getMcServerUrl(String serverName) {
            return RunFromMcFileBuilder.getMcServerUrl(serverName);
        }

        @Override
        public String getDisplayName() {
            return "Execute Micro Focus tests from file system";
        }

        /**
         * Do check fs tests form validation.
         *
         * @param value the value
         * @return the form validation
         */
        @SuppressWarnings("squid:S1172")
        public FormValidation doCheckFsTests(@QueryParameter String value) {
            return FormValidation.ok();
        }

        /**
         * Do check ignore error strings form validation.
         *
         * @param value the value
         * @return the form validation
         */
        @SuppressWarnings("squid:S1172")
        public FormValidation doCheckIgnoreErrorStrings(@QueryParameter String value) {
            return FormValidation.ok();
        }

        /**
         * Do check fs timeout form validation.
         *
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckFsTimeout(@QueryParameter String value) {
            return runFromLrFileBuilder.doCheckFsTimeout(value);
        }

        /**
         * Has mc servers boolean.
         *
         * @return the boolean
         */
        @SuppressWarnings("squid:S2259")
        public boolean hasMCServers() {
            return RunFromMcFileBuilder.hasMCServers();
        }

        /**
         * Get mc servers mc server settings model [ ].
         *
         * @return the mc server settings model [ ]
         */
        @SuppressWarnings("squid:S2259")

        public MCServerSettingsModel[] getMcServers() {
            return RunFromMcFileBuilder.getMcServers();
        }

        /**
         * Do check controller polling interval form validation.
         *
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckControllerPollingInterval(@QueryParameter String value) {
            return RunFromLrFileBuilder.doCheckControllerPollingInterval(value);
        }

        /**
         * Do check per scenario time out form validation.
         *
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckPerScenarioTimeOut(@QueryParameter String value) {
            return RunFromLrFileBuilder.doCheckPerScenarioTimeOut(value);
        }

        /**
         * Check if the value is parameterized.
         *
         * @param value the value
         * @return boolean
         */
        public boolean isParameterizedValue(String value) {
            return RunFromLrFileBuilder.isParameterizedValue(value);
        }
    }
}