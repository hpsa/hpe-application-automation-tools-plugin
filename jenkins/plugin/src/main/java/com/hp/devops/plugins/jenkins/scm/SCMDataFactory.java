package com.hp.devops.plugins.jenkins.scm;

import com.hp.devops.scm.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import hudson.scm.NullSCM;
import hudson.tasks.Mailer;
import org.eclipse.jgit.transport.RemoteConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 15/10/14
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public final class SCMDataFactory {
	static public SCMData create(AbstractProject project) {
		ArrayList<SCMRepository> repositories = new ArrayList<SCMRepository>();
		GitSCM scmGit;
		SCMType tmpType;
		SCMRepository tmpRepo;
		List<UserRemoteConfig> remoteConfigs;
		if (project.getScm() instanceof NullSCM) return null;
		if (project.getScm() instanceof GitSCM) {
			tmpType = SCMType.GIT;
			scmGit = (GitSCM) project.getScm();
			remoteConfigs = scmGit.getUserRemoteConfigs();
			for (UserRemoteConfig remoteConfig : remoteConfigs) {
				tmpRepo = new SCMRepository(
						tmpType,
						remoteConfig.getUrl()
				);
				repositories.add(tmpRepo);
			}
		}
		return new SCMData(repositories);
	}

	static public SCMData create(AbstractBuild build) {
		ArrayList<SCMRepository> repositories = new ArrayList<SCMRepository>();
		AbstractProject project = build.getProject();
		GitSCM scmGit;
		SCMType tmpType;
		SCMRepository tmpRepo;
		SCMCommit tmpCommit;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		BuildData buildData;
		Revision buildCommitRev;
		Set<String> repoUris;
		GitChangeSet gitChange;
		if (project.getScm() instanceof NullSCM) return null;
		if (project.getScm() instanceof GitSCM) {
			tmpType = SCMType.GIT;
			scmGit = (GitSCM) project.getScm();
			buildData = scmGit.getBuildData(build);
			buildCommitRev = buildData.getLastBuiltRevision();
			repoUris = buildData.getRemoteUrls();
			if (!repoUris.iterator().hasNext()) return null;
			tmpRepo = new SCMRepository(
					tmpType,
					repoUris.iterator().next(),
					buildCommitRev.getSha1String(),
					buildCommitRev.getBranches().iterator().hasNext() ? buildCommitRev.getBranches().iterator().next().getName() : null
			);
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
					tmpRepo.addCommit(tmpCommit);
				}
			}
			repositories.add(tmpRepo);
		}
		return new SCMData(repositories);
	}
}
