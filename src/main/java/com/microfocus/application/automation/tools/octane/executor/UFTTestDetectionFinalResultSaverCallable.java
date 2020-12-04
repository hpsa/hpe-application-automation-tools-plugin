/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.executor;

import com.hp.octane.integrations.uft.items.UftTestDiscoveryResult;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;

/**
 * used to save final UFTTestDetection results in slave machine workspace
 */
public class UFTTestDetectionFinalResultSaverCallable extends MasterToSlaveFileCallable<String> {
    private UftTestDiscoveryResult results;
    private int buildNumber;

    private static final Logger logger = SDKBasedLoggerProvider.getLogger(UFTTestDetectionFinalResultSaverCallable.class);

    public UFTTestDetectionFinalResultSaverCallable(UftTestDiscoveryResult results, int buildNumber) {
        this.results = results;
        this.buildNumber = buildNumber;
    }

    @Override
    public String invoke(File file, VirtualChannel virtualChannel) {
        //publish final results
        File subWorkspace = new File(file, "_Final_Detection_Results");
        try {
            if (!subWorkspace.exists()) {
                subWorkspace.mkdirs();
            }
            File reportXmlFile = new File(subWorkspace, "final_detection_result_build_" + buildNumber + ".json");
            results.writeToFile(reportXmlFile);
        } catch (IOException e) {
            logger.error("Failed to write final_detection_result file :" + e.getMessage());
        }

        return null;
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        //no need to check roles as this can be run on master and on slave
    }
}
