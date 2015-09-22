package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.xml.JUnitXmlIterator;
import com.hp.mqm.clt.xml.TestResultXmlWriter;
import org.apache.commons.io.IOUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XmlProcessor {

    public List<TestResult> processJUnitXmlFile(File junitXmlFile) {
        if (junitXmlFile == null || !junitXmlFile.canRead()) {
            String fileNameInfo = (junitXmlFile == null) ? "" : ": " + junitXmlFile.getName();
            System.out.println("Can not read the JUnit file" + fileNameInfo);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        List<TestResult> testResults = new LinkedList<TestResult>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(junitXmlFile);
            JUnitXmlIterator iterator = new JUnitXmlIterator(fis);
            while (iterator.hasNext()) {
                testResults.add(iterator.next());
            }
        } catch (IOException e) {
            System.out.println("Unable to process JUnit XML file: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process JUnit XML file, XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (InterruptedException e) {
            System.out.println("Unable to process JUnit XML file, process was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        }

        if (testResults.isEmpty()) {
            System.out.println("No valid test results to push");
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        return testResults;
    }
}
