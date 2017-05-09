/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.application.automation.tools.octane.executor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hp.octane.integrations.dto.executor.CredentialsInfo;
import com.hp.octane.integrations.dto.executor.TestConnectivityInfo;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.EnvVars;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.IOException;
import java.util.List;

/**
 * Utility for handling of scm repositories
 */
public class ExecutorConnTestService {

    private static final Logger logger = LogManager.getLogger(ExecutorConnTestService.class);

    /**
     * Validate that scm repository is valid
     * @param testConnectivityInfo
     * @return
     */
    public static boolean checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
        if (testConnectivityInfo.getScmRepository() != null &&
                StringUtils.isNotEmpty(testConnectivityInfo.getScmRepository().getUrl()) &&
                SCMType.GIT.equals(testConnectivityInfo.getScmRepository().getType())) {

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
        return false;
    }

    /**
     * Insert of update(if already exist) of credentials in Jenkins.
     * If credentialsInfo containscredentialsId - we update existing credentials with new user/password, otherwise - create new credentials
     * @param credentialsInfo
     * @return created/updated credentials with filled credentials id
     */
    public static CredentialsInfo upsertRepositoryCredentials(final CredentialsInfo credentialsInfo) {

        if (StringUtils.isEmpty(credentialsInfo.getCredentialId())) {
            if (StringUtils.isNotEmpty(credentialsInfo.getUsername()) && credentialsInfo.getPassword() != null) {
                BaseStandardCredentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null, null, credentialsInfo.getUsername(), credentialsInfo.getPassword());
                CredentialsStore store = new SystemCredentialsProvider.StoreImpl();
                try {
                    store.addCredentials(Domain.global(), c);
                    credentialsInfo.setCredentialId(c.getId());
                } catch (IOException e) {
                    logger.error("Failed to add credentials " + e.getMessage());
                }
            }
        } else {
            List<BaseStandardCredentials> list = CredentialsProvider.lookupCredentials(BaseStandardCredentials.class, (Item) null, null);
            for (BaseStandardCredentials cred : list) {
                if (cred.getId().equals(credentialsInfo.getCredentialId())) {
                    BaseStandardCredentials newCred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsInfo.getCredentialId(),
                            null, credentialsInfo.getUsername(), credentialsInfo.getPassword());
                    CredentialsStore store = new SystemCredentialsProvider.StoreImpl();
                    try {
                        store.updateCredentials(Domain.global(), cred, newCred);
                    } catch (IOException e) {
                        logger.error("Failed to add credentials " + e.getMessage());
                    }
                }
            }
        }

        return credentialsInfo;
    }
}
