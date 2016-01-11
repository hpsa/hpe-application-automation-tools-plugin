package com.hp.octane.plugins.common.bridge.tasks;

import com.hp.octane.plugins.jetbrains.teamcity.actions.StatusActionController;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;

/**
 * Created by linsha on 07/01/2016.
 */
public class TeamcityCITaskService implements CITaskService{
    @Override
    public String getProjects(boolean withParameters) {
        return null;
    }

    @Override
    public String getStatus() {
        StringBuilder data = new StringBuilder();
        data.append(Utils.jacksonRendering(new StatusActionController.PluginStatus()));
        return data.toString();
    }

    @Override
    public String getStructure(String id) {
        return null;
    }

    @Override
    public String getSnapshot(String id) {
        return null;
    }
}
