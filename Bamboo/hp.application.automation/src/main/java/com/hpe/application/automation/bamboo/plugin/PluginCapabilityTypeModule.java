package com.hpe.application.automation.bamboo.plugin;

import com.atlassian.bamboo.v2.build.agent.capability.AbstractCapabilityTypeModule;
import com.atlassian.bamboo.v2.build.agent.capability.Capability;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by schernikov on 7/30/2015.
 */
public class PluginCapabilityTypeModule extends AbstractCapabilityTypeModule {
    @NotNull
    @Override
    public Map<String, String> validate(Map<String, String[]> map) {
        return null;
    }

    @NotNull
    @Override
    public Capability getCapability(@NotNull Map<String, String[]> map) {
        CapabilityImpl uftCapability = new CapabilityImpl("UFT", "installed");
        return uftCapability;
    }

    @NotNull
    @Override
    public String getLabel(@NotNull String s) {
        return null;
    }
}
