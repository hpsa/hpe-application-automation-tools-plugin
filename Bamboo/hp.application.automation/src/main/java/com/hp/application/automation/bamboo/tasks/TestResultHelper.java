package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.util.concurrent.NotNull;
import com.hp.application.automation.tools.common.result.ResultSerializer;
import com.hp.application.automation.tools.common.result.model.junit.Testcase;
import com.hp.application.automation.tools.common.result.model.junit.Testsuite;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.DirectoryZipHelper;
import org.apache.commons.lang.StringUtils;

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

    private static final String TEST_STATUS_PASSED = "pass";
    private static final String TEST_STATUS_FAIL = "fail";

    public enum ResultTypeFilter {All, SUCCESSFUL, FAILED }

    public static final String HP_UFT_PREFIX = "HP_UFT_Build_";

    private TestResultHelper()
    {

    }

    public static void CollateResults(@NotNull final TestCollationService testCollationService,@NotNull final TaskContext taskContext)
    {
        testCollationService.collateTestResults(taskContext, TEST_REPORT_FILE_PATTERNS, new XmlTestResultsReportCollector());
    }

    public static Collection<ResultInfoItem> getTestResults(@NotNull File results, ResultTypeFilter filter
            ,@NotNull final String resultArtifactNameFormat, @NotNull final TaskContext taskContext, @NotNull final BuildLogger logger)
    {
        Collection<ResultInfoItem> resultItems = new ArrayList<ResultInfoItem>();

        if(!results.exists())
        {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") was not found.");
            return resultItems;
        }

        try
        {
            Testsuites testsuites = ResultSerializer.Deserialize(results);

            for (Testsuite testsuite : testsuites.getTestsuite())
            {
                for (Testcase testcase : testsuite.getTestcase())
                {
                    if(isInFilter(testcase, filter))
                    {
                        String testName = getTestName(new File(testcase.getName()));
                        File reportDir = new File(testcase.getReport());
                        File zipFileFolder = new File(getZipFilePath(taskContext));
                        zipFileFolder.mkdirs();
                        File reportZipFile = new File(zipFileFolder, testName + ".zip");
                        String resultArtifactName = String.format(resultArtifactNameFormat, testName);

                        ResultInfoItem resultItem = new ResultInfoItem(testName, reportDir, reportZipFile, resultArtifactName);
                        resultItems.add(resultItem);
                    }
                }
            }
        }
        catch (JAXBException ex)
        {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") has invalid format.");
        }

        return resultItems;
    }

    private static String getZipFilePath(TaskContext taskContext)
    {
        StringBuilder fileName = new StringBuilder(taskContext.getWorkingDirectory().toString());
        String taskName = taskContext.getConfigurationMap().get(CommonTaskConfigurationProperties.TASK_NAME);
        fileName.append("\\").append(getBuildRunPath(taskContext)).append("\\")
                .append(String.format("%03d", taskContext.getId())).append(" "+taskName+"\\");
        return fileName.toString();
    }

    private static String getTestName(File test)
    {
        return test.getName();
    }

    private static boolean isInFilter(Testcase testcase, ResultTypeFilter filter)
    {
        if(filter == ResultTypeFilter.All)
        {
            return true;
        }

        String status = testcase.getStatus();

        if(filter == ResultTypeFilter.SUCCESSFUL && status.equals(TEST_STATUS_PASSED))
        {
            return true;
        }

        if(filter == ResultTypeFilter.FAILED && status.equals(TEST_STATUS_FAIL))
        {
            return true;
        }

        return false;
    }

    public static void zipResults(Collection<ResultInfoItem> resultInfoItems, BuildLogger logger)
    {
        for(ResultInfoItem resultItem : resultInfoItems)
        {
            try
            {
                DirectoryZipHelper.zipFolder(resultItem.getSourceDir().getPath(), resultItem.getZipFile().getPath());
            }
            catch (IOException ex){
                logger.addBuildLogEntry(ex.getMessage());
            } catch (Exception ex) {
                logger.addBuildLogEntry(ex.getMessage());
            }
        }
    }

    private static File getCheckoutDirectory(BuildContext buildContext) {
        Iterator<Long> repoIdIterator = buildContext.getRelevantRepositoryIds().iterator();
        if (repoIdIterator.hasNext()) {
            long repoId = repoIdIterator.next();
            String checkoutLocation = buildContext.getCheckoutLocation().get(repoId);
            if (StringUtils.isNotBlank(checkoutLocation)) {
                return new File(checkoutLocation);
            }
        }
        return null;
    }

    public static String getBuildRunPath(TaskContext taskContext){
        return HP_UFT_PREFIX + taskContext.getBuildContext().getBuildNumber();
    }

}
