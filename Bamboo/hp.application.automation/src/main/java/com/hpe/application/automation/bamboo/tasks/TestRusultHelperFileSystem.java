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

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.util.concurrent.NotNull;
import com.hpe.application.automation.tools.common.result.ResultSerializer;
import com.hpe.application.automation.tools.common.result.model.junit.Testcase;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuite;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuites;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.hpe.application.automation.bamboo.tasks.TestResultHelper.getOutputFilePath;

public final class TestRusultHelperFileSystem {
    public enum ResultTypeFilter {All, FAILED }
    private static final String TEST_STATUS_FAIL = "fail";

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

            Map<String, Integer> testNames = new HashMap<String, Integer>();

            for (Testsuite testsuite : testsuites.getTestsuite())
            {
                for (Testcase testcase : testsuite.getTestcase())
                {
                    if(isInFilter(testcase, filter))
                    {
                        String testName = getTestName(new File(testcase.getName()));
                        if(!testNames.containsKey(testName)){
                            testNames.put(testName, 0);
                        }
                        testNames.put(testName, testNames.get(testName) + 1);

                        StringBuilder fileNameSuffix = new StringBuilder();
                        Integer fileNameEntriesAmount = testNames.get(testName);
                        if(fileNameEntriesAmount>1){
                            fileNameSuffix.append("_").append(fileNameEntriesAmount);
                        }
                        File reportDir = new File(testcase.getReport());
                        File zipFileFolder = new File(getOutputFilePath(taskContext));
                        zipFileFolder.mkdirs();
                        File reportZipFile = new File(zipFileFolder, testName + fileNameSuffix + ".zip");
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

        if(filter == ResultTypeFilter.FAILED && status.equals(TEST_STATUS_FAIL))
        {
            return true;
        }

        return false;
    }

    /*public static void zipResults(Collection<ResultInfoItem> resultInfoItems, BuildLogger logger)
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
    }*/
}
