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
    private GherkinTestResultsCollector() {
    }

    public GherkinTestResultsCollector(File buildDir) throws InterruptedException, ParserConfigurationException, TransformerException, SAXException, IOException {
        gherkinTestsByFeature = new HashMap<String, List<String>>();
        gherkinTestsByScenario = new HashMap<String, List<String>>();
        testResults = collectGherkinTestsResults(buildDir);
    }

    @Override
    public boolean shouldExclude(TestResult testResult) {
        String className = testResult.getClassName();
        String testName = testResult.getTestName();
        if (gherkinTestsByFeature.containsKey(className)) {
            if (gherkinTestsByFeature.get(className).contains(testName)) {
                return true;
            }
        } else if (gherkinTestsByScenario.containsKey(className)) {
            if (gherkinTestsByScenario.get(className).contains(testName)) {
                return true;
            }
        }
        return false;
    }

    public List<CustomTestResult> getGherkinTestsResults() {
        return testResults;
    }

    private List<CustomTestResult> collectGherkinTestsResults(File buildDir) throws ParserConfigurationException, IOException, InterruptedException, SAXException, TransformerException {
        List<CustomTestResult> result = new ArrayList<CustomTestResult>();

        //Retrieve the gherkin results xml
        int i = 0;
        FilePath gherkinTestResultsFilePath = new FilePath(buildDir).child(GHERKIN_NGA_RESULTS + i + ".xml");

        while (gherkinTestResultsFilePath.exists()) {
            String gherkinTestResultsPath = buildDir.getAbsolutePath() + File.separator + GHERKIN_NGA_RESULTS + i + ".xml";

            //parse the xml
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(gherkinTestResultsPath);
            doc.getDocumentElement().normalize();

            //Go over the features
            NodeList featureNodes = doc.getElementsByTagName("feature");
            for (int f = 0; f < featureNodes.getLength(); f++) {
                Element featureElement = (Element) featureNodes.item(f);
                String featureName = featureElement.getAttribute("name");
                List<String> scenarioNames = new ArrayList<String>();
                TestResultStatus featureStatus = TestResultStatus.PASSED;
                boolean featureStatusDetermined = false;
                long featureDuration = 0;
                NodeList backgroundNodes = featureElement.getElementsByTagName("background");
                Element backgroundElement = backgroundNodes.getLength() > 0 ? (Element)backgroundNodes.item(0) : null;
                NodeList backgroundSteps = backgroundElement != null ? backgroundElement.getElementsByTagName("step") : null;

                //Go over the scenarios
                NodeList scenarioNodes = featureElement.getElementsByTagName("scenario");
                for (int s = 0; s < scenarioNodes.getLength(); s++) {
                    Element scenarioElement = (Element) scenarioNodes.item(s);
                    String scenarioName = scenarioElement.getAttribute("name");
                    if (scenarioElement.hasAttribute("outlineIndex")) {
                        String outlineIndexStr = scenarioElement.getAttribute("outlineIndex");
                        if (outlineIndexStr != null && !outlineIndexStr.isEmpty()) {
                            Integer outlineIndex = Integer.valueOf(scenarioElement.getAttribute("outlineIndex"));
                            if (outlineIndex > 1) {
                                //we add the index only from 2 and upwards seeing as that is the naming convention in junit xml.
                                String delimiter = " ";
                                if (!scenarioName.contains(" ")) {
                                    //we need to use the same logic as used in the junit report
                                    delimiter = "_";
                                }
                                scenarioName = scenarioName + delimiter + scenarioElement.getAttribute("outlineIndex");
                            }
                        }
                    }
                    scenarioNames.add(scenarioName);
                    TestResultStatus scenarioStatus = TestResultStatus.PASSED;
                    boolean scenarioStatusDetermined = false;
                    long scenarioDuration = 0;
                    List<Element> stepElements = new ArrayList<Element>();
                    List<String> stepNames = new ArrayList<String>();

                    if(backgroundSteps != null) {
                        for (int bs = 0; bs < backgroundSteps.getLength(); bs++) {
                            Element stepElement = (Element) backgroundSteps.item(bs);
                            stepElements.add(stepElement);
                        }
                    }
                    NodeList stepNodes = scenarioElement.getElementsByTagName("step");
                    for (int sn = 0; sn < stepNodes.getLength(); sn++) {
                        Element stepElement = (Element) stepNodes.item(sn);
                        stepElements.add(stepElement);
                    }

                    //Go over the steps
                    for (Element stepElement : stepElements) {
                        String stepName = stepElement.getAttribute("name");
                        stepNames.add(stepName);

                        String duration = stepElement.getAttribute("duration");
                        long stepDuration = duration != "" ? Long.parseLong(duration) : 0;
                        scenarioDuration += stepDuration;

                        String stepStatus = stepElement.getAttribute("status");
                        if (!scenarioStatusDetermined && "pending".equals(stepStatus)) {
                            scenarioStatus = TestResultStatus.SKIPPED;
                            scenarioStatusDetermined = true;
                        } else if (!scenarioStatusDetermined && "failed".equals(stepStatus)) {
                            scenarioStatus = TestResultStatus.FAILED;
                            scenarioStatusDetermined = true;
                        }
                    }

                    featureDuration += scenarioDuration;
                    if (!featureStatusDetermined && TestResultStatus.SKIPPED.equals(scenarioStatus)) {
                        featureStatus = TestResultStatus.SKIPPED;
                        featureStatusDetermined = true;
                    } else if (!featureStatusDetermined && TestResultStatus.FAILED.equals(scenarioStatus)) {
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
