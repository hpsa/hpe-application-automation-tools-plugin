/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.uft.results;

import com.microfocus.application.automation.tools.model.ResultsPublisherModel;
import com.microfocus.application.automation.tools.results.HtmlBuildReportAction;
import com.microfocus.application.automation.tools.results.ReportMetaData;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.JUnitResultArchiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * using {@link JUnitResultArchiver};
 *
 * @author Thomas Maurel
 */
public class RunUftResultRecorder {

    public static final String REPORT_NAME_FIELD = "report";
    private static final String REPORTMETADATE_XML = "report_metadata.xml";
    private static final String PARALLEL_RESULT_FILE = "parallelrun_results.html";
    private final ResultsPublisherModel resultsPublisherModel;

    public RunUftResultRecorder(ResultsPublisherModel resultsPublisherModel) {
        this.resultsPublisherModel = resultsPublisherModel;
    }

    /**
     * checks if the given report path is a parallel runner report
     *
     * @param reportPath the path containing the report files
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean isParallelRunnerReportPath(FilePath reportPath) throws IOException, InterruptedException {
        FilePath parallelRunnerResultsFile = new FilePath(reportPath, PARALLEL_RESULT_FILE);
        return parallelRunnerResultsFile.exists();
    }

    public void archiveUftReport(Run<?, ?> build, TaskListener listener, FilePath runWorkspace, ArrayList<String> zipFileNames, ArrayList<FilePath> reportFolders, FilePath projectWS, File artifactsDir, List<ReportMetaData> reportInfoToCollect, Element testSuiteNode) throws IOException, InterruptedException {
        // UFT Test
        boolean reportIsHtml = false;
        NodeList testCasesNodes = testSuiteNode.getElementsByTagName("testcase");
        Map<String, Integer> fileNameCount = new HashMap<>();

        for (int i = 0; i < testCasesNodes.getLength(); i++) {

            Node nNode = testCasesNodes.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                if (!eElement.hasAttribute(REPORT_NAME_FIELD)) {
                    continue;
                }

                String reportFolderPath =
                        eElement.getAttribute(REPORT_NAME_FIELD); // e.g. "C:\UFTTest\GuiTest1\Report"

                String testFolderPath = eElement.getAttribute("name"); // e.g. "C:\UFTTest\GuiTest1"
                String testStatus = eElement.getAttribute("status"); // e.g. "pass"

                Node nodeSystemInfo = eElement.getElementsByTagName("system-out").item(0);
                String sysinfo = nodeSystemInfo.getFirstChild().getNodeValue();
                String testDateTime = sysinfo.substring(0, 19);

                FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);
                boolean isParallelRunnerReport = isParallelRunnerReportPath(reportFolder);

                reportFolders.add(reportFolder);

                String archiveTestResultMode =
                        resultsPublisherModel.getArchiveTestResultsMode();
                boolean archiveTestResult = false;

                //check for the new html report
                FilePath htmlReport = new FilePath(reportFolder,
                        isParallelRunnerReport ? PARALLEL_RESULT_FILE : "run_results.html");

                FilePath rrvReport = new FilePath(reportFolder, "Results.xml");
                if (htmlReport.exists()) {
                    reportIsHtml = true;
                    String htmlReportDir = reportFolder.getRemote();

                    ReportMetaData reportMetaData = new ReportMetaData();
                    reportMetaData.setFolderPath(htmlReportDir);
                    reportMetaData.setIsHtmlReport(true);
                    reportMetaData.setDateTime(testDateTime);
                    reportMetaData.setStatus(testStatus);
                    reportMetaData.setIsParallelRunnerReport(isParallelRunnerReport); // we need to handle the type for this report
                    File testFileFullName = new File(testFolderPath);
                    String testName = org.apache.commons.io.FilenameUtils.getName(testFileFullName.getPath());

                    // we must consider the case when we run the same test
                    // in the same build
                    if (isParallelRunnerReport) {
                        Integer nameCount = 1;

                        if (fileNameCount.containsKey(testName)) {
                            nameCount = fileNameCount.get(testName) + 1;
                        }

                        // update the count for this file
                        fileNameCount.put(testName, nameCount);

                        testName += "[" + nameCount + "]";
                    }

                    String resourceUrl = "artifact/UFTReport/" + testName;
                    reportMetaData.setResourceURL(resourceUrl);
                    reportMetaData.setDisPlayName(testName); // use the name, not the full path

                    //don't know reportMetaData's URL path yet, we will generate it later.
                    reportInfoToCollect.add(reportMetaData);

                    listener.getLogger()
                            .println("add html report info to ReportInfoToCollect: " + "[date]" + testDateTime);
                }

                archiveTestResult = isArchiveTestResult(testStatus, archiveTestResultMode);

                if (archiveTestResult && rrvReport.exists()) {

                    if (reportFolder.exists()) {

                        FilePath testFolder = new FilePath(projectWS.getChannel(), testFolderPath);

                        String zipFileName = getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                        zipFileNames.add(zipFileName);

                        listener.getLogger().println(
                                "Zipping report folder: " + reportFolderPath);

                        ByteArrayOutputStream outstr = new ByteArrayOutputStream();

                        //don't use FileFilter for zip, or it will cause bug when files are on slave
                        reportFolder.zip(outstr);

                        /*
                         * I did't use copyRecursiveTo or copyFrom due to
                         * bug in
                         * jekins:https://issues.jenkins-ci.org/browse
                         * /JENKINS-9189 //(which is cleaimed to have been
                         * fixed, but not. So I zip the folder to stream and
                         * copy it to the master.
                         */

                        ByteArrayInputStream instr = new ByteArrayInputStream(outstr.toByteArray());

                        FilePath archivedFile = new FilePath(new FilePath(artifactsDir), zipFileName);
                        archivedFile.copyFrom(instr);
                        listener.getLogger().println(
                                "copy from slave to master: " + archivedFile);
                        outstr.close();
                        instr.close();

                        // add to Report list
                        ReportMetaData reportMetaData = new ReportMetaData();
                        reportMetaData.setIsHtmlReport(false);
                        // reportMetaData.setFolderPath(htmlReportDir); //no need for RRV
                        File testFileFullName = new File(testFolderPath);
                        String testName = testFileFullName.getName();
                        reportMetaData.setDisPlayName(testName); // use the name, not the full path
                        String zipFileUrlName = "artifact/" + zipFileName;
                        reportMetaData.setUrlName(
                                zipFileUrlName); // for RRV, the file url and resource url are the same.
                        reportMetaData.setResourceURL(zipFileUrlName);
                        reportMetaData.setDateTime(testDateTime);
                        reportMetaData.setStatus(testStatus);
                        reportInfoToCollect.add(reportMetaData);

                    } else {
                        listener.getLogger().println(
                                "No report folder was found in: " + reportFolderPath);
                    }
                }

            }
        }

        if (reportIsHtml && !reportInfoToCollect.isEmpty()) {

            listener.getLogger().println("begin to collectAndPrepareHtmlReports");
            collectAndPrepareHtmlReports(build, listener, reportInfoToCollect, runWorkspace);
        }

        if (!reportInfoToCollect.isEmpty()) {
            // serialize report metadata
            File reportMetaDataXmlFile = new File(artifactsDir.getParent(), REPORTMETADATE_XML);
            String reportMetaDataXml = reportMetaDataXmlFile.getAbsolutePath();
            writeReportMetaData2XML(reportInfoToCollect, reportMetaDataXml, listener);

            // Add UFT report action
            try {
                listener.getLogger().println("Adding a report action to the current build.");
                HtmlBuildReportAction reportAction = new HtmlBuildReportAction(build);
                build.addAction(reportAction);

            } catch (IOException | SAXException | ParserConfigurationException ex) {
                listener.getLogger().println("a problem adding action: " + ex);
            }
        }
    }


    /*
     * if we have a directory with file name "file.zip" we will return "file_1.zip"
     */
    private String getUniqueZipFileNameInFolder(ArrayList<String> names, String fileName)
            throws IOException, InterruptedException {

        String result = fileName + "_Report.zip";

        int index = 0;

        while (names.indexOf(result) > -1) {
            result = fileName + "_" + (++index) + "_Report.zip";
        }

        return result;
    }

    private void writeReportMetaData2XML(List<ReportMetaData> htmlReportsInfo, String xmlFile, TaskListener _logger) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            _logger.error("Failed creating xml doc report: " + e);
            return;
        }
        Document doc = builder.newDocument();
        Element root = doc.createElement("reports_data");
        doc.appendChild(root);

        for (ReportMetaData htmlReportInfo : htmlReportsInfo) {
            String disPlayName = htmlReportInfo.getDisPlayName();
            String urlName = htmlReportInfo.getUrlName();
            String resourceURL = htmlReportInfo.getResourceURL();
            String dateTime = htmlReportInfo.getDateTime();
            String status = htmlReportInfo.getStatus();
            String isHtmlReport = htmlReportInfo.getIsHtmlReport() ? "true" : "false";
            String isParallelRunnerReport = htmlReportInfo.getIsParallelRunnerReport() ? "true" : "false";
            Element elmReport = doc.createElement(REPORT_NAME_FIELD);
            elmReport.setAttribute("disPlayName", disPlayName);
            elmReport.setAttribute("urlName", urlName);
            elmReport.setAttribute("resourceURL", resourceURL);
            elmReport.setAttribute("dateTime", dateTime);
            elmReport.setAttribute("status", status);
            elmReport.setAttribute("isHtmlreport", isHtmlReport);
            elmReport.setAttribute("isParallelRunnerReport", isParallelRunnerReport);
            root.appendChild(elmReport);

        }

        try {
            write2XML(doc, xmlFile);
        } catch (TransformerException e) {
            _logger.error("Failed transforming xml file: " + e);
        } catch (FileNotFoundException e) {
            _logger.error("Failed to find " + xmlFile + ": " + e);
        }
    }

    private void write2XML(Document document, String filename) throws TransformerException, FileNotFoundException {
        document.normalize();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(document);
        PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source, result);

    }

    private Boolean collectAndPrepareHtmlReports(Run build, TaskListener listener, List<ReportMetaData> htmlReportsInfo,
                                                 FilePath runWorkspace)
            throws IOException, InterruptedException {
        File reportDir = new File(new File(build.getRootDir(), "archive"), "UFTReport");

        FilePath rootTarget = new FilePath(reportDir);

        try {
            for (ReportMetaData htmlReportInfo : htmlReportsInfo) {

                // make sure it's a html report
                if (!htmlReportInfo.getIsHtmlReport()) {
                    continue;
                }

                String htmlReportDir = htmlReportInfo.getFolderPath(); // C:\UFTTest\GuiTest1\Report

                listener.getLogger().println("collectAndPrepareHtmlReports, collecting:" + htmlReportDir);
                listener.getLogger().println("workspace: " + runWorkspace);

                // copy to the subdirs of master
                FilePath source = new FilePath(runWorkspace, htmlReportDir);

                // if it's a parallel runner report path, we must change the
                // resFileName
                boolean isParallelRunner = isParallelRunnerReportPath(source);

                listener.getLogger().println("source: " + source);
                String testName = htmlReportInfo.getDisPlayName(); // like "GuiTest1"
                String dest = testName;
                FilePath targetPath = new FilePath(rootTarget, dest); // target path is something like "C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\GuiTest1"

                // zip copy and unzip
                ByteArrayOutputStream outstr = new ByteArrayOutputStream();

                // don't use FileFilter for zip, or it will cause bug when files are on slave
                source.zip(outstr);
                ByteArrayInputStream instr = new ByteArrayInputStream(outstr.toByteArray());

                String zipFileName = "UFT_Report_HTML_tmp.zip";
                FilePath archivedFile = new FilePath(rootTarget, zipFileName);

                archivedFile.copyFrom(instr);

                listener.getLogger().println("copy from slave to master: " + archivedFile);
                outstr.close();
                instr.close();

                // unzip
                archivedFile.unzip(rootTarget);
                archivedFile.delete();

                // now,all the files are in the C:\Program Files (x86)
                // \Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                // we need to rename the above path to targetPath.
                // So at last we got files in C:\Program Files (x86)
                // \Jenkins\jobs\testAction\builds\35\archive\UFTReport\GuiTest

                String unzippedFileName = org.apache.commons.io.FilenameUtils.getName(htmlReportDir);
                FilePath unzippedFolderPath = new FilePath(rootTarget, unzippedFileName); // C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                // FilePath unzippedFolderPath = new FilePath(rootTarget, source.getName()); //C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                unzippedFolderPath.renameTo(targetPath);
                listener.getLogger()
                        .println("UnzippedFolderPath is: " + unzippedFolderPath + " targetPath is: " + targetPath);
                // end zip copy and unzip

                // fill in the urlName of this report. we need a network path not a FS path
                String resourceUrl = htmlReportInfo.getResourceURL();
                String resFileName = isParallelRunner ? "/parallelrun_results.html" : "/run_results.html";

                String urlName = resourceUrl + resFileName; // like artifact/UFTReport/GuiTest1/run_results.html
                // or for Parallel runner /GuiTest1[1]/parallelrun_results.html

                listener.getLogger().println("set the report urlName to " + urlName);
                htmlReportInfo.setUrlName(urlName);

            }
        } catch (Exception ex) {
            listener.getLogger().println("catch exception in collectAndPrepareHtmlReports: " + ex);
        }

        return true;
    }

    private boolean isArchiveTestResult(String testStatus, String archiveTestResultMode) {
        if (archiveTestResultMode.equals(ResultsPublisherModel.alwaysArchiveResults.getValue()) ||
                archiveTestResultMode.equals(ResultsPublisherModel.CreateHtmlReportResults.getValue())) {
            return true;
        } else if (archiveTestResultMode.equals(ResultsPublisherModel.ArchiveFailedTestsResults.getValue())) {
            if ("fail".equals(testStatus)) {
                return true;
            } else if (archiveTestResultMode.equals(ResultsPublisherModel.dontArchiveResults.getValue())) {
                return false;
            }
        }
        return false;
    }
}
