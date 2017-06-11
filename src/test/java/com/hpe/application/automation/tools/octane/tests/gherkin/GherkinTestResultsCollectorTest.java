/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.gherkin;

import com.hpe.application.automation.tools.octane.tests.junit.TestResultStatus;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GherkinTestResultsCollectorTest {
    String defaultResourceRelativePath = "f1";
    String defaultResourceName = "OctaneGherkinResults0.xml";

    private String getDefaultRootResourceFolder(){
        return getRootResourceFolder(defaultResourceRelativePath,defaultResourceName);
    }

    private String getRootResourceFolder(String resourceRelativePath,String resourceName){
        String resource;
        if(resourceRelativePath.isEmpty()){
            resource = resourceName;
        } else {
            resource = resourceRelativePath + "/" + resourceName;
        }
        URL url = getClass().getResource(resource);
        String path = url.getPath();
        return path.substring(0,path.lastIndexOf(resourceName)-1);
    }

    @Test
    public void testConstruct() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getDefaultRootResourceFolder()));
    }

    @Test
    public void testGetResults() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        List<TestResult> gherkinTestsResults = GherkinTestResultsCollector.collectGherkinTestsResults(new File(getDefaultRootResourceFolder()));
        Assert.assertEquals(3,gherkinTestsResults.size());
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(0),"test Feature1",21, TestResultStatus.FAILED);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(1),"test Feature10",21,TestResultStatus.FAILED);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(2),"test Feature2",21,TestResultStatus.PASSED);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testXmlHasNoVersion() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getRootResourceFolder("f2",defaultResourceName)));
    }

    @Test (expected=IllegalArgumentException.class)
    public void testXmlHasHigherVersion() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getRootResourceFolder("f3",defaultResourceName)));
    }

    private void validateGherkinTestResult(GherkinTestResult gherkinTestResult, String name, long duration, TestResultStatus status){
        validateAttributes(gherkinTestResult, name, duration, status);
        Assert.assertNotNull(gherkinTestResult.getXmlElement());
    }

    private void validateAttributes(GherkinTestResult gherkinTestResult, String name, long duration, TestResultStatus status){
        Map<String, String> attributes = gherkinTestResult.getAttributes();
        Assert.assertEquals(name,attributes.get("name"));
        Assert.assertEquals(String.valueOf(duration),attributes.get("duration"));
        Assert.assertEquals(status.toPrettyName(),attributes.get("status"));
    }
}
