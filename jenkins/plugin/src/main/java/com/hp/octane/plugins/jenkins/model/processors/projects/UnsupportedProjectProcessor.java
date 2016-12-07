package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */

 class UnsupportedProjectProcessor extends AbstractProjectProcessor {
     UnsupportedProjectProcessor(Job job) {
        super(job);
    }

    @Override
    public List<Builder> tryGetBuilders() {
        return new ArrayList<>();
    }
}