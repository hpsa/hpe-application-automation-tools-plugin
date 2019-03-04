/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.model.processors.scm;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.scm.*;
import com.hp.octane.integrations.dto.scm.impl.LineRange;
import com.hp.octane.integrations.dto.scm.impl.RevisionsMap;
import com.hp.octane.integrations.dto.scm.impl.SCMFileBlameImpl;
import hudson.FilePath;
import hudson.model.*;
import hudson.plugins.git.Branch;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import hudson.plugins.git.util.BuildData;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.tasks.Mailer;
import hudson.util.DescribableList;
import jenkins.MasterToSlaveFileCallable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by gullery on 31/03/2015.
 */

class GitSCMProcessor implements SCMProcessor {
    private static final Logger logger = LogManager.getLogger(GitSCMProcessor.class);
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();
    private static final String MASTER = "refs/remotes/origin/master";

    @Override
    public SCMData getSCMData(AbstractBuild build, SCM scm) {
        List<ChangeLogSet<? extends ChangeLogSet.Entry>> changes = new ArrayList<>();
        changes.add(build.getChangeSet());
        SCMData scmData = extractSCMData(build, scm, changes);
        scmData = enrichLinesOnSCMData(scmData, build);
        return scmData;
    }

    /**
     * this method go over each of the changed files and enrich line changes
     * into existing scm events, so that the new enriched events will have line ranges.
     * in addition, for each renamed file, we enrich inside delete event the 'renamed to' file
     *
     * @param scmData
     * @param build
     */
    private SCMData enrichLinesOnSCMData(SCMData scmData, AbstractBuild build) {
        long startTime = System.currentTimeMillis();
        try {
            FileRepository repo = new FileRepository(new File(getRemoteString(build) + File.separator + ".git"));
            RevWalk rw = new RevWalk(repo);
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setRepository(repo);
            df.setDetectRenames(true);

            //add blame data to scm data
            Set<String> committedFiles = getCommittedFiles(scmData);
            List<SCMFileBlame> fileBlameList = getBlameData(repo, committedFiles);
            scmData.setFileBlameList(fileBlameList);

            for (SCMCommit curCommit : scmData.getCommits()) {
                Map<String, SCMChange> fileChanges = new HashMap<>();
                curCommit.getChanges().forEach(change -> fileChanges.put(change.getFile(), change));
                RevCommit commit = rw.parseCommit(repo.resolve(curCommit.getRevId())); // Any ref will work here (HEAD, a sha1, tag, branch)
                RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

                List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
                // FOR EACH FILE
                for (DiffEntry diff : diffs) { // each file change will be in seperate diff
                    EditList fileEdits = df.toFileHeader(diff).toEditList();
                    switch (diff.getChangeType()) {
                        case ADD:
                            // old path == null, need to use new path
                            handleAddLinesDiff(fileEdits, fileChanges.get(diff.getNewPath()));
                            break;
                        case COPY:
                            // need to validate this type
                            handleModifyDiff(fileEdits, fileChanges.get(diff.getNewPath()));
                            break;
                        case DELETE:
                            // new path == null, need to use old path
                            handleDeleteLinesDiff(fileEdits, fileChanges.get(diff.getOldPath()));
                            break;
                        case MODIFY:
                            handleModifyDiff(fileEdits, fileChanges.get(diff.getNewPath()));
                            break;
                        case RENAME:
                            // enrich delete event with 'rename to' data
                            SCMChange deletedChange = fileChanges.get(diff.getOldPath());
                            SCMChange newRenamedFile = fileChanges.get(diff.getNewPath());
                            deletedChange.setRenamedToFile(newRenamedFile.getFile());
                            // handle changes
                            handleModifyDiff(fileEdits, fileChanges.get(diff.getNewPath()));
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (
                IOException e1) {
            logger.error("Line enricher: FAILED. could not enrich lines on SCM Data " + e1);
        }
        logger.info("Line enricher: process took: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
        return scmData;
    }

    private Set<String> getCommittedFiles(SCMData scmData) {
        Set<String> filesCommittedInPPR = new HashSet<>();
        for (SCMCommit curCommit : scmData.getCommits()) {
            curCommit.getChanges().forEach(change -> filesCommittedInPPR.add(change.getFile()));
        }
        return filesCommittedInPPR;
    }


    private List<SCMFileBlame> getBlameData(Repository repo, Set<String> files) {
        BlameCommand blamer = new BlameCommand(repo);
        List<SCMFileBlame> fileBlameList = new ArrayList<>();
        ObjectId commitID = null;
        try {
            for (String filePath : files) {
                commitID = repo.resolve(Constants.HEAD);
                blamer.setStartCommit(commitID);
                blamer.setFilePath(filePath);
                BlameResult blameResult = blamer.call();
                RawText rawText = blameResult.getResultContents();
                Integer fileSize = rawText.size();

                RevisionsMap revisionsMap = new RevisionsMap();

                if (fileSize > 0) {
                    String startRangeRevision = blameResult.getSourceCommit(0).getName();
                    Integer startRange = 1;
                    for (int i = 1; i < fileSize; i++) {
                        String currentRevision = blameResult.getSourceCommit(i).getName();
                        if (!currentRevision.equals(startRangeRevision)) {
                            LineRange range = new LineRange(startRange, i);//line numbers starting from 1 not from 0.
                            revisionsMap.addRangeToRevision(startRangeRevision, range);
                            startRange = i + 1;
                            startRangeRevision = currentRevision;
                        }
                    }
                }
                fileBlameList.add(new SCMFileBlameImpl(filePath, revisionsMap));

            }

            return fileBlameList;

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleModifyDiff(EditList fileEdits, SCMChange scmChange) {
        if (scmChange != null) {
            for (Edit edit : fileEdits) {
                switch (edit.getType()) {
                    case INSERT:
                        scmChange.insertAddedLines(new LineRange(edit.getBeginB() + 1, edit.getEndB()));
                        break;
                    case DELETE:
                        scmChange.insertDeletedLines(new LineRange(edit.getBeginA() + 1, edit.getEndA()));
                        break;
                    case REPLACE:
                        scmChange.insertDeletedLines(new LineRange(edit.getBeginA() + 1, edit.getEndA()));
                        scmChange.insertAddedLines(new LineRange(edit.getBeginB() + 1, edit.getEndB()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // probably it's useless to track deleted lines (inside scm change), consider removing it later.
    private void handleDeleteLinesDiff(EditList fileEdits, SCMChange scmChange) {
        if (scmChange != null) {
            for (Edit edit : fileEdits) {
                scmChange.insertDeletedLines(new LineRange(edit.getBeginA() + 1, edit.getEndA()));
            }
        }
    }

    private void handleAddLinesDiff(EditList fileEdits, SCMChange scmChange) {
        if (scmChange != null) {
            for (Edit edit : fileEdits) {
                scmChange.insertAddedLines(new LineRange(edit.getBeginB() + 1, edit.getEndB()));
            }
        }
    }

    @Override
    public SCMData getSCMData(WorkflowRun run, SCM scm) {
        return extractSCMData(run, scm, run.getChangeSets());
    }

    @Override
    public CommonOriginRevision getCommonOriginRevision(final Run run) {
        //for phase 1 this is hard coded since its not possible to calculate it, and configuration from outside will complicate the feature
        //so for this phase we keep it hardcoded.
        CommonOriginRevision commonOriginRevision = new CommonOriginRevision();
        commonOriginRevision.branch = getBranchName(run);

        try {
            final AbstractBuild abstractBuild = (AbstractBuild) run;
            FilePath workspace = ((AbstractBuild) run).getWorkspace();
            if (workspace != null) {
                File repoDir = new File(getRemoteString(abstractBuild) + File.separator + ".git");
                commonOriginRevision.revision = workspace.act(new FileContentCallable(repoDir));

            }
            logger.info("most recent common revision resolved to " + commonOriginRevision.revision + " (branch: " + commonOriginRevision.branch + ")");
        } catch (Exception e) {
            logger.error("failed to resolve most recent common revision", e);
            return commonOriginRevision;
        }
        return commonOriginRevision;
    }

    private static final class FileContentCallable extends MasterToSlaveFileCallable<String> {
        private final File repoDir;

        private FileContentCallable(File file) {
            this.repoDir = file;
        }

        @Override
        public String invoke(File rootDir, VirtualChannel channel) throws IOException {

            Git git = Git.open(repoDir);
            Repository repo = git.getRepository();
            if (repo == null) {
                return "";
            }
            final RevWalk walk = new RevWalk(repo);
            if (walk == null) {
                return "";
            }
            ObjectId resolveForCurrentBranch = repo.resolve(Constants.HEAD);
            if (resolveForCurrentBranch == null) {
                return "";
            }
            RevCommit currentBranchCommit = walk.parseCommit(resolveForCurrentBranch);
            if (currentBranchCommit == null) {
                return "";
            }
            ObjectId resolveForMaster = repo.resolve(MASTER);
            if (resolveForMaster == null) {
                return "";
            }
            RevCommit masterCommit = walk.parseCommit(resolveForMaster);

            walk.reset();
            walk.setRevFilter(RevFilter.MERGE_BASE);
            walk.markStart(currentBranchCommit);
            walk.markStart(masterCommit);
            RevCommit base = walk.next();
            if (base == null)
                return "";
            final RevCommit base2 = walk.next();
            if (base2 != null) {
                throw new NoMergeBaseException(NoMergeBaseException.MergeBaseFailureReason.MULTIPLE_MERGE_BASES_NOT_SUPPORTED,
                        MessageFormat.format(JGitText.get().multipleMergeBasesFor, currentBranchCommit.name(), masterCommit.name(), base.name(), base2.name()));
            }
            //in order to return actual revision and not merge commit
            while (base.getParents().length > 1) {
                RevCommit base_1 = base.getParent(0);
                RevCommit base_2 = base.getParent(1);
                if (base_1.getParents().length == 1) {
                    base = base_1;
                } else {
                    base = base_2;
                }
            }
            return base.getId().getName();
        }

    }

    private SCMData extractSCMData(Run run, SCM scm, List<ChangeLogSet<? extends ChangeLogSet.Entry>> changes) {
        if (!(scm instanceof GitSCM)) {
            throw new IllegalArgumentException("GitSCM type of SCM was expected here, found '" + scm.getClass().getName() + "'");
        }

        GitSCM gitData = (GitSCM) scm;
        SCMRepository repository;
        List<SCMCommit> tmpCommits;
        String builtRevId = null;

        repository = getRepository(run, gitData);

        BuildData buildData = gitData.getBuildData(run);
        if (buildData != null && buildData.getLastBuiltRevision() != null) {
            builtRevId = buildData.getLastBuiltRevision().getSha1String();
        }

        tmpCommits = extractCommits(changes);
        return dtoFactory.newDTO(SCMData.class)
                .setRepository(repository)
                .setBuiltRevId(builtRevId)
                .setCommits(tmpCommits);
    }

    private String getBranchName(Run r) {
        try {
            SCM scm = ((AbstractBuild) r).getProject().getScm();
            GitSCM git = (GitSCM) scm;
            List<BranchSpec> branches = git.getBranches();
            String rawBranchName = branches.get(0).toString();
            if (rawBranchName != null && rawBranchName.startsWith("${") && rawBranchName.endsWith("}")) {
                String param = rawBranchName.substring(2, rawBranchName.length() - 1);
                if (((AbstractBuild) r).getBuildVariables().get(param) != null) {
                    return ((AbstractBuild) r).getBuildVariables().get(param).toString();
                } else {
                    return param;
                }
            }
            if (rawBranchName != null && rawBranchName.startsWith("*/")) {
                return rawBranchName.substring(2);
            }
            return rawBranchName; //trunk the '*/' from the '*/<branch name>' in order to get clean branch name
        } catch (Exception e) {
            logger.error("failed to extract branch name", e);
        }
        return null;
    }

    private static String getRemoteString(AbstractBuild r) {
        final DescribableList<GitSCMExtension, GitSCMExtensionDescriptor> extensions = ((GitSCM) (r.getProject()).getScm()).getExtensions();
        String relativeTargetDir = "";
        if (extensions != null) {
            final RelativeTargetDirectory relativeTargetDirectory = extensions.get(RelativeTargetDirectory.class);
            if (relativeTargetDirectory != null && relativeTargetDirectory.getRelativeTargetDir() != null) {
                relativeTargetDir = File.separator + relativeTargetDirectory.getRelativeTargetDir();
            }
        }
        if (r.getWorkspace().isRemote()) {
            VirtualChannel vc = r.getWorkspace().getChannel();
            String fp = r.getWorkspace().getRemote();
            String remote = new FilePath(vc, fp).getRemote();
            return remote + relativeTargetDir;
        } else {
            String remote = r.getWorkspace().getRemote();
            return remote + relativeTargetDir;
        }
    }

    private SCMRepository getRepository(Run run, GitSCM gitData) {
        SCMRepository result = null;
        String url = null;
        String branch = null;
        if (gitData != null && gitData.getBuildData(run) != null) {
            BuildData buildData = gitData.getBuildData(run);
            if (buildData != null) {
                if (buildData.getRemoteUrls() != null && !buildData.getRemoteUrls().isEmpty()) {
                    url = (String) buildData.getRemoteUrls().toArray()[0];
                }
                if (buildData.getLastBuiltRevision() != null && !buildData.getLastBuiltRevision().getBranches().isEmpty()) {
                    branch = ((Branch) buildData.getLastBuiltRevision().getBranches().toArray()[0]).getName();
                }
                result = dtoFactory.newDTO(SCMRepository.class)
                        .setType(SCMType.GIT)
                        .setUrl(url)
                        .setBranch(branch);
            } else {
                logger.warn("failed to obtain BuildData; no SCM repository info will be available");
            }
        }
        return result;
    }

    private List<SCMCommit> extractCommits(List<ChangeLogSet<? extends ChangeLogSet.Entry>> changes) {
        List<SCMCommit> commits = new LinkedList<>();
        for (ChangeLogSet<? extends ChangeLogSet.Entry> set : changes) {
            for (ChangeLogSet.Entry change : set) {
                if (change instanceof GitChangeSet) {
                    GitChangeSet commit = (GitChangeSet) change;
                    User user = commit.getAuthor();
                    String userEmail = null;

                    List<SCMChange> tmpChanges = new ArrayList<>();
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

                    commits.add(tmpCommit);
                }
            }
        }
        return commits;
    }
}
