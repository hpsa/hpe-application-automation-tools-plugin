package com.hp.application.automation.tools.octane.executor;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hp.octane.integrations.dto.executor.TestConnectivityInfo;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.IOException;

/**
 * Created by shitritn on 4/23/2017.
 */
public class ExecutorConnTestService {

    private static final Logger logger = LogManager.getLogger(ExecutorConnTestService.class);

    public static boolean checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
        if (testConnectivityInfo.getScmRepository() != null && StringUtils.isNotEmpty(testConnectivityInfo.getScmRepository().getUrl())) {
            if (SCMType.GIT.equals(testConnectivityInfo.getScmRepository().getType())) {
                BaseStandardCredentials c = null;
                if (StringUtils.isNotEmpty(testConnectivityInfo.getUsername()) && testConnectivityInfo.getPassword() != null) {
                    c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null, null, testConnectivityInfo.getUsername(), testConnectivityInfo.getPassword());
                }

                try {
                    EnvVars environment = new EnvVars(System.getenv());
                    GitClient git = Git.with(TaskListener.NULL, environment).using(GitTool.getDefaultInstallation().getGitExe()).getClient();
                    git.addDefaultCredentials(c);
                    git.getHeadRev(testConnectivityInfo.getScmRepository().getUrl(), "HEAD");

                    return true;
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to connect to git : " + e.getMessage());
                } catch (GitException e) {
                    logger.error("Failed to execute getHeadRev : " + e.getMessage());
                }
            }
        }
        return false;
    }
}
