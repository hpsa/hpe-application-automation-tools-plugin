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

package com.microfocus.application.automation.tools.lr.results;

import com.microfocus.application.automation.tools.model.ResultsPublisherModel;
import com.microfocus.application.automation.tools.results.PerformanceJobReportAction;
import com.microfocus.application.automation.tools.results.PerformanceReportAction;
import com.microfocus.application.automation.tools.results.RichReportAction;
import com.microfocus.application.automation.tools.results.TransactionSummaryAction;
import com.microfocus.application.automation.tools.results.projectparser.performance.*;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

import static com.microfocus.application.automation.tools.results.projectparser.performance.XmlParserUtil.getNode;
import static com.microfocus.application.automation.tools.results.projectparser.performance.XmlParserUtil.getNodeAttr;

/**
 * using {@link JUnitResultArchiver};
 *
 * @author Thomas Maurel
 */
public class RunLrResultRecorder {

    private static final String REPORT_NAME_FIELD = "report";
    private static final int SECS_IN_DAY = 86400;
    private static final int SECS_IN_HOUR = 3600;
    private static final int SECS_IN_MINUTE = 60;
    private static final String SLA_ULL_NAME = "FullName";
    private static final String PERFORMANCE_REPORT_FOLDER = "PerformanceReport";
    private static final String IE_REPORT_FOLDER = "IE";
    private static final String HTML_REPORT_FOLDER = "HTML";
    private static final String LRA_FOLDER = "LRA";
    private static final String INDEX_HTML_NAME = "index.html";
    private static final String REPORT_INDEX_NAME = "report.index";
    private static final String TRANSACTION_SUMMARY_FOLDER = "TransactionSummary";
    private static final String RICH_REPORT_FOLDER = "RichReport";
    private static final String TRANSACTION_REPORT_NAME = "TransactionReport";
    private static final String SLA_ACTUAL_VALUE_LABEL = "ActualValue";
    private static final String SLA_GOAL_VALUE_LABEL = "GoalValue";
    private static final String NO_RICH_REPORTS_ERROR = "Template contains no rich reports.";
    private static final String NO_TRANSACTION_SUMMARY_REPORT_ERROR = "Template contains no transaction summary report.";
    private static final String ARCHIVING_TEST_REPORTS_FAILED_DUE_TO_XML_PARSING_ERROR =
            "Archiving test reports failed due to xml parsing error: ";

    private final ResultsPublisherModel resultsPublisherModel;
    private List<FilePath> runReportList;

    public RunLrResultRecorder(ResultsPublisherModel resultsPublisherModel) {
        this.resultsPublisherModel = resultsPublisherModel;
    }

    private static void addTimeRanges(TimeRangeResult transactionTimeRange, Element slaRuleElement) {
        Node timeRangeNode;
        Element timeRangeElement;
        NodeList timeRanges = slaRuleElement.getElementsByTagName("TimeRangeInfo");
        if (timeRanges == null || timeRanges.getLength() == 0) {
            return;
        }
        //Taking the goal per transaction -
        double generalGoalValue = Double.parseDouble(((Element) timeRanges.item(0)).getAttribute(SLA_GOAL_VALUE_LABEL));
        transactionTimeRange.setGoalValue(generalGoalValue);

        for (int k = 0; k < timeRanges.getLength(); k++) {
            timeRangeNode = timeRanges.item(k);
            timeRangeElement = (Element) timeRangeNode;
            double actualValue = Double.parseDouble(timeRangeElement.getAttribute(SLA_ACTUAL_VALUE_LABEL));
            double goalValue = Double.parseDouble(timeRangeElement.getAttribute(SLA_GOAL_VALUE_LABEL));
            int loadValue = Integer.parseInt(timeRangeElement.getAttribute("LoadValue"));
            double startTime = Double.parseDouble(timeRangeElement.getAttribute("StartTime"));
            double endTIme = Double.parseDouble(timeRangeElement.getAttribute("EndTime"));
            transactionTimeRange.incActualValue(actualValue);
            LrTest.SLA_STATUS slaStatus =
                    LrTest.SLA_STATUS.checkStatus(timeRangeElement.getFirstChild().getTextContent());
            TimeRange timeRange = new TimeRange(actualValue, goalValue, slaStatus, loadValue, startTime, endTIme);
            transactionTimeRange.getTimeRanges().add(timeRange);
        }
    }

    public void setRunReportList(List<FilePath> runReportList) {
        this.runReportList = runReportList;
    }

    /**
     * Adds the html reports actions to the left side menu.
     *
     * @param build
     */
    private void publishLrReports(@Nonnull Run<?, ?> build) throws IOException {
        File reportDirectory = new File(build.getRootDir(), PERFORMANCE_REPORT_FOLDER);
        if (reportDirectory.exists()) {
            File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
            if (htmlIndexFile.exists()) {
                build.replaceAction(new PerformanceReportAction(build));
            }
        }

        File summaryDirectory = new File(build.getRootDir(), TRANSACTION_SUMMARY_FOLDER);
        if (summaryDirectory.exists()) {
            File htmlIndexFile = new File(summaryDirectory, INDEX_HTML_NAME);
            if (htmlIndexFile.exists()) {
                build.replaceAction(new TransactionSummaryAction(build));
            }
        }

        File richDirectory = new File(build.getRootDir(), RICH_REPORT_FOLDER);
        if (richDirectory.exists()) {
            File htmlIndexFile = new File(richDirectory, INDEX_HTML_NAME);
            if (htmlIndexFile.exists()) {
                build.replaceAction(new RichReportAction(build));
            }
        }
    }

    public boolean archiveLrReport(Run<?, ?> build, TaskListener listener, TestResult testResult, ArrayList<String> zipFileNames, ArrayList<FilePath> reportFolders, List<String> reportNames, FilePath projectWS, File artifactsDir, Document doc) throws IOException, InterruptedException {
        Node testSuiteNode;
        Element testSuiteElement;// LR test
        NodeList testSuiteNodes = doc.getElementsByTagName("testsuite");
        for (int i = 0; i < testSuiteNodes.getLength(); i++) {
            testSuiteNode = testSuiteNodes.item(i);
            testSuiteElement = (Element) testSuiteNode;
            if (!testSuiteElement.hasAttribute("name")) {
                continue;
            }
            String testFolderPath = testSuiteElement.getAttribute("name");
            int testPathArr = testFolderPath.lastIndexOf('\\');
            String testName = testFolderPath.substring(testPathArr + 1);
            reportNames.add(testName);
            String testStatus = ("0".equals(testSuiteElement.getAttribute("failures"))) ? "pass" : "fail";

            Node testCaseNode = testSuiteElement.getElementsByTagName("testcase").item(0);
            if (testCaseNode == null) {
                listener.getLogger().println("No report folder was found in results");
                return true;
            }
            if (testCaseNode.getNodeType() == Node.ELEMENT_NODE) {

                Element testCaseElement = (Element) testCaseNode;

                if (!testCaseElement.hasAttribute(REPORT_NAME_FIELD)) {
                    continue;
                }

                String reportFolderPath = testCaseElement.getAttribute(REPORT_NAME_FIELD);
                FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);
                reportFolders.add(reportFolder);

                FilePath testFolder = new FilePath(projectWS.getChannel(), testFolderPath);
                String zipFileName = getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                FilePath archivedFile = new FilePath(new FilePath(artifactsDir), zipFileName);

                if (archiveFolder(reportFolder, testStatus, archivedFile, listener)) {
                    zipFileNames.add(zipFileName);
                }

                createRichReports(reportFolder, testFolderPath, artifactsDir, reportNames, testResult, listener);
                createHtmlReport(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                createTransactionSummary(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                try {
                    FilePath testSla = copyRunReport(reportFolder, build.getRootDir(),
                            testFolder.getName());
                    if (testSla == null) {
                        listener.getLogger().println("no RunReport.xml file was created");
                    } else {
                        runReportList.add(testSla);
                    }
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println(e);
                }
            }
        }
        return false;
    }


    /**
     * Copies the run report from the executing node to the Jenkins master for processing.
     *
     * @param reportFolder
     * @param buildDir
     * @param scenarioName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private FilePath copyRunReport(FilePath reportFolder, File buildDir, String scenarioName)
            throws IOException, InterruptedException {
        FilePath slaReportFilePath = new FilePath(reportFolder, "RunReport.xml");
        if (slaReportFilePath.exists()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            slaReportFilePath.zip(baos);
            File slaDirectory = new File(buildDir, "RunReport");
            if (!slaDirectory.exists()) {
                slaDirectory.mkdir();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            FilePath slaDirectoryFilePath = new FilePath(slaDirectory);
            FilePath tmpZipFile = new FilePath(slaDirectoryFilePath, "runReport.zip");
            tmpZipFile.copyFrom(bais);
            bais.close();
            baos.close();
            tmpZipFile.unzip(slaDirectoryFilePath);
            FilePath slaFile = new FilePath(slaDirectoryFilePath, "RunReport.xml");
            slaFile.getBaseName();
            slaFile.renameTo(new FilePath(slaDirectoryFilePath, scenarioName + ".xml"));

            slaFile = new FilePath(slaDirectoryFilePath, scenarioName + ".xml");

            return slaFile;
        }
        return null;
    }

    private boolean archiveFolder(FilePath reportFolder,
                                  String testStatus,
                                  FilePath archivedFile,
                                  TaskListener listener) throws IOException, InterruptedException {
        String archiveTestResultMode = resultsPublisherModel.getArchiveTestResultsMode();
        boolean archiveTestResult;

        archiveTestResult = isArchiveTestResult(testStatus, archiveTestResultMode);

        if (archiveTestResult) {

            if (reportFolder.exists()) {

                listener.getLogger().println(
                        "Zipping report folder: " + reportFolder);

                ByteArrayOutputStream outstr = new ByteArrayOutputStream();
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

                archivedFile.copyFrom(instr);

                outstr.close();
                instr.close();
                return true;
            } else {
                listener.getLogger().println(
                        "No report folder was found in: " + reportFolder);
            }
        }

        return false;
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

    /**
     * Copy Summary Html reports created by LoadRunner
     *
     * @param reportFolder
     * @param testFolderPath
     * @param artifactsDir
     * @param reportNames
     * @param testResult
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("squid:S134")
    private void createHtmlReport(FilePath reportFolder,
                                  String testFolderPath,
                                  File artifactsDir,
                                  List<String> reportNames,
                                  TestResult testResult) throws IOException, InterruptedException {
        String archiveTestResultMode =
                resultsPublisherModel.getArchiveTestResultsMode();
        boolean createReport = archiveTestResultMode.equals(ResultsPublisherModel.CreateHtmlReportResults.getValue());

        if (createReport) {
            File testFolderPathFile = new File(testFolderPath);
            FilePath srcDirectoryFilePath = new FilePath(reportFolder, HTML_REPORT_FOLDER);
            if (srcDirectoryFilePath.exists()) {
                FilePath srcFilePath = new FilePath(srcDirectoryFilePath, IE_REPORT_FOLDER);
                if (srcFilePath.exists()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    srcFilePath.zip(baos);
                    File reportDirectory = new File(artifactsDir.getParent(), PERFORMANCE_REPORT_FOLDER);
                    if (!reportDirectory.exists()) {
                        reportDirectory.mkdir();
                    }
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    FilePath reportDirectoryFilePath = new FilePath(reportDirectory);
                    FilePath tmpZipFile = new FilePath(reportDirectoryFilePath, "tmp.zip");
                    tmpZipFile.copyFrom(bais);
                    bais.close();
                    baos.close();
                    tmpZipFile.unzip(reportDirectoryFilePath);
                    String newFolderName = org.apache.commons.io.FilenameUtils.getName(testFolderPathFile.getPath());
                    FileUtils.moveDirectory(new File(reportDirectory, IE_REPORT_FOLDER),
                            new File(reportDirectory, newFolderName));
                    tmpZipFile.delete();
                    outputReportFiles(reportNames, reportDirectory, testResult, "Performance Report", HTML_REPORT_FOLDER);
                }
            }
        }
    }

    /**
     * Create a new directory RichReport/scenario_name on master and copy the .pdf files from slave LRA folder
     */
    private void createRichReports(FilePath reportFolder,
                                   String testFolderPath,
                                   File artifactsDir,
                                   List<String> reportNames,
                                   TestResult testResult,
                                   TaskListener listener) {
        try {
            File testFolderPathFile = new File(testFolderPath);
            FilePath htmlReportPath = new FilePath(reportFolder, LRA_FOLDER);
            if (htmlReportPath.exists()) {
                File reportDirectory = new File(artifactsDir.getParent(), RICH_REPORT_FOLDER);
                if (!reportDirectory.exists()) {
                    reportDirectory.mkdir();
                }
                String newFolderName = org.apache.commons.io.FilenameUtils.getName(testFolderPathFile.getPath());
                File testDirectory = new File(reportDirectory, newFolderName);
                if (!testDirectory.exists()) {
                    testDirectory.mkdir();
                }

                FilePath dstReportPath = new FilePath(testDirectory);
                FileFilter reportFileFilter = new WildcardFileFilter("*.pdf");
                List<FilePath> reportFiles = htmlReportPath.list(reportFileFilter);
                List<String> richReportNames = new ArrayList<>();
                for (FilePath fileToCopy : reportFiles) {
                    FilePath dstFilePath = new FilePath(dstReportPath, fileToCopy.getName());
                    fileToCopy.copyTo(dstFilePath);
                    richReportNames.add(dstFilePath.getName());
                }

                outputReportFiles(reportNames, reportDirectory, testResult, "Rich Reports", INDEX_HTML_NAME);
                createRichReportHtml(testDirectory, richReportNames);
            }
        } catch (IOException | InterruptedException ex) {
            listener.getLogger().println("Exception caught while creating rich reports: " + ex);
        }
    }

    private void createErrorHtml(File htmlDirectory, String error) throws IOException {
        String htmlFileContents = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<HTML><HEAD>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                "<TITLE>Rich Report</TITLE>" +
                "</HEAD>" + "<BODY>" + error + "</BODY>";

        writeToFile(htmlDirectory, htmlFileContents);
    }

    private void createRichReportHtml(File reportDirectory, List<String> richReportNames) throws IOException {
        String htmlFileContents = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<HTML><HEAD>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                "<TITLE>Rich Report</TITLE>" + "</HEAD>" + "<BODY>";

        if (richReportNames.size() == 0) {
            htmlFileContents += NO_RICH_REPORTS_ERROR;
        } else {
            for (String richReportName : richReportNames) {
                htmlFileContents += "<iframe src=\"./" + richReportName + "\" width=\"100%%\" height=\"800px\" frameBorder=\"0\"></iframe>";
            }
        }

        htmlFileContents += "</BODY>";

        File richReportsHtml = new File(reportDirectory, HTML_REPORT_FOLDER + ".html");
        writeToFile(richReportsHtml, htmlFileContents);
    }

    private void writeToFile(File file, String contents) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.flush();
        writer.close();
    }

    /**
     * creates index files as index for the different scenarios.
     *
     * @param reportNames
     * @param reportDirectory
     * @param testResult
     * @throws IOException
     */
    private void outputReportFiles(List<String> reportNames, File reportDirectory, TestResult testResult,
                                   String title, String htmlFileName) throws IOException {
        if (reportNames.isEmpty()) {
            return;
        }
        File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
        BufferedWriter writer = new BufferedWriter(new FileWriter(htmlIndexFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>%n");
        writer.write("<HTML><HEAD>%n");
        writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">%n");
        writer.write(String.format("<TITLE>%s</TITLE>%n", title));
        writer.write("</HEAD>%n");
        writer.write("<BODY>%n");
        writer.write(
                "<table style=\"font-size:15px;width:100%;max-width:100%;border-left:1px solid #DDD;border-right:1px " +
                        "solid #DDD;border-bottom:1px solid #DDD;\">%n");
        writer.write(
                "<tr style=\"background-color: #F1F1F1;\"><th style=\"padding:8px;line-height:1.42857;" +
                        "vertical-align:top;border-top:1px solid #DDD;\">Name</th></tr>%n");
        boolean rolling = true;
        for (String report : reportNames) {
            if (rolling) {
                writer.write(String.format(
                        "<tr style=\"background-color: #FFF;\"><td style=\"padding:8px;line-height:1.42857;" +
                                "vertical-align:top;border-top:1px solid #DDD;" +
                                "\"><a href=\"./%s/%s\">%s</a></td></tr>%n",
                        report, htmlFileName, report));
                rolling = false;
            } else {
                writer.write(String.format(
                        "<tr style=\"background-color: #F1F1F1;\"><td style=\"padding:8px;line-height:1.42857;" +
                                "vertical-align:top;border-top:1px solid #DDD;" +
                                "\"><a href=\"./%s/%s\">%s</a></td></tr>%n",
                        report, htmlFileName, report));
                rolling = true;
            }
        }

        writer.write("</table>%n");
        writer.write("</BODY>%n");
        writer.flush();
        writer.close();

        File indexFile = new File(reportDirectory, REPORT_INDEX_NAME);
        try {
            writer = new BufferedWriter(new FileWriter(indexFile));
            Iterator<SuiteResult> resultIterator = null;
            if ((testResult != null) && (!testResult.getSuites().isEmpty())) {
                resultIterator = testResult.getSuites().iterator();//get the first
            }
            for (String report : reportNames) {
                SuiteResult suitResult = null;
                if ((resultIterator != null) && resultIterator.hasNext()) {
                    suitResult = resultIterator.next();
                }
                if (suitResult == null) {
                    writer.write(report + "\t##\t##\t##%n");
                } else {
                    int iDuration = (int) suitResult.getDuration();
                    StringBuilder bld = new StringBuilder();
                    String duration = "";
                    if ((iDuration / SECS_IN_DAY) > 0) {
                        bld.append(String.format("%dday ", iDuration / SECS_IN_DAY));
                        iDuration = iDuration % SECS_IN_DAY;
                    }
                    if ((iDuration / SECS_IN_HOUR) > 0) {
                        bld.append(String.format("%02dhr ", iDuration / SECS_IN_HOUR));
                        iDuration = iDuration % SECS_IN_HOUR;
                    } else if (!duration.isEmpty()) {
                        bld.append("00hr ");
                    }
                    if ((iDuration / SECS_IN_MINUTE) > 0) {
                        bld.append(String.format("%02dmin ", iDuration / SECS_IN_MINUTE));
                        iDuration = iDuration % SECS_IN_MINUTE;
                    } else if (!duration.isEmpty()) {
                        bld.append("00min ");
                    }
                    bld.append(String.format("%02dsec", iDuration));
                    duration = bld.toString();
                    int iPassCount = 0;
                    int iFailCount = 0;
                    for (CaseResult caseResult : suitResult.getCases()) {
                        iPassCount += caseResult.getPassCount();
                        iFailCount += caseResult.getFailCount();
                    }
                    writer.write(
                            String.format("%s\t%s\t%d\t%d%n",
                                    report,
                                    duration,
                                    iPassCount,
                                    iFailCount));
                }
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }

    /**
     * Copies and creates the transaction summery on the master
     *
     * @param reportFolder
     * @param testFolderPath
     * @param artifactsDir
     * @param reportNames
     * @param testResult
     * @throws IOException
     * @throws InterruptedException
     */
    private void createTransactionSummary(FilePath reportFolder,
                                          String testFolderPath,
                                          File artifactsDir,
                                          List<String> reportNames,
                                          TestResult testResult) throws IOException, InterruptedException {

        File testFolderPathFile = new File(testFolderPath);
        String subFolder = HTML_REPORT_FOLDER + File.separator + IE_REPORT_FOLDER + File.separator + HTML_REPORT_FOLDER;
        FilePath htmlReportPath = new FilePath(reportFolder, subFolder);
        if (htmlReportPath.exists()) {
            File reportDirectory = new File(artifactsDir.getParent(), TRANSACTION_SUMMARY_FOLDER);
            if (!reportDirectory.exists()) {
                reportDirectory.mkdir();
            }
            String newFolderName = org.apache.commons.io.FilenameUtils.getName(testFolderPathFile.getPath());
            File testDirectory = new File(reportDirectory, newFolderName);
            if (!testDirectory.exists()) {
                testDirectory.mkdir();
            }

            FilePath dstReportPath = new FilePath(testDirectory);
            FilePath transSummaryReport,
                    transSummaryReportExcel;
            if ((transSummaryReport = getTransactionSummaryReport(htmlReportPath)) != null) {
                FilePath dstFilePath = new FilePath(dstReportPath, TRANSACTION_REPORT_NAME + ".html");
                transSummaryReport.copyTo(dstFilePath);

                //Copy the .xls which is being referenced by the report
                if ((transSummaryReportExcel =
                        getTransactionSummaryReportExcel(htmlReportPath, transSummaryReport.getName())) != null) {
                    dstFilePath = new FilePath(dstReportPath, transSummaryReportExcel.getName());
                    transSummaryReportExcel.copyTo(dstFilePath);
                }
            } else {
                File htmlIndexFile = new File(testDirectory, TRANSACTION_REPORT_NAME + ".html");
                createErrorHtml(htmlIndexFile, NO_TRANSACTION_SUMMARY_REPORT_ERROR);
            }

            FilePath cssFilePath = new FilePath(htmlReportPath, "Properties.css");
            if (cssFilePath.exists()) {
                FilePath dstFilePath = new FilePath(dstReportPath, cssFilePath.getName());
                cssFilePath.copyTo(dstFilePath);
            }
            FilePath pngFilePath = new FilePath(htmlReportPath, "tbic_toexcel.png");
            if (pngFilePath.exists()) {
                FilePath dstFilePath = new FilePath(dstReportPath, pngFilePath.getName());
                pngFilePath.copyTo(dstFilePath);
            }

            FilePath jsDstReportPath = new FilePath(testDirectory);
            FileFilter jsReportFileFilter = new WildcardFileFilter("*.js");
            List<FilePath> jsReporFiles = htmlReportPath.list(jsReportFileFilter);
            for (FilePath fileToCopy : jsReporFiles) {
                FilePath dstFilePath = new FilePath(jsDstReportPath, fileToCopy.getName());
                fileToCopy.copyTo(dstFilePath);
            }

            outputReportFiles(reportNames, reportDirectory, testResult, "Transaction Summary",
                    TRANSACTION_REPORT_NAME + ".html");
        }
    }

    /**
     * Scan the LRA folder from slave to find the report containting Transaction Summary
     * as title (or title variants based on language packs)
     */
    private FilePath getTransactionSummaryReport(FilePath htmlReportPath) throws IOException, InterruptedException {
        String[] transactionSummaryNames = {
                "Transaction Summary", //eng
                "トランザクション サマリ", //jpn
                "트랜잭션 요약", //kor
                "事务摘要", //chs
                "Transaktionsübersicht", //deu
                "Resumen de transacciones", //spn
                "Riepilogo transazioni", //ita
                "Récapitulatif des transactions", //fr
                "Сводка транзакций", //rus
        };

        FileFilter reportFileFilter = new WildcardFileFilter("Report*.html");
        List<FilePath> reportFiles = htmlReportPath.list(reportFileFilter);
        for (FilePath fileToCopy : reportFiles) {
            Scanner scanner = new Scanner(fileToCopy.read()).useDelimiter("\\A");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String transactionSummaryName : transactionSummaryNames) {
                    if (line.contains(transactionSummaryName)) {
                        return fileToCopy;
                    }
                }
            }
        }

        return null;
    }

    private FilePath getTransactionSummaryReportExcel(FilePath htmlReportPath, String reportName)
            throws IOException, InterruptedException {
        FileFilter reportFileFilter = new WildcardFileFilter(reportName.replace("html", "xls"));
        List<FilePath> reportFiles = htmlReportPath.list(reportFileFilter);

        if (!reportFiles.isEmpty()) {
            return reportFiles.get(0);
        }

        return null;
    }

    /*
     * if we have a directory with file name "file.zip" we will return "file_1.zip"
     */
    private String getUniqueZipFileNameInFolder(ArrayList<String> names, String fileName) {
        String result = fileName + "_Report.zip";
        int index = 0;

        while (names.indexOf(result) > -1) {
            result = fileName + "_" + (++index) + "_Report.zip";
        }

        return result;
    }

    private LrJobResults buildJobDataset(TaskListener listener)
            throws ParserConfigurationException, SAXException,
            IOException, InterruptedException {
        listener.getLogger().println(
                "Parsing test run dataset for perfomrance report");
        LrJobResults jobResults = new LrJobResults();

        // read each RunReport.xml
        for (FilePath reportFilePath : runReportList) {
            JobLrScenarioResult jobLrScenarioResult = parseScenarioResults(reportFilePath);
            jobResults.addScenario(jobLrScenarioResult);
        }

        return jobResults;
    }

    private JobLrScenarioResult parseScenarioResults(FilePath slaFilePath)
            throws ParserConfigurationException, SAXException, IOException, InterruptedException {
        JobLrScenarioResult jobLrScenarioResult = new JobLrScenarioResult(slaFilePath.getBaseName());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(slaFilePath.read());

        processSLA(jobLrScenarioResult, doc);
        processLrScenarioStats(jobLrScenarioResult, doc);
        //TODO: add fail / Pass count
        return jobLrScenarioResult;
    }

    private void processLrScenarioStats(JobLrScenarioResult jobLrScenarioResult, Document doc) {

        NodeList rootNodes = doc.getChildNodes();
        Node root = getNode("Runs", rootNodes);
        Element generalNode = (Element) getNode("General", root.getChildNodes());
        NodeList generalNodeChildren = generalNode.getChildNodes();

        extractVUserScenarioReult(jobLrScenarioResult, generalNodeChildren);
        extractTransactionScenarioResult(jobLrScenarioResult, generalNodeChildren);
        extractConnectionsScenarioResult(jobLrScenarioResult, generalNodeChildren);
        extractDuration(jobLrScenarioResult, generalNodeChildren);
    }

    private void extractDuration(JobLrScenarioResult jobLrScenarioResult, NodeList generalNodeChildren) {
        Node ScenrioDurationNode = getNode("Time", generalNodeChildren);
        String scenarioDurationAttr = getNodeAttr("Duration", ScenrioDurationNode);
        jobLrScenarioResult.setScenarioDuration(Long.valueOf(scenarioDurationAttr));
    }

    private void extractConnectionsScenarioResult(JobLrScenarioResult jobLrScenarioResult,
                                                  NodeList generalNodeChildren) {
        Node connections = getNode("Connections", generalNodeChildren);
        jobLrScenarioResult.setConnectionMax(Integer.valueOf(getNodeAttr("MaxCount", connections)));
    }

    private void extractTransactionScenarioResult(JobLrScenarioResult jobLrScenarioResult,
                                                  NodeList generalNodeChildren) {
        int atrrCount;
        Node transactions = getNode("Transactions", generalNodeChildren);
        atrrCount = transactions.getAttributes().getLength();
        for (int atrrIndx = 0; atrrIndx < atrrCount; atrrIndx++) {
            Node vUserAttr = transactions.getAttributes().item(atrrIndx);
            jobLrScenarioResult.transactionSum.put(vUserAttr.getNodeName(), Integer.valueOf(vUserAttr.getNodeValue()));
        }

        NodeList transactionNodes = transactions.getChildNodes();
        int transactionNodesCount = transactionNodes.getLength();
        for (int transIdx = 0; transIdx < transactionNodesCount; transIdx++) {
            if (transactionNodes.item(transIdx).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element transaction = (Element) transactionNodes.item(transIdx);
            TreeMap<String, Integer> transactionData = new TreeMap<>();
            transactionData.put("Pass", Integer.valueOf(transaction.getAttribute("Pass")));
            transactionData.put("Fail", Integer.valueOf(transaction.getAttribute("Fail")));
            transactionData.put("Stop", Integer.valueOf(transaction.getAttribute("Stop")));
            jobLrScenarioResult.transactionData.put(transaction.getAttribute("Name"), transactionData);
        }
    }

    private void extractVUserScenarioReult(JobLrScenarioResult jobLrScenarioResult, NodeList generalNodeChildren) {
        Node vUser = getNode("VUsers", generalNodeChildren);
        int atrrCount = vUser.getAttributes().getLength();
        for (int atrrIndx = 0; atrrIndx < atrrCount; atrrIndx++) {
            Node vUserAttr = vUser.getAttributes().item(atrrIndx);
            jobLrScenarioResult.vUserSum.put(vUserAttr.getNodeName(), Integer.valueOf(vUserAttr.getNodeValue()));
        }
    }

    private void processSLA(JobLrScenarioResult jobLrScenarioResult, Document doc) {
        Node slaRuleNode;
        Element slaRuleElement;

        NodeList rootNodes = doc.getChildNodes();
        Node root = getNode("Runs", rootNodes);
        Element slaRoot = (Element) getNode("SLA", root.getChildNodes());
        NodeList slaRuleResults = slaRoot.getChildNodes();


        for (int j = 0; j < slaRuleResults.getLength(); j++) {
            slaRuleNode = slaRuleResults.item(j);
            if (slaRuleNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            slaRuleElement = (Element) slaRuleNode;
            //check type by measurement field:
            LrTest.SLA_GOAL slaGoal = LrTest.SLA_GOAL.checkGoal(slaRuleElement.getAttribute("Measurement"));

            processSlaRule(jobLrScenarioResult, slaRuleElement, slaGoal);
        }

    }

    private void processSlaRule(JobLrScenarioResult jobLrScenarioResult, Element slaRuleElement,
                                LrTest.SLA_GOAL slaGoal) {
        switch (slaGoal) {
            case AverageThroughput:
                WholeRunResult averageThroughput = new WholeRunResult();
                averageThroughput.setSlaGoal(LrTest.SLA_GOAL.AverageThroughput);
                averageThroughput.setActualValue(Double.valueOf(slaRuleElement.getAttribute(SLA_ACTUAL_VALUE_LABEL)));
                averageThroughput.setGoalValue(Double.valueOf(slaRuleElement.getAttribute(SLA_GOAL_VALUE_LABEL)));
                averageThroughput.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                averageThroughput.setStatus(LrTest.SLA_STATUS.checkStatus(slaRuleElement.getLastChild().getTextContent().trim()));
                jobLrScenarioResult.scenarioSlaResults.add(averageThroughput);
                break;
            case TotalThroughput:
                WholeRunResult totalThroughput = new WholeRunResult();
                totalThroughput.setSlaGoal(LrTest.SLA_GOAL.TotalThroughput);
                totalThroughput.setActualValue(Double.valueOf(slaRuleElement.getAttribute(SLA_ACTUAL_VALUE_LABEL)));
                totalThroughput.setGoalValue(Double.valueOf(slaRuleElement.getAttribute(SLA_GOAL_VALUE_LABEL)));
                totalThroughput.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                totalThroughput.setStatus(LrTest.SLA_STATUS.checkStatus(slaRuleElement.getLastChild().getTextContent().trim()));
                jobLrScenarioResult.scenarioSlaResults.add(totalThroughput);

                break;
            case AverageHitsPerSecond:
                WholeRunResult averageHitsPerSecond = new WholeRunResult();
                averageHitsPerSecond.setSlaGoal(LrTest.SLA_GOAL.AverageHitsPerSecond);
                averageHitsPerSecond.setActualValue(Double.valueOf(slaRuleElement.getAttribute(SLA_ACTUAL_VALUE_LABEL)));
                averageHitsPerSecond.setGoalValue(Double.valueOf(slaRuleElement.getAttribute(SLA_GOAL_VALUE_LABEL)));
                averageHitsPerSecond.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                averageHitsPerSecond.setStatus(LrTest.SLA_STATUS.checkStatus(slaRuleElement.getLastChild().getTextContent().trim()));
                jobLrScenarioResult.scenarioSlaResults.add(averageHitsPerSecond);

                break;
            case TotalHits:
                WholeRunResult totalHits = new WholeRunResult();
                totalHits.setSlaGoal(LrTest.SLA_GOAL.TotalHits);
                totalHits.setActualValue(Double.valueOf(slaRuleElement.getAttribute(SLA_ACTUAL_VALUE_LABEL)));
                totalHits.setGoalValue(Double.valueOf(slaRuleElement.getAttribute(SLA_GOAL_VALUE_LABEL)));
                totalHits.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                totalHits.setStatus(LrTest.SLA_STATUS.checkStatus(slaRuleElement.getLastChild().getTextContent().trim()));
                jobLrScenarioResult.scenarioSlaResults.add(totalHits);

                break;
            case ErrorsPerSecond:
                TimeRangeResult errPerSec = new AvgTransactionResponseTime();
                errPerSec.setSlaGoal(LrTest.SLA_GOAL.ErrorsPerSecond);
                errPerSec.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                errPerSec.setLoadThrashold(slaRuleElement.getAttribute("SLALoadThresholdValue"));
                errPerSec.setStatus(LrTest.SLA_STATUS.checkStatus(
                        slaRuleElement.getLastChild().getTextContent().trim())); //Might not work due to time ranges
                addTimeRanges(errPerSec, slaRuleElement);
                jobLrScenarioResult.scenarioSlaResults.add(errPerSec);

                break;
            case PercentileTRT:
                PercentileTransactionWholeRun percentileTransactionWholeRun = new PercentileTransactionWholeRun();
                percentileTransactionWholeRun.setSlaGoal(LrTest.SLA_GOAL.PercentileTRT);
                percentileTransactionWholeRun.setName(slaRuleElement.getAttribute("TransactionName"));
                percentileTransactionWholeRun
                        .setActualValue(Double.valueOf(slaRuleElement.getAttribute(SLA_ACTUAL_VALUE_LABEL)));
                percentileTransactionWholeRun.setGoalValue(Double.valueOf(slaRuleElement.getAttribute(
                        SLA_GOAL_VALUE_LABEL)));
                percentileTransactionWholeRun.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                percentileTransactionWholeRun.setPrecentage(Double.valueOf(slaRuleElement.getAttribute("Percentile")));
                percentileTransactionWholeRun.setStatus(LrTest.SLA_STATUS.checkStatus(slaRuleElement.getLastChild().getTextContent().trim()));
                jobLrScenarioResult.scenarioSlaResults.add(percentileTransactionWholeRun);

                break;
            case AverageTRT:
                AvgTransactionResponseTime transactionTimeRange = new AvgTransactionResponseTime();
                transactionTimeRange.setSlaGoal(LrTest.SLA_GOAL.AverageTRT);
                transactionTimeRange.setName(slaRuleElement.getAttribute("TransactionName"));
                transactionTimeRange.setFullName(slaRuleElement.getAttribute(SLA_ULL_NAME));
                transactionTimeRange.setLoadThrashold(slaRuleElement.getAttribute("SLALoadThresholdValue"));
                transactionTimeRange.setStatus(LrTest.SLA_STATUS.checkStatus(
                        slaRuleElement.getLastChild().getTextContent().trim())); //Might not work due to time ranges
                addTimeRanges(transactionTimeRange, slaRuleElement);
                jobLrScenarioResult.scenarioSlaResults.add(transactionTimeRange);
                break;
            case Bad:
                break;
        }
    }

    public void setActionsAndPublishReports(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        if ((runReportList != null) && !(runReportList.isEmpty())) {
            LrJobResults jobDataSet = null;
            try {
                jobDataSet = buildJobDataset(listener);
            } catch (ParserConfigurationException | SAXException e) {
                listener.error(ARCHIVING_TEST_REPORTS_FAILED_DUE_TO_XML_PARSING_ERROR + e);
            }

            if ((jobDataSet != null && !jobDataSet.getLrScenarioResults().isEmpty())) {
                PerformanceJobReportAction performanceJobReportAction = build.getAction(PerformanceJobReportAction.class);
                if (performanceJobReportAction != null) {
                    performanceJobReportAction.mergeResults(jobDataSet);
                } else {
                    performanceJobReportAction = new PerformanceJobReportAction(build, jobDataSet);
                }
                build.replaceAction(performanceJobReportAction);
            }
        }
        publishLrReports(build);
    }
}
