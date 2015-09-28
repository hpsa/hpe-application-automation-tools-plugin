/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.DefaultJob;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;

public class HpTasksArtifactRegistrator {
    public void registerCommonArtifact(@NotNull Object job, @NotNull  I18nBean i18nBean, @NotNull ArtifactDefinitionManager artifactDefinitionManager)
    {
        if (job == null || !(job instanceof DefaultJob) || i18nBean == null || artifactDefinitionManager == null)
            return;

        Job defaultJob = (Job)job;
        String name = i18nBean.getText("AllTasksArtifactDefinitionLabel");
        String ARTIFACT_COPY_PATTERN = TestResultHelper.HP_UFT_PREFIX + "${bamboo.buildNumber}/**";
        if (null == artifactDefinitionManager.findArtifactDefinition(defaultJob, name)) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(defaultJob);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}
