package com.hp.octane.plugins.jetbrains.teamcity.tests.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResult;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResultContainer;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.TestResultStatus;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */
public class BuildTestsService {

	static final String TEST_RESULT_FILE = "mqmTests.xml";

	private static XmlMapper mapper = new XmlMapper();

	static {
		JaxbAnnotationModule module = new JaxbAnnotationModule();
		mapper.registerModule(module);
	}

	public static boolean handleTestResult(String surefirePath, File destPath, long buildStartingTime) {

		File checkoutFolder = new File(surefirePath);
		if (checkoutFolder.exists()) {
			List<File> fileList = new ArrayList<File>();
			searchForSureFireFiles(checkoutFolder, fileList);
			if (fileList.isEmpty()) {
				return false;
			}
			List<TestResult> testList = new ArrayList<TestResult>();
			for (File surefireFile : fileList) {
				createTestListFromFile(surefireFile, buildStartingTime, testList);
				createTestListFromFile2(surefireFile, buildStartingTime, testList);

			}
			TestResultContainer testResult = new TestResultContainer(testList);
			try {
				mapper.writeValue(new File(destPath.getPath() + File.separator + TEST_RESULT_FILE), testResult);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		} else {
			return false;
		}
		return true;
	}


	private static void createTestListFromFile(File surefireFile, long startingTime, List<TestResult> testList) {

//		try {
//			TestSuite testSuite = mapper.readValue(surefireFile, TestSuite.class);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//
//        try {
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(surefireFile);
//            doc.getDocumentElement().normalize();
//            NodeList nList = doc.getElementsByTagName("testcase");
//
//            for (int i = 0; i < nList.getLength(); i++) {
//                String moduleName = "";
//                String packageName = "";
//                String className = "";
//                String testName = "";
//                String duration = "";
//                TestResultStatus status;
//
//                Node nNode = nList.item(i);
//                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//                    Element eElement = (Element) nNode;
//                    duration = eElement.getAttribute("time");
//                    int moudleInd = surefireFile.getParentFile().getPath().lastIndexOf("\\target\\surefire-reports");
//                    int moudleInd2 = surefireFile.getParentFile().getPath().substring(0, moudleInd).lastIndexOf("\\");
//                    moduleName = surefireFile.getParentFile().getPath().substring(moudleInd2+1,moudleInd);
//                    int p = eElement.getAttribute("classname").lastIndexOf(".");
//                    packageName = eElement.getAttribute("classname").substring(0, p);
//                    className = eElement.getAttribute("classname").substring(p + 1);
//                    testName = eElement.getAttribute("name");
//                    if(eElement.getElementsByTagName("error").item(0) != null){
//                        status = TestResultStatus.FAILED;
//                    }else{
//                        status = TestResultStatus.PASSED;
//                    }
//                    testList.add(new TestResult(moduleName, packageName, className, testName, duration, status));
//                }
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
	}

	private static void createTestListFromFile2(File surefireFile, long startingTime, List<TestResult> testList) {

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
				String duration = "";
				TestResultStatus status;

				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					duration = eElement.getAttribute("time");
					int moudleInd = surefireFile.getParentFile().getPath().lastIndexOf("\\target\\surefire-reports");
					int moudleInd2 = surefireFile.getParentFile().getPath().substring(0, moudleInd).lastIndexOf("\\");
					moduleName = surefireFile.getParentFile().getPath().substring(moudleInd2 + 1, moudleInd);
					int p = eElement.getAttribute("classname").lastIndexOf(".");
					packageName = eElement.getAttribute("classname").substring(0, p);
					className = eElement.getAttribute("classname").substring(p + 1);
					testName = eElement.getAttribute("name");
					if (eElement.getElementsByTagName("error").item(0) != null) {
						status = TestResultStatus.FAILED;
					} else {
						status = TestResultStatus.PASSED;
					}
					testList.add(new TestResult(moduleName, packageName, className, testName, duration, status));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void searchForSureFireFiles(File root, List<File> surefireOnly) {
		if (root == null || surefireOnly == null) return;
		if (root.isDirectory()) {
			for (File file : root.listFiles()) {
				searchForSureFireFiles(file, surefireOnly);
			}
		} else if (root.isFile() && root.getPath().contains("surefire-reports\\TEST-") && root.getPath().endsWith(".xml")) {
			surefireOnly.add(root);
		}
	}
}
