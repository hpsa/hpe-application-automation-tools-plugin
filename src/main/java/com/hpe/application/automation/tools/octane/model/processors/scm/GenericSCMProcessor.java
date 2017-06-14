/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.model.processors.scm;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.scm.SCMChange;
import com.hp.octane.integrations.dto.scm.SCMCommit;
import com.hp.octane.integrations.dto.scm.SCMData;
import com.hp.octane.integrations.dto.scm.SCMRepository;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benmeior on 9/8/2016.
 */

public class GenericSCMProcessor implements SCMProcessor {
    private static final Logger logger = LogManager.getLogger(GenericSCMProcessor.class);
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();

    GenericSCMProcessor(){
    }

    @Override
    public SCMData getSCMData(AbstractBuild build) {
        SCMData result;
        SCMRepository repository = buildScmRepository();

        ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
        ArrayList<SCMCommit> tmpCommits = buildScmCommits(changes);

        result = dtoFactory.newDTO(SCMData.class)
                .setCommits(tmpCommits)
                .setRepository(repository);

        return result;
    }

    @Override
    public List<SCMData> getSCMData(WorkflowRun run) {
        // todo: implement default - yanivl
        return null;
    }

    private ArrayList<SCMCommit> buildScmCommits(ChangeLogSet<ChangeLogSet.Entry> changes) {
        ArrayList<SCMCommit> tmpCommits = new ArrayList<>();
        ArrayList<SCMChange> tmpChanges;
        SCMChange tmpChange;

        for (ChangeLogSet.Entry c : changes) {
            User user = c.getAuthor();
            String userEmail = null;

            tmpChanges = new ArrayList<>();

            for (ChangeLogSet.AffectedFile item : c.getAffectedFiles()) {
                tmpChange = dtoFactory.newDTO(SCMChange.class)
                        .setType(item.getEditType().getName())
                        .setFile(item.getPath());
                tmpChanges.add(tmpChange);
            }

            for (UserProperty property : user.getAllProperties()) {
                if (property instanceof Mailer.UserProperty) {
                    userEmail = ((Mailer.UserProperty) property).getAddress();
                }
            }
            SCMCommit tmpCommit = buildScmCommit(tmpChanges, c, userEmail);
            tmpCommits.add(tmpCommit);
        }
        return tmpCommits;
    }

    private SCMCommit buildScmCommit(ArrayList<SCMChange> tmpChanges, ChangeLogSet.Entry commit, String userEmail) {
        return dtoFactory.newDTO(SCMCommit.class)
                        .setTime(commit.getTimestamp())
                        .setUser(commit.getAuthor().getId())
                        .setUserEmail(userEmail)
                        .setRevId(commit.getCommitId())
                        .setComment(commit.getMsg().trim())
                        .setChanges(tmpChanges);
    }

    private SCMRepository buildScmRepository() {
        return dtoFactory.newDTO(SCMRepository.class)
                    .setType(SCMType.UNKNOWN);
    }
}
