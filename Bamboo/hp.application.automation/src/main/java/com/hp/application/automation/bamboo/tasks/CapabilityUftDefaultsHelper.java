package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.v2.build.agent.capability.AbstractFileCapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import com.atlassian.util.concurrent.NotNull;
import com.hp.application.automation.tools.common.SSEException;

import java.io.File;

/**
 * Created by schernikov on 7/29/2015.
 */
public class CapabilityUftDefaultsHelper implements CapabilityDefaultsHelper {

    @NotNull
    @Override
    public CapabilitySet addDefaultCapabilities(CapabilitySet capabilitySet) {
        if(isUftInstalled()) {
            CapabilityImpl capability = new CapabilityImpl("UFT", "installed");
            capabilitySet.addCapability(capability);
        }
        return capabilitySet;
    }

    private boolean isUftInstalled() {
        SSEException e;
        String installPath = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Mercury Interactive\\QuickTest Professional\\CurrentVersion", "QuickTest Professional");
        File f = new File(installPath);
        return f.exists() && f.isDirectory();
    }
}
