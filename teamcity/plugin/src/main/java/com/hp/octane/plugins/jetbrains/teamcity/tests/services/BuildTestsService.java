package com.hp.octane.plugins.jetbrains.teamcity.tests.services;

import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResult;
import jetbrains.buildServer.serverSide.ServerExtension;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */
public class BuildTestsService implements ServerExtension {

    public static boolean handleTestResult(String surefirePath, File destPath, long buildStartingTime){
        File checkoutFolder = new File(surefirePath);
        if(checkoutFolder.exists()){
            List<File> fileList = new ArrayList<File>();
            searchForSureFireFiles(checkoutFolder, fileList);
            if(fileList.isEmpty()){
                return false;
            }
            for(File surefireFile:fileList){
                createTestListFromFile(surefireFile, buildStartingTime);
            }
        }else{
            return false;
        }
        return true;
    }

    private static List<TestResult> createTestListFromFile(File surefireFile, long startingTime){
        List<TestResult> testList = new ArrayList<TestResult>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(surefireFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("testcase");

            for (int i = 0; i < nList.getLength(); i++) {
                String moduleName = "";
                String packageName = "";
                String className = "";
                String testName = "";
                long duration = 0;
                //TestResultStatus status;

                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    duration = Long.valueOf(eElement.getAttribute("time"));
                    moduleName = surefireFile.getParentFile().getName();
                    //new TestResult(moduleName, packageName, className, testName, status, duration, buildStarted);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return testList;
    }


    private static void searchForSureFireFiles(File root, List<File> surefireOnly) {
        if(root == null || surefireOnly == null) return;
        if(root.isDirectory()) {
            for(File file : root.listFiles()) {
                searchForSureFireFiles(file, surefireOnly);
            }
        } else if(root.isFile() && root.getPath().contains("surefire-reports") && root.getPath().endsWith(".xml")) {
            surefireOnly.add(root);
        }
    }
}
