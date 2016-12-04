package com.hp.octane.plugins.jenkins.model.processors.projects;

import com.tikal.jenkins.plugins.multijob.MultiJobProject;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */

public class MultiJobProjectProcessor extends AbstractProjectProcessor {


    public MultiJobProjectProcessor(Job job) {
        super(job);
        //  Internal phases
        //
        super.processBuilders(((MultiJobProject) this.job).getBuilders(), (MultiJobProject)this.job);

        //  Post build phases
        //
        super.processPublishers((MultiJobProject)this.job);
    }

    @Override
    public List<Builder> tryGetBuilders() {
        return ((MultiJobProject) job).getBuilders();
    }
}
