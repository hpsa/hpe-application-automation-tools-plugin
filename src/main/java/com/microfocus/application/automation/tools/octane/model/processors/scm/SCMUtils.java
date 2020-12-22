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

package com.microfocus.application.automation.tools.octane.model.processors.scm;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.scm.SCMData;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.FilePath;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.SCM;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SCMUtils {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(SCMUtils.class);

    private static final String SCM_DATA_FILE = "scmdata.json";
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();

    private SCMUtils() {
        //code climate : Add a private constructor to hide the implicit public one
    }

    public static SCMData extractSCMData(Run run, SCM scm, SCMProcessor scmProcessor) {
        SCMData result = null;
        if (run.getParent() instanceof MatrixConfiguration || run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;
            if (!build.getChangeSet().isEmptySet()) {
                result = scmProcessor.getSCMData(build, scm);
            }
        } else if (run instanceof WorkflowRun) {
            WorkflowRun wRun = (WorkflowRun) run;
            if (!wRun.getChangeSets().isEmpty()) {
                result = scmProcessor.getSCMData(wRun, scm);
            }
        }
        return result;
    }

    public static void persistSCMData(Run run, String jobCiId, String buildCiId, SCMData scmData) throws IOException, InterruptedException {
        FilePath resultFile = new FilePath(run.getRootDir()).child(SCM_DATA_FILE);
        List<SCMData> scmDataList = new ArrayList<>();
        scmDataList.add(scmData);
        String scmDataContent = dtoFactory.dtoCollectionToJson(scmDataList);

        try {
            resultFile.write(scmDataContent, "UTF-8");
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to persist SCMData for jobCiId " + jobCiId + " buildCiId " + buildCiId, e);
            throw e;
        }
    }

    public static InputStream getSCMData(Run run) throws IOException, InterruptedException {
        FilePath resultFile = new FilePath(run.getRootDir()).child(SCM_DATA_FILE);
        return resultFile.read();
    }

}
