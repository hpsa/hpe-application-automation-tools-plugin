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

    private I18nBean i18nBean;
    private ArtifactDefinitionManager artifactDefinitionManager;

    public void setI18nBean(I18nBean i18nBean){
        this.i18nBean = i18nBean;
    }

    public void setArtifactDefinitionManager(ArtifactDefinitionManager artifactDefinitionManager){
        this.artifactDefinitionManager = artifactDefinitionManager;
    }

    public void registerCommonArtifact(Object job)
    {
        if (job == null || !(job instanceof DefaultJob))
            return;

        Job defaultJob = (Job)job;
        String name = this.i18nBean.getText("AllTasksArtifactDefinitionLabel");
        String ARTIFACT_COPY_PATTERN = "**";
        if (null == artifactDefinitionManager.findArtifactDefinition(defaultJob, name)) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "${bamboo.buildNumber}", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(defaultJob);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}
