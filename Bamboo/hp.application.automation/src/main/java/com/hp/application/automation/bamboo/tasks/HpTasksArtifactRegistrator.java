package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.DefaultJob;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.utils.i18n.I18nBean;

/**
 * Created by ybobrik on 9/10/2015.
 */
public class HpTasksArtifactRegistrator {
    public void registerCommonArtifact(Object job, I18nBean i18nBean, ArtifactDefinitionManager artifactDefinitionManager)
    {
        if (job == null || !(job instanceof DefaultJob) || i18nBean == null || artifactDefinitionManager == null)
            return;

        Job defaultJob = (Job)job;
        String name = i18nBean.getText("AllTasksArtifactDefinitionLabel");
        String ARTIFACT_COPY_PATTERN = "**";
        if (null == artifactDefinitionManager.findArtifactDefinition(defaultJob, name)) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "${bamboo.buildNumber}", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(defaultJob);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}
