package com.hp.octane.plugins.jenkins.tests.gherkin;

import com.hp.octane.plugins.jenkins.tests.CustomTestResult;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class GherkinTestResultsCollectorTest {

    String root = "";

    @Before
    public void init(){
        String resourceName = "gherkinNGAResults0.xml";
        URL url = getClass().getResource(resourceName);
        String path = url.getPath();
        root =  path.substring(0,path.lastIndexOf(resourceName)-1);
    }

    @Test
    public void testConstruct() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        new GherkinTestResultsCollector(new File(root));
    }

    @Test
    public void testShouldExclude() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector gherkinTestResultsCollector =  new GherkinTestResultsCollector(new File(root));
        Assert.assertFalse(gherkinTestResultsCollector.shouldExclude(new TestResult("","","Class1","test6",null,(long)0,(long)0,null,"")));
        Assert.assertTrue(gherkinTestResultsCollector.shouldExclude(new TestResult("","","test Feature1","test scenario2",null,(long)0,(long)0,null,"")));
        Assert.assertTrue(gherkinTestResultsCollector.shouldExclude(new TestResult("","","test Feature1","test scenario3",null,(long)0,(long)0,null,"")));
        Assert.assertTrue(gherkinTestResultsCollector.shouldExclude(new TestResult("","","test Feature2","test scenario4",null,(long)0,(long)0,null,"")));
        Assert.assertTrue(gherkinTestResultsCollector.shouldExclude(new TestResult("","","test Feature2","test scenario5",null,(long)0,(long)0,null,"")));
    }

    @Test
    public void testGetResults() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector gherkinTestResultsCollector =  new GherkinTestResultsCollector(new File(root));
        ArrayList<CustomTestResult> gherkinTestsResults = (ArrayList<CustomTestResult>) gherkinTestResultsCollector.getGherkinTestsResults();
        Assert.assertEquals(gherkinTestsResults.size(),2);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(0),"test Feature1",21,TestResultStatus.FAILED);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(1),"test Feature2",21,TestResultStatus.PASSED);
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
