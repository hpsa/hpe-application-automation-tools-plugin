package com.microfocus.application.automation.tools.octane;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class JellyUtils {

    public static final String NONE = "none";
    public static final String NONE_DISPLAY = "- none - ";

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

}
