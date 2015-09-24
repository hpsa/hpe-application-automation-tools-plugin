/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import com.atlassian.util.concurrent.NotNull;
import com.hpe.application.automation.tools.common.StringUtils;

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
