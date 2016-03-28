package com.hp.octane.plugins.jenkins.tests.gherkin;

import com.hp.octane.plugins.jenkins.tests.CustomTestResult;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.TestResultsExcluder;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by franksha on 20/03/2016.
 */
public class GherkinTestResultsCollector implements TestResultsExcluder {
  public static final String GHERKIN_NGA_RESULTS_XML_ = "gherkinNGAResults.xml_";
  public static final String GHERKIN_NGA_RESULTS = "gherkinNGAResults";

  private Map<String, List<String>> gherkinTestsByFeature;
  private Map<String, List<String>> gherkinTestsByScenario;
  private List<CustomTestResult> testResults;

  //force using the parametrized c-tor
  private GherkinTestResultsCollector() {}

  public GherkinTestResultsCollector(AbstractBuild build) throws InterruptedException, ParserConfigurationException, TransformerException, SAXException, IOException {
    gherkinTestsByFeature = new HashMap<String, List<String>>();
    gherkinTestsByScenario = new HashMap<String, List<String>>();
    testResults = collectGherkinTestsResults(build);
  }

  @Override
  public boolean shouldExclude(TestResult testResult) {
    String className = testResult.getClassName();
    String testName = testResult.getTestName();
    if(gherkinTestsByFeature.containsKey(className)) {
      if(gherkinTestsByFeature.get(className).contains(testName)) {
        return true;
      }
    } else if(gherkinTestsByScenario.containsKey(className)) {
      if(gherkinTestsByScenario.get(className).contains(testName)) {
        return true;
      }
    }
    return false;
  }

  public List<CustomTestResult> getGherkinTestsResults() {
    return testResults;
  }

  private List<CustomTestResult> collectGherkinTestsResults(AbstractBuild build) throws ParserConfigurationException, IOException, InterruptedException, SAXException, TransformerException {
    List<CustomTestResult> result = new ArrayList<CustomTestResult>();

    //Retrieve the gherkin results xml
    int i=0;
    File buildDir = build.getRootDir();
    FilePath gherkinTestResultsFilePath = new FilePath(buildDir).child(GHERKIN_NGA_RESULTS + i + ".xml");

    while(gherkinTestResultsFilePath.exists()) {
      String gherkinTestResultsPath = buildDir.getAbsolutePath() + File.separator + GHERKIN_NGA_RESULTS + i + ".xml";

      //parse the xml
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(gherkinTestResultsPath);
      doc.getDocumentElement().normalize();

      //Go over the features
      NodeList featureNodes = doc.getElementsByTagName("feature");
      for(int f=0; f<featureNodes.getLength(); f++) {
        Element featureElement = (Element)featureNodes.item(f);
        String featureName = featureElement.getAttribute("name");
        List<String> scenarioNames = new ArrayList<String>();
        TestResultStatus featureStatus = TestResultStatus.PASSED;
        boolean featureStatusDetermined = false;
        long featureDuration = 0;

        //Go over the scenarios
        NodeList scenarioNodes = featureElement.getElementsByTagName("scenario");
        for(int s=0; s<scenarioNodes.getLength(); s++) {
          Element scenarioElement = (Element)scenarioNodes.item(s);
          String scenarioName = scenarioElement.getAttribute("name");
          scenarioNames.add(scenarioName);
          TestResultStatus scenarioStatus = TestResultStatus.PASSED;
          boolean scenarioStatusDetermined = false;
          long scenarioDuration = 0;

          //Go over the steps
          NodeList stepNodes = scenarioElement.getElementsByTagName("step");
          List<String> stepNames = new ArrayList<String>();
          for(int t=0; t<stepNodes.getLength(); t++) {
            Element stepElement = (Element)stepNodes.item(t);
            String stepName = stepElement.getAttribute("name");
            stepNames.add(stepName);

            long stepDuration = Long.parseLong(stepElement.getAttribute("duration"));
            scenarioDuration += stepDuration;

            String stepStatus = stepElement.getAttribute("status");
            if(!scenarioStatusDetermined && "pending".equals(stepStatus)) {
              scenarioStatus = TestResultStatus.SKIPPED;
              scenarioStatusDetermined = true;
            } else if(!scenarioStatusDetermined && "failed".equals(stepStatus)) {
              scenarioStatus = TestResultStatus.FAILED;
              scenarioStatusDetermined = true;
            }
          }

          featureDuration += scenarioDuration;
          if(!featureStatusDetermined && TestResultStatus.SKIPPED.equals(scenarioStatus)) {
            featureStatus = TestResultStatus.SKIPPED;
            featureStatusDetermined = true;
          } else if(!featureStatusDetermined && TestResultStatus.FAILED.equals(scenarioStatus)) {
            featureStatus = TestResultStatus.FAILED;
            featureStatusDetermined = true;
          }

          scenarioElement.setAttribute("status", scenarioStatus.toPrettyName());

          //for surefire report
          stepNames.add(scenarioName);
          stepNames.add("Scenario: " + scenarioName);
          gherkinTestsByScenario.put(scenarioName, stepNames);
          gherkinTestsByScenario.put("Scenario: " + scenarioName, stepNames);
        }

        gherkinTestsByFeature.put(featureName, scenarioNames);
        result.add(new GherkinTestResult(featureName, featureElement, featureDuration, featureStatus));
      }

      i++;
      gherkinTestResultsFilePath = new FilePath(buildDir).child(GHERKIN_NGA_RESULTS + i + ".xml");
    } //end while

    return result;
  }
}
