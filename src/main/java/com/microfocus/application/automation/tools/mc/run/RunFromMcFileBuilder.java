/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.mc.run;

import com.microfocus.application.automation.tools.EncryptionUtils;
import com.microfocus.application.automation.tools.mc.JobConfigurationProxy;
import com.microfocus.application.automation.tools.model.MCServerSettingsModel;
import com.microfocus.application.automation.tools.model.RunFromFileSystemModel;
import com.microfocus.application.automation.tools.run.RunFromFileBuilder;
import com.microfocus.application.automation.tools.settings.MCServerSettingsBuilder;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * Describs a regular jenkins build step from UFT or LR
 */
public class RunFromMcFileBuilder extends Builder {
    private RunFromFileSystemModel runFromFileModel;

    /**
     * Instantiates a new Run from file builder.
     *
     * @param runFromFileModel the run from file model
     */
    public RunFromMcFileBuilder(RunFromFileSystemModel runFromFileModel) {
        this.runFromFileModel = runFromFileModel;
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
    public static String getJobId(String mcUrl, String mcUserName, String mcPassword, String mcTenantId,
                                  String proxyAddress, String proxyUserName, String proxyPassword, String previousJobId) {
        JobConfigurationProxy instance = JobConfigurationProxy.getInstance();
        if (null != previousJobId && !previousJobId.isEmpty()) {
            JSONObject jobJSON = instance.getJobById(mcUrl, mcUserName, mcPassword, mcTenantId, proxyAddress,
                    proxyUserName, proxyPassword, previousJobId);

            if (jobJSON != null && previousJobId.equals(jobJSON.getAsString("id"))) {
                return previousJobId;
            } else {
                return instance.createTempJob(mcUrl, mcUserName, mcPassword, mcTenantId, proxyAddress, proxyUserName,
                        proxyPassword);
            }
        }
        return instance.createTempJob(mcUrl, mcUserName, mcPassword, mcTenantId, proxyAddress, proxyUserName,
                proxyPassword);
    }

    /**
     * Gets mc server url.
     *
     * @param serverName the server name
     * @return the mc server url
     */
    @SuppressWarnings("squid:S2259")
    @JavaScriptMethod
    public static String getMcServerUrl(String serverName) {
        String serverUrl = "";
        MCServerSettingsModel[] servers = Jenkins.getInstance().getDescriptorByType(
                MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
        for (MCServerSettingsModel mcServer : servers) {
            if (mcServer.getMcServerName().equals(serverName)) {
                serverUrl = mcServer.getMcServerUrl();
            }
        }
        return serverUrl;
    }

    /**
     * Has mc servers boolean.
     *
     * @return the boolean
     */
    @SuppressWarnings("squid:S2259")
    public static boolean hasMCServers() {
        return Jenkins.getInstance().getDescriptorByType(
                MCServerSettingsBuilder.MCDescriptorImpl.class).hasMCServers();
    }

    /**
     * Get mc servers mc server settings model [ ].
     *
     * @return the mc server settings model [ ]
     */
    @SuppressWarnings("squid:S2259")
    public static MCServerSettingsModel[] getMcServers() {
        return Jenkins.getInstance().getDescriptorByType(
                MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
    }

    public void setRunFromFileModel(RunFromFileSystemModel runFromFileModel) {
        this.runFromFileModel = runFromFileModel;
    }

    /**
     * Gets mc server settings model.
     *
     * @return the mc server settings model
     */
    public MCServerSettingsModel getMCServerSettingsModel() {
        for (MCServerSettingsModel mcServer : getDescriptor().getMcServers()) {
            if (this.runFromFileModel != null
                    && runFromFileModel.getMcServerName() != null
                    && mcServer.getMcServerName() != null
                    && runFromFileModel.getMcServerName().equals(mcServer.getMcServerName())) {
                return mcServer;
            }
        }
        return null;
    }

    public RunFromFileBuilder.DescriptorImpl getDescriptor() {
        return (RunFromFileBuilder.DescriptorImpl) super.getDescriptor();
    }

    public void addMcProperties(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener,
                                MCServerSettingsModel mcServerSettingsModel, Properties mergedProperties) {
        String mcServerUrl;
        JSONObject jobDetails;
        if (mcServerSettingsModel != null) {
            mcServerUrl = mcServerSettingsModel.getProperties().getProperty("MobileHostAddress");
            if (runFromFileModel.getProxySettings() == null) {
                jobDetails = runFromFileModel.getJobDetails(mcServerUrl, null, null, null);
            } else {
                jobDetails = runFromFileModel.getJobDetails(mcServerUrl,
                        runFromFileModel.getProxySettings().getFsProxyAddress(),
                        runFromFileModel.getProxySettings().getFsProxyUserName(),
                        runFromFileModel.getProxySettings().getFsProxyPassword());
            }
            mergedProperties.setProperty("mobileinfo", jobDetails != null ? jobDetails.toJSONString() : "");
            mergedProperties.setProperty("MobileHostAddress", mcServerUrl);
        }

        if (runFromFileModel != null && StringUtils.isNotBlank(runFromFileModel.getFsPassword())) {
            try {
                String encPassword = EncryptionUtils.Encrypt(runFromFileModel.getFsPassword(),
                        EncryptionUtils.getSecretKey());
                mergedProperties.put("MobilePassword", encPassword);
            } catch (Exception e) {
                build.setResult(Result.FAILURE);
                listener.fatalError("problem in mobile center password encryption" + e);
            }
        }
    }
}
