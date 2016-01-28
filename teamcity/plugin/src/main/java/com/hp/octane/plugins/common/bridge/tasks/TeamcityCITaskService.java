package com.hp.octane.plugins.common.bridge.tasks;

import com.hp.nga.integrations.dto.projects.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.actions.StatusActionController;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.TeamCityModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
import com.hp.nga.integrations.serialization.SerializationService;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotItem;

/**
 * Created by linsha on 07/01/2016.
 */
public class TeamcityCITaskService implements CITaskService{

    @Override
    public String getProjects(boolean withParameters) {
        //test
        ModelFactory modelFactory = new TeamCityModelFactory(NGAPlugin.getInstance().getProjectManager());
        ProjectsList projectsList = modelFactory.CreateProjectList();
        return SerializationService.toJSON(projectsList);
    }

    @Override
    public String getStatus() {
        StringBuilder data = new StringBuilder();
        data.append(SerializationService.toJSON(new StatusActionController.PluginStatus()));
        return data.toString();
    }

    @Override
    public String getStructure(String id) {
        ModelFactory modelFactory = new TeamCityModelFactory(NGAPlugin.getInstance().getProjectManager());
        StructureItem treeRoot =  modelFactory.createStructure(id);
        return SerializationService.toJSON(treeRoot);
    }

    @Override
    public String getSnapshot(String id) {
        ModelFactory modelFactory = new TeamCityModelFactory(NGAPlugin.getInstance().getProjectManager());
        SnapshotItem snapshotItem = modelFactory.createSnapshot(id);
        return SerializationService.toJSON(snapshotItem);
    }
}
