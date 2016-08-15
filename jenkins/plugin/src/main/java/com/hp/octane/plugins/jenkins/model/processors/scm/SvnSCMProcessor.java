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
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionChangeLogSet;
import hudson.scm.SubversionSCM;
import hudson.tasks.Mailer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		List<SCMChange> tmpChanges;
		SCMChange tmpChange;
		ChangeLogSet<ChangeLogSet.Entry> changes = build.getChangeSet();
		SubversionChangeLogSet.LogEntry commit;

		if (project.getScm() instanceof SubversionSCM) {
			svnData = (SubversionSCM) project.getScm();
			scmRepository = getSCMRepository(svnData);

			try {
				String revisionStateStr = String.valueOf(svnData.calcRevisionsFromBuild(build, null, null));
				builtRevId = getRevisionIdFromBuild(revisionStateStr, scmRepository.getUrl());
			} catch (IOException e) {
				logger.error("failed to get revision state", e);
            } catch (InterruptedException e) {
				logger.error("failed to get revision state", e);
			}

			tmpCommits = new ArrayList<SCMCommit>();
			for (ChangeLogSet.Entry c : changes) {
				if (c instanceof SubversionChangeLogSet.LogEntry) {
					SubversionChangeLogSet.LogEntry commit = (SubversionChangeLogSet.LogEntry) c;
					User user = commit.getAuthor();
					String userEmail = null;

					tmpChanges = new ArrayList<SCMChange>();
					for (SubversionChangeLogSet.Path item : commit.getAffectedFiles()) {
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

					SCMCommit tmpCommit = dtoFactory.newDTO(SCMCommit.class)
							.setTime(commit.getTimestamp())
							.setUser(commit.getAuthor().getId())
							.setUserEmail(userEmail)
							.setRevId(commit.getCommitId())
							.setComment(commit.getMsg().trim())
							.setChanges(tmpChanges);
					tmpCommits.add(tmpCommit);
				}
			}

			result = dtoFactory.newDTO(SCMData.class)
					.setRepository(scmRepository)
                    .setBuiltRevId(builtRevId)
					.setCommits(tmpCommits);
		}
		return result;
	}

	private String getRevisionIdFromBuild(String revisionStateStr, String repositoryUrl) {
		Matcher m = Pattern.compile("(\\d+)").matcher(
				revisionStateStr.substring(revisionStateStr.indexOf(repositoryUrl) + repositoryUrl.length() + 1)
		);
		if (!m.find()) {
			return null;
		}
		return m.group(1);
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