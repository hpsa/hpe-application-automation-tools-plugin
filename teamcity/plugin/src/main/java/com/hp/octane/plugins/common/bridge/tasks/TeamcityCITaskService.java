package com.hp.octane.plugins.common.bridge.tasks;

import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.actions.StatusActionController;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.TeamCityModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.beans.factory.annotation.Autowired;
import com.hp.octane.serialization.SerializationService;

/**
 * Created by linsha on 07/01/2016.
 */
public class TeamcityCITaskService implements CITaskService{

    @Override
    public String getProjects(boolean withParameters) {
        ModelFactory modelFactory = new TeamCityModelFactory(NGAPlugin.getInstance().getProjectManager());
        ProjectsList projectsList = modelFactory.CreateProjectList();
        return Utils.jacksonRendering(projectsList);
    }

    @Override
    public String getStatus() {
        StringBuilder data = new StringBuilder();
        data.append(SerializationService.toJSON(new StatusActionController.PluginStatus()));
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
