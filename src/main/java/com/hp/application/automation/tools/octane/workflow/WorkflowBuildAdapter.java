package com.hp.application.automation.tools.octane.workflow;

import hudson.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import hudson.model.Job;

import java.io.File;
import java.io.IOException;


/**
 * Created by lazara on 31/01/2017.
 */
public class WorkflowBuildAdapter extends Run {

    private WorkflowRun run;
    private FilePath workspace;
    protected WorkflowBuildAdapter(@Nonnull Job job, WorkflowRun run,FilePath workspace) throws IOException {
        super(job);
        this.run = run;
        this.workspace =workspace;
    }

    @Override
    public Job getParent() {
        return run.getParent();
    }

    @Override
    public String getId(){
        return run.getId();
    }

    @Override
    public int getNumber() {
        return run.getNumber();
    }

    @Override
    public Action getAction(Class type){
        return run.getAction(type);
    }
    @Nonnull
    @Override
    public File getRootDir() {
        return run.getRootDir();
    }

    @Override
    public Executor getExecutor() {
        return run.getExecutor();
    }

    public FilePath getWorkspace(){
        return workspace;
    }
//    build.getRootDir()
//            build.getParent()
//            build.getResult()
//            build.getNumber()
//            BuildHandlerUtils.getProjectFullName(build)
//    build.setResult
}
