package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;
import com.hp.application.automation.tools.common.result.ResultSerializer;
import com.hp.application.automation.tools.common.result.model.junit.Testcase;
import com.hp.application.automation.tools.common.result.model.junit.Testsuite;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.DirectoryZipHelper;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by dsinelnikov on 7/31/2015.
 */
public final class TestResultHelper
{
    private static final String TEST_REPORT_FILE_PATTERNS = "*.xml";
    private static final String TEST_STATUS_FAIL = "fail";
    public static final String HP_UFT_PREFIX = "HP_UFT_Build_";
    public enum ResultTypeFilter {All, FAILED }

    private TestResultHelper()
    {

    }

    public static void CollateResults(@NotNull final TestCollationService testCollationService,@NotNull final TaskContext taskContext)
    {
        testCollationService.collateTestResults(taskContext, TEST_REPORT_FILE_PATTERNS, new XmlTestResultsReportCollector());
    }

    static String getOutputFilePath(TaskContext taskContext)
    {
        StringBuilder fileName = new StringBuilder(taskContext.getWorkingDirectory().toString());
        String taskName = taskContext.getConfigurationMap().get(CommonTaskConfigurationProperties.TASK_NAME);
        fileName.append("\\").append(HP_UFT_PREFIX).append(taskContext.getBuildContext().getBuildNumber())
                .append("\\").append(String.format("%03d", taskContext.getId())).append("_")
                .append(taskName).append("\\");
        return fileName.toString();
    }
}
