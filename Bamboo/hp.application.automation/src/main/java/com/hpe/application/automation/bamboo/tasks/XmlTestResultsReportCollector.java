package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import com.atlassian.bamboo.build.test.TestCollectionResultBuilder;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.tests.TestState;
import com.google.common.collect.Sets;
import com.hpe.application.automation.tools.common.result.ResultSerializer;
import com.hpe.application.automation.tools.common.result.model.junit.JUnitTestCaseStatus;
import com.hpe.application.automation.tools.common.result.model.junit.Testcase;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuite;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuites;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by dsinelnikov on 7/31/2015.
 */
public class XmlTestResultsReportCollector implements TestReportCollector {
    @NotNull
    @Override
    public TestCollectionResult collect(File file) throws Exception {

        TestCollectionResultBuilder builder = new TestCollectionResultBuilder();

        Collection<TestResults> successfulTestResults = new ArrayList<TestResults>();
        Collection<TestResults> failingTestResults = new ArrayList<TestResults>();

        Testsuites testsuites = ResultSerializer.Deserialize(file);

        for (Testsuite testsuite : testsuites.getTestsuite())
        {
            for (Testcase testcase : testsuite.getTestcase())
            {
                TestResults testResult = new TestResults(testcase.getClassname(), testcase.getName(), testcase.getTime());
                if(testcase.getStatus().equals(JUnitTestCaseStatus.PASS))
                {
                    testResult.setState(TestState.SUCCESS);
                    successfulTestResults.add(testResult);
                }
                else
                {
                    testResult.setState(TestState.FAILED);
                    failingTestResults.add(testResult);
                }
            }
        }

        return builder
                .addSuccessfulTestResults(successfulTestResults)
                .addFailedTestResults(failingTestResults)
                .build();
    }

    @NotNull
    @Override
    public Set<String> getSupportedFileExtensions() {
        return Sets.newHashSet("xml");
    }
}
