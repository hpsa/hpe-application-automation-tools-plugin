package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.nga.integrations.dto.scm.SCMCommit;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.scm.SCMRepository;
import com.hp.nga.integrations.dto.scm.SCMType;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by gullery on 31/03/2015.
 */

public class GitSCMProcessor implements SCMProcessor {
	private static final Logger logger = Logger.getLogger(GitSCMProcessor.class.getName());

	GitSCMProcessor() {
	}

	@Override
	public SCMData getSCMData(AbstractBuild build) {
		AbstractProject project = build.getProject();
		SCMData result = null;
		GitSCM scmGit;
		SCMRepository scmRepository;
		String builtCommitRevId = null;
		ArrayList<SCMCommit> tmpCommits;
		SCMCommit tmpCommit;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		BuildData buildData;
		GitChangeSet commit;

		if (project.getScm() instanceof GitSCM) {
			scmGit = (GitSCM) project.getScm();
			buildData = scmGit.getBuildData(build);
			if (buildData != null) {
				scmRepository = getSCMRepository(scmGit, build);
				if (buildData.getLastBuiltRevision() != null) {
					builtCommitRevId = buildData.getLastBuiltRevision().getSha1String();
				}

				tmpCommits = new ArrayList<SCMCommit>();
				for (ChangeLogSet.Entry c : changes) {
					if (c instanceof GitChangeSet) {
						commit = (GitChangeSet) c;
						tmpCommit = new SCMCommit(
								commit.getTimestamp(),
								commit.getAuthor().getId(),
								commit.getCommitId(),
								commit.getParentCommit(),
								commit.getComment().trim()
						);

						//  [YG] Changes will be handled later
//						for (GitChangeSet.Path item : commit.getAffectedFiles()) {
//							tmpCommit.addChange(
//									item.getEditType().getName(),
//									item.getPath()
//							);
//						}
						tmpCommits.add(tmpCommit);
					}
				}

				result = new SCMData(scmRepository, builtCommitRevId, tmpCommits.toArray(new SCMCommit[tmpCommits.size()]));
			}
		}
		return result;
	}

	private SCMRepository getSCMRepository(GitSCM gitData, AbstractBuild build) {
		SCMRepository result = null;
		String url = null;
		String branch = null;
		if (gitData != null && gitData.getBuildData(build) != null) {
			BuildData buildData = gitData.getBuildData(build);
			if (!buildData.getRemoteUrls().isEmpty()) {
				url = (String) buildData.getRemoteUrls().toArray()[0];
			}
			if (buildData.getLastBuiltRevision() != null && !buildData.getLastBuiltRevision().getBranches().isEmpty()) {
				branch = ((Branch) buildData.getLastBuiltRevision().getBranches().toArray()[0]).getName();
			}

			result = new SCMRepository(
					SCMType.GIT,
					url,
					branch);
		}
		return result;
	}
}
