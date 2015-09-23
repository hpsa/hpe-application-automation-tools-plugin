package com.hp.application.automation.bamboo.tasks;

import com.atlassian.util.concurrent.NotNull;

public final class ResourceManager{
    public static String getText(@NotNull String resourceString)
    {
        return RunFromAlmTaskConfigurator.getResourceString(resourceString);
    }
}
