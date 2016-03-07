package com.hp.octane.plugins.jetbrains.teamcity.tests.services;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.BuildContext;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResult;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResultContainer;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResultStatus;
import jetbrains.buildServer.serverSide.STestRun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */
public class BuildTestsService{

    static final String TEST_RESULT_FILE = "mqmTests.xml";

    //private static XmlMapper mapper = new XmlMapper();
    private static XmlMapper mapper = new XmlMapper();
    static {
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        mapper.registerModule(module);
    }

    public static boolean handleTestResult(List<STestRun> tests, File destPath, long buildStartingTime, BuildContext buildContext){

        List<TestResult> testList = new ArrayList<TestResult>();
        createTestList(tests,buildStartingTime,testList);
        TestResultContainer testResult = new TestResultContainer(testList, buildContext);
        try {
            mapper.writeValue(new File(destPath.getPath() + "\\" + TEST_RESULT_FILE), testResult);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        return true;
    }

    private static void createTestList(List<STestRun> tests, long startingTime, List<TestResult> testList){
        for (STestRun testRun: tests){
            TestResultStatus testResultStatus = null;
            if(testRun.isIgnored()) {
                testResultStatus = TestResultStatus.SKIPPED;
            }else if(testRun.getStatus().isFailed()){
                testResultStatus = TestResultStatus.FAILED;
            }else if(testRun.getStatus().isSuccessful()){
                testResultStatus = TestResultStatus.PASSED;
            }

            TestResult test = new TestResult(testRun.getBuild().getArtifactsDirectory().toString(), testRun.getTest().getName().getPackageName(),testRun.getTest().getName().getClassName(), testRun.getTest().getName().getTestMethodName() , testRun.getDuration(), testResultStatus, startingTime);
            testList.add(test);
        }
    }


}
