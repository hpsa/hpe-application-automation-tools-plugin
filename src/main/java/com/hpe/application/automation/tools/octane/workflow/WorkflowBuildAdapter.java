/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.workflow;

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
        super(job, run.getTimestamp().getTimeInMillis());
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
