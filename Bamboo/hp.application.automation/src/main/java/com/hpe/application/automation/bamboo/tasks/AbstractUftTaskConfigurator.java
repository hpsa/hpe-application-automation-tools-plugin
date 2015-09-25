package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by schernikov on 7/31/2015.
 */
public abstract class AbstractUftTaskConfigurator extends AbstractTaskConfigurator implements TaskRequirementSupport {

    @NotNull
    @Override
    public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
        return defineUftRequirement();
    }
    private Set<Requirement> defineUftRequirement() {
        RequirementImpl uftReq = new RequirementImpl(CapabilityUftDefaultsHelper.CAPABILITY_UFT, true, ".*");
        Set<Requirement> result = new HashSet<Requirement>();
        result.add(uftReq);
        return result;
    }

}
