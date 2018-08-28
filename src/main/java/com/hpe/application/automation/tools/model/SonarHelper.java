package com.hpe.application.automation.tools.model;

import com.hpe.application.automation.tools.octane.actions.Webhooks;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.*;
import hudson.plugins.sonar.SonarRunnerBuilder;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import jenkins.security.ApiTokenProperty;

import javax.annotation.CheckForNull;
import java.io.*;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the adapter enables usage of sonar classes and interfaces without adding a full dependency on
 * sonar plugin.
 * this class is only dependent on sonar plugin for compile time.
 */
public class SonarHelper {
    private final String sonarId = "hudson.plugins.sonar.SonarRunnerBuilder";
    private SonarRunnerBuilder builder = null;

    public SonarHelper(Run<?, ?> run) {
        DescribableList<Builder, Descriptor<Builder>> postbuilders = null;
        if (run instanceof AbstractBuild) {
            AbstractBuild abstractBuild = (AbstractBuild) run;
            if (abstractBuild instanceof MavenModuleSetBuild) {
                postbuilders = ((MavenModuleSetBuild) run).getProject().getPostbuilders();
            } else {
                AbstractProject project = abstractBuild.getProject();
                if (project instanceof Project) {
                    postbuilders = ((Project) project).getBuildersList();
                }
            }
            if (postbuilders != null) {
                setSonarBuilder(postbuilders);
            }
        }
    }

    public SonarHelper(AbstractProject project) {
        DescribableList<Builder, Descriptor<Builder>> postbuilders = null;
        if (project instanceof MavenModuleSet) {
            postbuilders = ((MavenModuleSet) project).getPostbuilders();
        } else if (project instanceof Project) {
            postbuilders = ((Project) project).getBuildersList();
        }
        if (postbuilders != null) {
            setSonarBuilder(postbuilders);
        }
    }

    private void setSonarBuilder(DescribableList<Builder, Descriptor<Builder>> postbuilders) {
        Builder sonarBuilder = postbuilders.getDynamic(sonarId);
        if (sonarBuilder != null) {
            this.builder = (SonarRunnerBuilder) sonarBuilder;
        }
    }

    /**
     * get sonar URL address
     * @return
     */
    public String extractSonarUrl() {
        return this.builder != null ? this.builder.getSonarInstallation().getServerUrl() : "";
    }

    /**
     * get sonar server token
     * @return
     */
    public String extractSonarToken() {
        return this.builder != null ? this.builder.getSonarInstallation().getServerAuthenticationToken() : "";
    }

    /**
     * get sonar project key from properties section
     * @return
     */
    public String extractSonarProjectKey() {
        return this.builder != null ? this.extractProjectKeyFromProperties(this.builder.getProperties()) : "";
    }

    /**
     * extract project key from properties using regular expression
     * @param properties
     * @return
     */
    private String extractProjectKeyFromProperties(String properties) {
        String sonarProjectKey = "";
        // this regex ignore look for project key definition, and extract the value (excluding un-relevant spaces)
        Pattern pattern = Pattern.compile("((sonar.projectKey)+[\\s]*)=([\\s]*)(\"[^\"]*\"|[^\\s]*)", Pattern.CASE_INSENSITIVE);
        Scanner scanner = new Scanner(properties);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                sonarProjectKey = matcher.group(4);
                break;
            }

        }
        scanner.close();
        return sonarProjectKey;
    }


    public static String get() {
        // extract token from user
        String user = ConfigurationService.getModel().getImpersonatedUser();
        if (user != null && !user.equalsIgnoreCase("")) {
            User jenkinsUser = User.get(user, false, Collections.emptyMap());
            if (jenkinsUser != null) {
                return jenkinsUser.getProperty(ApiTokenProperty.class).getApiToken();
            }
        }
        return "";
    }
}
