package com.hp.octane.plugins.jenkins.actions.cucumber;

import com.hp.octane.plugins.jenkins.Messages;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.File;

/**
 * Created by franksha on 07/12/2016.
 */
public class CucumberTestResultsAction implements Action {
    private final String glob;
    private final AbstractBuild build;

    CucumberTestResultsAction(AbstractBuild build, String glob, BuildListener listener) {
        this.build = build;
        this.glob = glob;
        CucumberResultsService.setListener(listener);
    }

    public boolean copyResultsToBuildFolder() {
        try {
            CucumberResultsService.log(Messages.CucumberResultsActionCollecting());
            String[] files = CucumberResultsService.getCucumberResultFiles(build.getWorkspace(), glob);
            boolean found = files.length > 0;

            for (String fileName : files) {
                File resultFile = new File(build.getWorkspace().child(fileName).toURI());
                CucumberResultsService.copyResultFile(resultFile, build.getRootDir(), build.getWorkspace());
            }

            if (!found && build.getResult() != Result.FAILURE) {
                // most likely a configuration error in the job - e.g. false pattern to match the cucumber result files
                CucumberResultsService.log(Messages.CucumberResultsActionNotFound());
            }  // else , if results weren't found but build result is failure - most likely a build failed before us. don't report confusing error message.

            return found;

        } catch (Exception e) {
            CucumberResultsService.log(Messages.CucumberResultsActionError(), e.toString());
            return false;
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
