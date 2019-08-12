/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.model;

import com.microfocus.application.automation.tools.sse.common.StringUtils;
import hudson.EnvVars;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.sonar.SonarGlobalConfiguration;
import hudson.plugins.sonar.SonarInstallation;
import hudson.plugins.sonar.SonarRunnerBuilder;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * the adapter enables usage of sonar classes and interfaces without adding a full dependency on
 * sonar plugin.
 * this class is only dependent on sonar plugin for compile time.
 */

public class SonarHelper {

    public enum DataType {VULNERABILITIES, COVERAGE}

    public static final String SONAR_GLOBAL_CONFIG = "hudson.plugins.sonar.SonarGlobalConfiguration";
    private static final String SONAR_ACTION_ID = "hudson.plugins.sonar.SonarRunnerBuilder";
    private static final String SONAR_SERVER_HOST_VARIABLE = "SONAR_HOST_URL";
    private static final String SONAR_SERVER_TOKEN_VARIABLE = "SONAR_AUTH_TOKEN";

    private String serverUrl;
    private String serverToken;

    public SonarHelper(Run<?, ?> run, TaskListener listener) {
        DescribableList<Builder, Descriptor<Builder>> postbuilders = null;
        if (run instanceof AbstractBuild) {
            AbstractBuild abstractBuild = (AbstractBuild) run;
            setSonarDetailsFromMavenEnvironment(abstractBuild, listener);
            AbstractProject project = abstractBuild.getProject();

            // for jobs that does not use new sonar approach for maven jobs,
            // extract data for old approach which used post build step
            if (StringUtils.isNullOrEmpty(this.getServerUrl()) || StringUtils.isNullOrEmpty(this.getServerToken())) {
                if (project instanceof MavenModuleSet) {
                    postbuilders = ((MavenModuleSet) project).getPostbuilders();
                } else if (project instanceof Project) {
                    postbuilders = ((Project) project).getBuildersList();
                }
                if (postbuilders != null){
                    setDataFromSonarBuilder(postbuilders, run);
                }
            }
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getServerToken() {
        return serverToken;
    }

    // used by the web hook
    public static String getSonarInstallationTokenByUrl(GlobalConfiguration sonarConfiguration, String sonarUrl, Run run) {
        if (sonarConfiguration instanceof SonarGlobalConfiguration) {
            SonarGlobalConfiguration sonar = (SonarGlobalConfiguration) sonarConfiguration;
            Optional<SonarInstallation> installation = Arrays.stream(sonar.getInstallations())
                    .filter(sonarInstallation -> sonarInstallation.getServerUrl().equals(sonarUrl))
                    .findFirst();
            if (installation.isPresent()) {
                return extractAuthenticationToken(installation.get(), run);
            }
        }
        return "";
    }

    private static String extractAuthenticationToken(SonarInstallation sonarInstallation, Run run) {

        try {
            String result="";
            String methodName = "getServerAuthenticationToken";
            Method method = Arrays.stream(SonarInstallation.class.getDeclaredMethods())
                    .filter(m->m.getName().equals(methodName))
                    .findFirst().orElse(null);

            if (method == null) {
                throw new NoSuchMethodException();
            } else if (method.getParameterCount() == 1) {//2.9+
                result = (String) method.invoke(sonarInstallation, run);
            } else if (method.getReturnType().equals(String.class)) {
                result = (String) method.invoke(sonarInstallation);//2.6.1 version
            } else if (method.getReturnType().equals(Secret.class)) {
                Secret secret = (Secret) method.invoke(sonarInstallation);//2.8.1 version
                result = Secret.toString(secret);
            }

            return result;


        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Not Supported version of Sonar Plugin");
        }

    }

    private void setSonarDetailsFromMavenEnvironment(AbstractBuild build, TaskListener listener) {
        EnvVars environment;
        try {
            environment = build.getEnvironment(listener);
            if (environment != null) {
                this.serverUrl = environment.get(SONAR_SERVER_HOST_VARIABLE, "");
                this.serverToken = environment.get(SONAR_SERVER_TOKEN_VARIABLE, "");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setDataFromSonarBuilder(DescribableList<Builder, Descriptor<Builder>> postBuilders, Run run) {
        Builder sonarBuilder = postBuilders.getDynamic(SONAR_ACTION_ID);
        if (sonarBuilder != null) {
            SonarRunnerBuilder builder = (SonarRunnerBuilder) sonarBuilder;
            this.serverUrl = extractSonarUrl(builder);
            this.serverToken = extractSonarToken(builder, run);
        }
    }

    /**
     * get sonar URL address
     *
     * @return Sonar's URL
     */
    private String extractSonarUrl(SonarRunnerBuilder builder) {
        return builder != null ? builder.getSonarInstallation().getServerUrl() : "";
    }

    /**
     * get sonar server token
     *
     * @return Sonar's auth token
     */
    private String extractSonarToken(SonarRunnerBuilder builder, Run run) {
        String result  = builder != null  ? extractAuthenticationToken(builder.getSonarInstallation(), run) : "";
        return result;
    }
}
