package com.hp.octane.plugins.jetbrains.teamcity;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;


/**
 * Created by lazara on 07/01/2016.
 */
public class BuildEventListener extends BuildServerAdapter {

    private static Logger LOG = Logger.getLogger(BuildEventListener.class.getName());

    @Override
    public void buildTypeAddedToQueue(@NotNull SBuildType buildType) {
        String name = buildType.getName();
        LOG.warning("add to queue: "+name);
    }
    @Override
    public void buildTypeAddedToQueue(@NotNull SQueuedBuild queuedBuild) {
        String name =   queuedBuild.getBuildType().getName();
        LOG.warning("add to queue: "+name);

    }
    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        String name = build.getBuildTypeName();
        LOG.warning("build started: "+name);

    }

    @Override
    public void beforeBuildFinish(@NotNull SRunningBuild runningBuild) {
        String name = runningBuild.getBuildTypeName();
        LOG.warning("before build finish: "+name);

    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        String name = build.getBuildTypeName();
        LOG.warning(" build finished: "+name);
    }


}
