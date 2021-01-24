/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class JellyUtils {

    public static final String NONE = "none";
    public static final String NONE_DISPLAY = "- none - ";

    private JellyUtils(){
        //for code climate
    }

    public static ListBoxModel createComboModelWithNoneValue() {
        ListBoxModel m = new ListBoxModel();
        m.add(NONE_DISPLAY, NONE);
        return m;
    }

    public static ListBoxModel fillWorkspaceModel(String configurationId, String workspaceId) {
        ListBoxModel m = createComboModelWithNoneValue();
        if (StringUtils.isNotEmpty(configurationId) && !NONE.equals(configurationId)) {
            try {
                EntitiesService entitiesService = OctaneSDK.getClientByInstanceId(configurationId).getEntitiesService();
                List<Entity> workspaces = entitiesService.getEntities(null, "workspaces", null, null);
                for (Entity workspace : workspaces) {
                    m.add(workspace.getId() + " " + workspace.getName(), String.valueOf(workspace.getId()));
                }
            } catch (Exception e) {
                //octane configuration not found
                m.add(workspaceId, workspaceId);
                return m;
            }
        }
        return m;
    }

    public static ListBoxModel fillConfigurationIdModel() {
        ListBoxModel m = createComboModelWithNoneValue();

        for (OctaneClient octaneClient : OctaneSDK.getClients()) {
            OctaneServerSettingsModel model = ConfigurationService.getSettings(octaneClient.getInstanceId());
            m.add(model.getCaption(), model.getIdentity());
        }
        return m;
    }


    public static ListBoxModel fillCredentialsIdItems(Item project, String credentialsId, CredentialsMatcher credentialsMatcher) {

        if (project == null || !project.hasPermission(Item.CONFIGURE)) {
            return new StandardUsernameListBoxModel().includeCurrentValue(credentialsId);
        }

        return new StandardListBoxModel()
                .includeEmptyValue()
                .includeMatchingAs(
                        project instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) project) : ACL.SYSTEM,
                        project,
                        StandardCredentials.class,
                        URIRequirementBuilder.create().build(),
                        credentialsMatcher)
                .includeCurrentValue(credentialsId);
    }

}
