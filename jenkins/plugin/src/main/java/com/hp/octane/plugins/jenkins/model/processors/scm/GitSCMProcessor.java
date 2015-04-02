package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.plugins.jenkins.model.scm.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by gullery on 31/03/2015.
 */

public class GitSCMProcessor extends AbstractSCMProcessor {
	GitSCMProcessor() {
	}

	@Override
	public SCMData getSCMChanges(AbstractBuild build) {
		ArrayList<SCMRepository> repositories = new ArrayList<SCMRepository>();
		AbstractProject project = build.getProject();
		GitSCM scmGit;
		SCMType tmpType;
		SCMRepository tmpRepo;
		SCMConfiguration tmpConfiguration;
		ArrayList<SCMCommit> tmpCommits;
		SCMCommit tmpCommit;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		BuildData buildData;
		Revision buildCommitRev;
		Set<String> repoUris;
		GitChangeSet gitChange;
		if (project.getScm() instanceof GitSCM) {
			tmpType = SCMType.GIT;
			scmGit = (GitSCM) project.getScm();
			buildData = scmGit.getBuildData(build);
			if (buildData != null) {
				buildCommitRev = buildData.getLastBuiltRevision();
				repoUris = buildData.getRemoteUrls();
				if (!repoUris.iterator().hasNext()) return null;
				tmpConfiguration = new SCMConfiguration(
						tmpType,
						repoUris.iterator().next(),
						buildCommitRev.getBranches().iterator().hasNext() ? buildCommitRev.getBranches().iterator().next().getName() : null,
						buildCommitRev.getSha1String()
				);
				tmpCommits = new ArrayList<SCMCommit>();
				for (ChangeLogSet.Entry change : changes) {
					if (change instanceof GitChangeSet) {
						gitChange = (GitChangeSet) change;
						tmpCommit = new SCMCommit(
								gitChange.getId(),
								gitChange.getComment().trim(),
								gitChange.getTimestamp()
						);
						tmpCommit.setUser(
								gitChange.getAuthor().getId(),
								gitChange.getAuthorName(),
								gitChange.getAuthor().getProperty(Mailer.UserProperty.class).getAddress()
						);
						for (GitChangeSet.Path item : gitChange.getAffectedFiles()) {
							tmpCommit.addChange(
									item.getEditType().getName(),
									item.getPath()
							);
						}
						tmpCommits.add(tmpCommit);
					}
				}
				tmpRepo = new SCMRepository(tmpConfiguration, tmpCommits.toArray(new SCMCommit[tmpCommits.size()]));
				repositories.add(tmpRepo);
			}
		}
		return new SCMData(repositories);
	}
}
