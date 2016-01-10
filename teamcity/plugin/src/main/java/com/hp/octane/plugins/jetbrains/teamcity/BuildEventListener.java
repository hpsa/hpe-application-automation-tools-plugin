package com.hp.octane.plugins.jetbrains.teamcity;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jetbrains.annotations.NotNull;

/**
 * Created by lazara on 07/01/2016.
 */
public class BuildEventListener extends BuildServerAdapter {

    @Override
    public void buildTypeAddedToQueue(@NotNull SBuildType buildType) {
    }
    @Override
    public void buildTypeAddedToQueue(@NotNull SQueuedBuild queuedBuild) {
    }
    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
    }


}
