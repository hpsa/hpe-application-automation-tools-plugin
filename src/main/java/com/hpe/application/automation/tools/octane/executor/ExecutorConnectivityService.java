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

package com.hpe.application.automation.tools.octane.executor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.executor.CredentialsInfo;
import com.hp.octane.integrations.dto.executor.TestConnectivityInfo;
import com.hp.octane.integrations.dto.scm.SCMType;
import com.hpe.application.automation.tools.common.HttpStatus;
import hudson.EnvVars;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility for handling connectivity with scm repositories
 */
public class ExecutorConnectivityService {

    private static final Logger logger = LogManager.getLogger(ExecutorConnectivityService.class);
    private static final Map<Permission, String> requirePremissions = initRequirePremissions();
    private static final Map<Permission, String> credentialsPremissions = initCredentialsPremissions();

    /**
     * Validate that scm repository is valid
     *
     * @param testConnectivityInfo contains values to check
     * @return OctaneResponse return status code and error to show for client
     */
    public static OctaneResponse checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
        OctaneResponse result = DTOFactory.getInstance().newDTO(OctaneResponse.class);
        if (testConnectivityInfo.getScmRepository() != null &&
                StringUtils.isNotEmpty(testConnectivityInfo.getScmRepository().getUrl()) &&
                SCMType.GIT.equals(testConnectivityInfo.getScmRepository().getType())) {

            BaseStandardCredentials c = null;
            if (StringUtils.isNotEmpty(testConnectivityInfo.getUsername()) && testConnectivityInfo.getPassword() != null) {
                c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null, null, testConnectivityInfo.getUsername(), testConnectivityInfo.getPassword());
            } else if (StringUtils.isEmpty(testConnectivityInfo.getCredentialsId())) {
                c = getCredentialsById(testConnectivityInfo.getCredentialsId());
            }


            Jenkins jenkins = Jenkins.getActiveInstance();

            List<String> permissionResult = checkCIPermissions(jenkins, c != null);

            if (permissionResult != null && !permissionResult.isEmpty()) {
                String user = User.current() != null ? User.current().getId() : jenkins.ANONYMOUS.getPrincipal().toString();
                String error = String.format("Failed : User \'%s\' is missing permissions \'%s\' on CI server", user, permissionResult);
                logger.error(error);
                result.setStatus(HttpStatus.FORBIDDEN.getCode());
                result.setBody(error);
                return result;
            }

            try {
                EnvVars environment = new EnvVars(System.getenv());
                GitClient git = Git.with(TaskListener.NULL, environment).using(GitTool.getDefaultInstallation().getGitExe()).getClient();
                git.addDefaultCredentials(c);
                git.getHeadRev(testConnectivityInfo.getScmRepository().getUrl(), "HEAD");

                result = result.setStatus(HttpStatus.OK.getCode());

            } catch (IOException | InterruptedException e) {
                logger.error("Failed to connect to git : " + e.getMessage());
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                result.setBody(e.getMessage());
            } catch (GitException e) {
                logger.error("Failed to execute getHeadRev : " + e.getMessage());
                result.setStatus(HttpStatus.NOT_FOUND.getCode());
                result.setBody(e.getMessage());
            }
        } else {
            result.setStatus(HttpStatus.BAD_REQUEST.getCode());
            result.setBody("Missing input for testing");
        }
        return result;
    }

    /**
     * Insert of update(if already exist) of credentials in Jenkins.
     * If credentialsInfo contains credentialsId - we update existing credentials with new user/password, otherwise - create new credentials
     *
     * @param credentialsInfo contains values to insert / update - exist credentials will be updated or recreate if deleted in jenkins
     * @return OctaneResponse created/updated credentials with filled credentials id as body
     */
    public static OctaneResponse upsertRepositoryCredentials(final CredentialsInfo credentialsInfo) {

        OctaneResponse result = DTOFactory.getInstance().newDTO(OctaneResponse.class);
        result.setStatus(HttpStatus.CREATED.getCode());

        if (StringUtils.isNotEmpty(credentialsInfo.getCredentialsId())) {
            BaseStandardCredentials cred = getCredentialsById(credentialsInfo.getCredentialsId());
            if (cred != null) {
                BaseStandardCredentials newCred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsInfo.getCredentialsId(),
                        null, credentialsInfo.getUsername(), credentialsInfo.getPassword());
                CredentialsStore store = new SystemCredentialsProvider.StoreImpl();
                try {
                    store.updateCredentials(Domain.global(), cred, newCred);
                    result.setStatus(HttpStatus.CREATED.getCode());
                    result.setBody(newCred.getId());
                } catch (IOException e) {
                    logger.error("Failed to update credentials " + e.getMessage());
                    result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                    result.setBody("Failed to update credentials " + e.getMessage());
                }
                return result;
            }
        }
        if (StringUtils.isNotEmpty(credentialsInfo.getUsername()) && credentialsInfo.getPassword() != null) {
            BaseStandardCredentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsInfo.getCredentialsId(), null, credentialsInfo.getUsername(), credentialsInfo.getPassword());
            CredentialsStore store = new SystemCredentialsProvider.StoreImpl();
            try {
                store.addCredentials(Domain.global(), c);
                result.setStatus(HttpStatus.CREATED.getCode());
                result.setBody(c.getId());
            } catch (IOException e) {
                logger.error("Failed to add credentials " + e.getMessage());
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                result.setBody("Failed to add credentials " + e.getMessage());
            }
        }

        return result;
    }

    private static BaseStandardCredentials getCredentialsById(String credentialsId) {
        List<BaseStandardCredentials> list = CredentialsProvider.lookupCredentials(BaseStandardCredentials.class, (Item) null, null, (DomainRequirement) null);
        for (BaseStandardCredentials cred : list) {
            if (cred.getId().equals(credentialsId))
                return cred;
        }
        return null;
    }

    private static List<String> checkCIPermissions(final Jenkins jenkins, boolean hasCredentials) {
        List<String> result = new ArrayList<>();
        checkPermissions(jenkins, result, requirePremissions);
        if (hasCredentials) {
            checkPermissions(jenkins, result, credentialsPremissions);
        }
        return result;
    }

    private static void checkPermissions(Jenkins jenkins, List<String> result, Map<Permission, String> permissions) {
        for (Permission permission : permissions.keySet()) {
            if (!jenkins.hasPermission(permission)) {
                result.add(permissions.get(permission));
            }
        }
    }

    private static Map<Permission, String> initRequirePremissions() {
        Map<Permission, String> result = new HashedMap();
        result.put(Item.CREATE, "Job.CREATE");
        result.put(Item.DELETE, "Job.DELETE");
        result.put(Item.READ, "Job.READ");
        return result;
    }

    private static Map<Permission, String> initCredentialsPremissions() {
        Map<Permission, String> result = new HashedMap();
        result.put(CredentialsProvider.CREATE, "Credentials.CREATE");
        result.put(CredentialsProvider.UPDATE, "Credentials.UPDATE");
        return result;

    }
}
