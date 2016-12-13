package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.scm.SCMChange;
import com.hp.octane.integrations.dto.scm.SCMCommit;
import com.hp.octane.integrations.dto.scm.SCMData;
import com.hp.octane.integrations.dto.scm.SCMRepository;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 31/03/2015.
 */

public class GitSCMProcessor implements SCMProcessor {
	private static final Logger logger = LogManager.getLogger(GitSCMProcessor.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	GitSCMProcessor() {
	}

	@Override
	public SCMData getSCMData(AbstractBuild build) {
		AbstractProject project = build.getProject();
		SCMData result = null;
		GitSCM scmGit;
		SCMRepository scmRepository;
		String builtCommitRevId = null;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		BuildData buildData;

		if (project.getScm() instanceof GitSCM) {
			scmGit = (GitSCM) project.getScm();
			buildData = scmGit.getBuildData(build);
			if (buildData != null) {
				scmRepository = getSCMRepository(scmGit, build);
				if (buildData.getLastBuiltRevision() != null) {
					builtCommitRevId = buildData.getLastBuiltRevision().getSha1String();
				}

				ArrayList<SCMCommit> tmpCommits = new ArrayList<SCMCommit>();
				for (ChangeLogSet.Entry c : changes) {
					if (c instanceof GitChangeSet) {
						GitChangeSet commit = (GitChangeSet) c;
						User user = commit.getAuthor();
						String userEmail = null;

						List<SCMChange> tmpChanges = new ArrayList<SCMChange>();
						for (GitChangeSet.Path item : commit.getAffectedFiles()) {
							SCMChange tmpChange = dtoFactory.newDTO(SCMChange.class)
									.setType(item.getEditType().getName())
									.setFile(item.getPath());
							tmpChanges.add(tmpChange);
						}

						for (UserProperty property : user.getAllProperties()) {
							if (property instanceof Mailer.UserProperty) {
								userEmail = ((Mailer.UserProperty) property).getAddress();
							}
						}

						SCMCommit tmpCommit = dtoFactory.newDTO(SCMCommit.class)
								.setTime(commit.getTimestamp())
								.setUser(user.getId())
								.setUserEmail(userEmail)
								.setRevId(commit.getCommitId())
								.setParentRevId(commit.getParentCommit())
								.setComment(commit.getComment().trim())
								.setChanges(tmpChanges);

						tmpCommits.add(tmpCommit);
					}
				}

				result = dtoFactory.newDTO(SCMData.class)
						.setRepository(scmRepository)
						.setBuiltRevId(builtCommitRevId)
						.setCommits(tmpCommits);
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
			result = dtoFactory.newDTO(SCMRepository.class)
					.setType(SCMType.GIT)
					.setUrl(url)
					.setBranch(branch);
		}
		return result;
	}
}
