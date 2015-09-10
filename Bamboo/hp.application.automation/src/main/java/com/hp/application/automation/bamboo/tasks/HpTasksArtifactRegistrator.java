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

        DefaultJob defaultJob = (DefaultJob)job;
        Job j = (Job)defaultJob;
        final String ARTIFACT_NAME = this.i18nBean.getText("AllTasksArtifactDefinitionLabel");
        final String ARTIFACT_COPY_PATTERN = "**";
        if (null == artifactDefinitionManager.findArtifactDefinition(j, ARTIFACT_NAME)) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(ARTIFACT_NAME, "${bamboo.buildNumber}", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(j);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}
