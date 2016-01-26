package com.hp.octane.plugins.jenkins.model.pipelines;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by lazara on 26/01/2016.
 */
public class PipelinesFactory {

    private static final Logger logger = Logger.getLogger(PipelinesFactory.class.getName());

    public static StructureItem createStructureItem(AbstractProject project) {

        StructureItem structureItem = new StructureItem();
        structureItem.setName(project.getName());
        structureItem.setParameters(ParameterProcessors.getConfigs(project));

        AbstractProjectProcessor projectProcessor = AbstractProjectProcessor.getFlowProcessor(project);
        structureItem.setInternals(projectProcessor.getInternals());
        structureItem.setPostBuilds(projectProcessor.getPostBuilds());

        return structureItem;
    }

    public static StructurePhase createStructurePhase(String name, boolean blocking, List<AbstractProject> items) {

        StructurePhase structurePhase = new StructurePhase();
        structurePhase.setName(name);
        structurePhase.setBlocking(blocking);

        StructureItem[] tmp = new StructureItem[items.size()];
        for (int i = 0; i < tmp.length; i++) {
            if (items.get(i) != null) {
                tmp[i] = PipelinesFactory.createStructureItem(items.get(i));

            } else {
                logger.warning("One of referenced jobs is null, your Jenkins config probably broken, skipping this job...");
            }
        }

        structurePhase.setJobs(Arrays.asList(tmp));

        return structurePhase;
    }

    public static BuildHistory.SCMUser createScmUser(User user){
        BuildHistory.SCMUser scmUser = new BuildHistory.SCMUser();
        scmUser.setDisplayName(user.getDisplayName());
        scmUser.setFullName(user.getFullName());
        scmUser.setId(user.getId());

        return scmUser;
    }

    public static Set<BuildHistory.SCMUser> createScmUsersList(Set<User> users) {
        Set<BuildHistory.SCMUser> userList =PipelinesFactory.createScmUsersList(users);

        for(User user: users){
            userList.add(PipelinesFactory.createScmUser(user));
        }
        return userList;
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd) {
        return createParameterConfig(pd, ParameterType.UNKNOWN, null, null);
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type) {
        return createParameterConfig(pd, type, null, null);
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type, Object defaultValue) {
        return createParameterConfig(pd, type, defaultValue, null);
    }

    public static ParameterConfig createParameterConfig(String name,ParameterType  type, List<Object> choices) {
        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setName(name);
        parameterConfig.setType(type);
        parameterConfig.setDescription("");
        parameterConfig.setChoices(choices.toArray());
        return parameterConfig;
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type, Object defaultValue, List<Object> choices) {

        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setName(pd.getName());
        parameterConfig.setType(type);
        parameterConfig.setDescription(pd.getDescription());
        ParameterValue tmp;
        if (type != ParameterType.UNKNOWN) {
            if (defaultValue != null || type == ParameterType.PASSWORD) {
                parameterConfig.setDefaultValue(defaultValue);
            } else {
                tmp = pd.getDefaultParameterValue();
                parameterConfig.setDefaultValue(tmp == null ? "" : tmp.getValue());
            }
            parameterConfig.setChoices(choices.toArray());
        }

        return parameterConfig;
    }

    public static ParameterConfig createParameterConfig(ParameterConfig pc) {
        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setName(pc.getName());
        parameterConfig.setType(pc.getType());
        parameterConfig.setDescription(pc.getDescription());
        parameterConfig.setChoices(pc.getChoices());
        parameterConfig.setDefaultValue(pc.getDefaultValue());

        return parameterConfig;
    }

}
