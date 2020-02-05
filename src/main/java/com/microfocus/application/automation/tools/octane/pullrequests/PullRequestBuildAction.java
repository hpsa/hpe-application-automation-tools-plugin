package com.microfocus.application.automation.tools.octane.pullrequests;

import com.hp.octane.integrations.dto.scm.PullRequest;
import com.hp.octane.integrations.dto.scm.SCMCommit;
import com.microfocus.application.automation.tools.octane.Messages;
import hudson.model.Action;
import hudson.model.Run;

import javax.annotation.CheckForNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PullRequestBuildAction implements Action {

    private final Run<?, ?> build;

    private final List<PullRequest> pullRequests;
    private final long minUpdateTime;
    private final String sourceBranchFilter;
    private final String targetBranchFilter;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private SimpleDateFormat updatedDateFormat = null;

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    public PullRequestBuildAction(final Run<?, ?> build, List<PullRequest> pullRequests, long minUpdateTime,
                                  String sourceBranchFilter, String targetBranchFilter) {
        this.build = build;
        this.pullRequests = pullRequests;
        this.minUpdateTime = minUpdateTime;
        this.sourceBranchFilter = sourceBranchFilter;
        this.targetBranchFilter = targetBranchFilter;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.PullRequestActionConfigurationLabel();
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "pull-request-report";
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    @SuppressWarnings("squid:S1452")
    public final Run<?, ?> getBuild() {
        return build;
    }

    public String getFormattedDate(long longTime) {
       if (updatedDateFormat == null) {
            updatedDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            TimeZone utc = TimeZone.getTimeZone("UTC");
            updatedDateFormat.setTimeZone(utc);
        }
        return updatedDateFormat.format(new Date(longTime));
    }

    public String getTopCommits(PullRequest p) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        int max_detailed_count = 10;
        int max_commit_message_size = 50;
        for (SCMCommit commit : p.getCommits()) {
            sb.append(commit.getComment(), 0, Math.min(max_commit_message_size, commit.getComment().length()));
            if (commit.getComment().length() > max_commit_message_size) {
                sb.append("...");
            }
            sb.append("\n");
            counter++;
            if (counter >= max_detailed_count) {
                sb.append("And other " + (p.getCommits().size() - max_detailed_count) + " commits");
                break;
            }
        }
        return sb.toString();
    }

    public long getMinUpdateTime() {
        return minUpdateTime;
    }

    public String getSourceBranchFilter() {
        return sourceBranchFilter;
    }

    public String getTargetBranchFilter() {
        return targetBranchFilter;
    }
}
