package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import com.atlassian.util.concurrent.NotNull;
import com.hp.application.automation.tools.common.StringUtils;

import java.io.File;

/**
 * Created by schernikov on 7/29/2015.
 */
public class CapabilityUftDefaultsHelper implements CapabilityDefaultsHelper {

    public static final String CAPABILITY_HP_ROOT = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".HP";
    public static final String CAPABILITY_UFT = CAPABILITY_HP_ROOT + ".HP Unified Functional Testing";

    @NotNull
    @Override
    public CapabilitySet addDefaultCapabilities(CapabilitySet capabilitySet) {
        String uftPath = getUftExePath();
        if(!StringUtils.isNullOrEmpty(uftPath)) {
            CapabilityImpl capability = new CapabilityImpl(CAPABILITY_UFT, uftPath);
            capabilitySet.addCapability(capability);
        }
        else
        {
            capabilitySet.removeCapability(CAPABILITY_UFT);
        }
        return capabilitySet;
    }

    private static final String uftRegistryKey = "SOFTWARE\\Mercury Interactive\\QuickTest Professional\\CurrentVersion";
    private static final String uftRegistryValue = "QuickTest Professional";
    private static final String uftExeName = "bin\\UFT.exe";
    private static String getUftExePath()
    {
        String installPath = WindowsRegistry.readHKLMString(uftRegistryKey, uftRegistryValue);
        if(StringUtils.isNullOrEmpty(installPath))
        {
            return "";
        }
        File f = new File(installPath);
        if(f.exists() && f.isDirectory())
        {
            f = new File(f, uftExeName);
            if(f.exists() && f.isFile())
            {
                return f.getAbsolutePath();
            }
        }
        return "";
    }
}
