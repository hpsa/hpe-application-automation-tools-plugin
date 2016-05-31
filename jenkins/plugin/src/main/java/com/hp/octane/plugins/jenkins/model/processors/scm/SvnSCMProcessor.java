package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.scm.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionChangeLogSet;
import hudson.scm.SubversionSCM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benmeior on 5/15/2016.
 */
public class SvnSCMProcessor implements SCMProcessor {
	private static final Logger logger = LogManager.getLogger(SvnSCMProcessor.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public SCMData getSCMData(AbstractBuild build) {
		AbstractProject project = build.getProject();
		SCMData result = null;
		SubversionSCM svnData;
		SCMRepository scmRepository;
		ArrayList<SCMCommit> tmpCommits;
		SCMCommit tmpCommit;
		List<SCMChange> tmpChanges;
		SCMChange tmpChange;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		SubversionChangeLogSet.LogEntry commit;

		if (project.getScm() instanceof SubversionSCM) {
			svnData = (SubversionSCM) project.getScm();
			scmRepository = getSCMRepository(svnData);

			tmpCommits = new ArrayList<SCMCommit>();
			for (ChangeLogSet.Entry c : changes) {
				if (c instanceof SubversionChangeLogSet.LogEntry) {
					commit = (SubversionChangeLogSet.LogEntry) c;

					tmpChanges = new ArrayList<SCMChange>();
					for (SubversionChangeLogSet.Path item : commit.getAffectedFiles()) {
						tmpChange = dtoFactory.newDTO(SCMChange.class)
								.setType(item.getEditType().getName())
								.setFile(item.getPath());
						tmpChanges.add(tmpChange);
					}
					tmpCommit = dtoFactory.newDTO(SCMCommit.class)
							.setTime(commit.getTimestamp())
							.setUser(commit.getAuthor().getId())
							.setRevId(commit.getCommitId())
							.setComment(commit.getMsg().trim())
							.setChanges(tmpChanges);
					tmpCommits.add(tmpCommit);
				}
			}

			result = dtoFactory.newDTO(SCMData.class)
					.setRepository(scmRepository)
					.setCommits(tmpCommits);
		}
		return result;
	}

	private SCMRepository getSCMRepository(SubversionSCM svnData) {
		SCMRepository result;
		String url = null;
		if (svnData.getLocations().length == 1) {
			url = svnData.getLocations()[0].getURL();
		}
		result = dtoFactory.newDTO(SCMRepository.class)
				.setType(SCMType.SVN)
				.setUrl(url);
		return result;
	}
}