package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.Maps;
import com.hp.application.automation.tools.common.result.ResultSerializer;
import com.hp.application.automation.tools.common.result.model.junit.JUnitTestCaseStatus;
import com.hp.application.automation.tools.common.result.model.junit.Testcase;
import com.hp.application.automation.tools.common.result.model.junit.Testsuite;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.DirectoryZipHelper;
import com.hp.application.automation.tools.common.sdk.Logger;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

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

    private static class ResultInfoItem
    {
        private String _testName;
        public String getTestName()
        {
            return _testName;
        }

        private String _resultName;
        public String getResultName()
        {
            return _resultName;
        }

        private File _sourceDir;
        public File getSourceDir()
        {
            return _sourceDir;
        }

        private File _zipFile;
        public File getZipFile()
        {
            return _zipFile;
        }

        public ResultInfoItem(String testName, File sourceDir, File zipFile)
        {
            _testName = testName;
            _resultName = testName + " Result";
            _sourceDir = sourceDir;
            _zipFile = zipFile;
        }
    }

    private TestResultHelper()
    {

    }

    public static void CollateResults(@NotNull final TestCollationService testCollationService,@NotNull final TaskContext taskContext)
    {
        testCollationService.collateTestResults(taskContext, TEST_REPORT_FILE_PATTERNS, new XmlTestResultsReportCollector());
    }

    public static Collection<String> getTestResultsPathes(@NotNull File results, ResultTypeFilter filter, @NotNull Logger logger)
    {
        Collection<String> resultsFolders = new ArrayList<String>();

        if(!results.exists())
        {
            logger.log("Test results file (" + results.getName() + ") was not found.");
            return resultsFolders;
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
                        resultsFolders.add(testcase.getReport());
                    }
                }
            }
        }
        catch (JAXBException ex)
        {
            logger.log("Test results file (" + results.getName() + ") has invalid format.");
        }

        return resultsFolders;
    }

    public static void publishArtifacts(@NotNull final TaskContext taskContext, final ArtifactManager artifactManager, Collection<String> reportDirectories, @NotNull BuildLogger logger)
    {
        File workingDirectory = taskContext.getWorkingDirectory();

        Collection<ResultInfoItem> resultInfoItems = getResultInfoItems(workingDirectory, reportDirectories);
        zipResults(resultInfoItems, logger);

        BuildContext buildContext = taskContext.getBuildContext();

        final PlanResultKey planResultKey = buildContext.getPlanResultKey();
        File checkoutDir = getCheckoutDirectory(buildContext);
        Map<String, String> config = Maps.newHashMap();

        SecureToken securityToken = SecureToken.create();

        for(ResultInfoItem resultItem : resultInfoItems)
        {
            ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl(securityToken);
            artifact.setName(resultItem.getResultName());
            artifact.setLocation("\\");
            artifact.setCopyPattern(resultItem.getZipFile().getName());

            buildContext.getArtifactContext().getDefinitionContexts().add(artifact);

            artifactManager.publish(logger, planResultKey, checkoutDir, artifact, config, 1);
        }
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

    private static Collection<ResultInfoItem> getResultInfoItems(File destDir, Collection<String> resultDirs)
    {
        Collection<ResultInfoItem> resultItems = new ArrayList<ResultInfoItem>();

        for(String dir : resultDirs)
        {
            File resultsDir = new File(dir);
            String testName = getTestName(resultsDir);
            String resultsArchiveName = testName + "_Result.zip";
            File zipResults = new File(destDir, resultsArchiveName);

            ResultInfoItem resultItem = new ResultInfoItem(testName, resultsDir, zipResults);
            resultItems.add(resultItem);
        }

        return resultItems;
    }

    private static String getTestName(File resultDir)
    {
        return resultDir.getParentFile().getName();
    }

    private static void zipResults(Collection<ResultInfoItem> resultInfoItems, BuildLogger logger)
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
}
