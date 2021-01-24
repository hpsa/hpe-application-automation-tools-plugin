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

package com.microfocus.application.automation.tools.octane.branches;

import com.hp.octane.integrations.services.pullrequestsandbranches.BranchSyncResult;
import com.microfocus.application.automation.tools.octane.Messages;
import hudson.model.Action;
import hudson.model.Run;

import javax.annotation.CheckForNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BranchesBuildAction implements Action {

    private final Run<?, ?> build;

    private final BranchSyncResult branchSyncResult;
    private final String filter;
    private final String repositoryUrl;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private SimpleDateFormat updatedDateFormat = null;

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    public BranchesBuildAction(final Run<?, ?> build, BranchSyncResult branchSyncResult, String repositoryUrl, String filter) {
        this.build = build;
        this.branchSyncResult = branchSyncResult;
        this.filter = filter;
        this.repositoryUrl = repositoryUrl;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.BranchActionConfigurationLabel();
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "branch-report";
    }

    public BranchSyncResult getBranchSyncResult () {
        return branchSyncResult;
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

    public String getFilter() {
        return filter;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }
}
