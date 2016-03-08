package com.hp.octane.plugins.jetbrains.teamcity.tests.services;


import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.tests.BuildContext;
import com.hp.nga.integrations.dto.tests.TestRun;
import com.hp.nga.integrations.dto.tests.TestResult;
import com.hp.nga.integrations.dto.tests.TestRunResult;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.intellij.openapi.vfs.FilePath;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.impl.auth.SecuredRunningBuild;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */
public class BuildTestsService{
    //private static final String TEAMCITY_BUILD_CHECKOUT_DIR = "teamcity.build.checkoutDir";

    private static final DTOFactory dtoFactory = DTOFactory.getInstance();

    public static void handleTestResult(List<STestRun> tests, long buildStartingTime,  SRunningBuild build){

        BuildContext buildContext = dtoFactory.newDTO(BuildContext.class)
                .setBuildId(build.getBuildId())
                .setBuildType(build.getBuildType().getName())
                .setServer(NGAPlugin.getInstance().getConfig().getIdentity());
        TestRun [] testArr = createTestList(tests, buildStartingTime);
        TestResult result = dtoFactory.newDTO(com.hp.nga.integrations.dto.tests.TestResult.class).setTestRuns(testArr).setBuildContext(buildContext);
        String xml = dtoFactory.dtoToXml(result);

    }


//    private static void findMoudle(SRunningBuild build, String className){
//        String currPath = ((SecuredRunningBuild) build).getBuildFinishParameters ().get(TEAMCITY_BUILD_CHECKOUT_DIR);
//        File path = new File(currPath);
//        if(path.isFile() && path.isDirectory()){
//            File classFile = findFolder(path, className);
//
//        }
//    }
//
//    private static File findFolder(File root, String fileName){
//        File result = null;
//        if(root == null) return null;
//        if(root.isDirectory()) {
//            for(File file : root.listFiles()) {
//                if(file.isDirectory()) {
//                    if(result != null) {
//                        result = findFolder(file, fileName);
//                    }
//                }
//                else if(root.isFile() && file.getName().contains(fileName)) {
//                    return new File(file.getPath());
//                }
//
//            }
//        }
//        return result;
//    }
//
//    private static boolean isMavenFolder(File root){
//        if(root.isDirectory()) {
//            for(File file : root.listFiles()) {
//                if(root.isFile() && file.getName().equals("pom.xml")) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private static TestRun [] createTestList(List<STestRun> tests, long startingTime){

        List<TestRun> testList = new ArrayList<TestRun>();
        for (STestRun testRun: tests){
            TestRunResult testResultStatus = null;
            if(testRun.isIgnored()) {
                testResultStatus = TestRunResult.SKIPPED;
            }else if(testRun.getStatus().isFailed()){
                testResultStatus = TestRunResult.FAILED;
            }else if(testRun.getStatus().isSuccessful()){
                testResultStatus = TestRunResult.PASSED;
            }

            TestRun tr = dtoFactory.newDTO(TestRun.class)
                    .setModuleName("")
                    .setPackageName(testRun.getTest().getName().getPackageName())
                    .setClassName(testRun.getTest().getName().getClassName())
                    .setTestName(testRun.getTest().getName().getTestMethodName())
                    .setResult(testResultStatus)
                    .setStarted(startingTime)
                    .setDuration(testRun.getDuration());


            testList.add(tr);
        }
        TestRun [] testArr = testList.toArray(new TestRun[testList.size()]);

        return testArr;
    }


}
