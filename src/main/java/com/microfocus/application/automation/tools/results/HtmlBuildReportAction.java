/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.results;

import hudson.model.Action;
import hudson.model.Run;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;


/**
 * Created by betzalel on 28/06/2015.
 */
public class HtmlBuildReportAction implements Action {
    private Run build;
    private List<ReportMetaData> reportMetaDataList;
    private Integer index;

    //NOTE: if parameter has BuildListener, the build cannot be serialize normally.
    public HtmlBuildReportAction(Run<?, ?> build, String reportName, Integer index) throws IOException, SAXException, ParserConfigurationException {
        this.build = build;

        File reportMetaData_XML = new File(build.getRootDir(), reportName);
        if (reportMetaData_XML.exists()) {
            this.reportMetaDataList = readReportFromXMLFile(reportMetaData_XML.getAbsolutePath());
        }
        this.index = index;
    }


	@SuppressWarnings("squid:S1452")
    public final Run<?, ?> getBuild() {
        return build;
    }

    protected File reportFile() {
        return getBuildHtmlReport(this.build);
    }

    private File getBuildHtmlReport(Run run) {
		return new File(new File(new File(run.getRootDir(),"archive"), "UFTReport"), "index.html");
    }

    @Override
    public String getDisplayName() {
		return "UFT Report";
    }

    @Override
    public String getUrlName() {
        return "uft-report" + (this.index != null ? "-" + this.index : "");
    }

    @Override
    public String getIconFileName() {
		return "/plugin/hp-application-automation-tools-plugin/icons/24x24/uft_report.png";
    }

    // other property of the report
    public List<ReportMetaData> getAllReports() {
        return reportMetaDataList;
    }

    public List<ReportMetaData> getReportMetaDataList(){
        return this.reportMetaDataList;
    }


   private List<ReportMetaData> readReportFromXMLFile(String filename) throws ParserConfigurationException, IOException, SAXException {
        List<ReportMetaData> listReport = new ArrayList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(filename);

        Element root = doc.getDocumentElement();
        NodeList reportList = root.getElementsByTagName("report");
        for (int i = 0; i < reportList.getLength(); i++) {
            ReportMetaData reportmetadata = new ReportMetaData();
            Node nNode = reportList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element report = (Element) nNode;
                String disPlayName = report.getAttribute("disPlayName");
                String urlName = report.getAttribute("urlName");
                String resourceURL = report.getAttribute("resourceURL");
                String dateTime = report.getAttribute("dateTime");
                String status = report.getAttribute("status");
                String isHtmlreport = report.getAttribute("isHtmlreport");
                String isParallelRunnerReport = report.getAttribute("isParallelRunnerReport");
                String archiveUrl = report.getAttribute("archiveUrl");

                reportmetadata.setDisPlayName(disPlayName);
                reportmetadata.setUrlName(urlName);
                reportmetadata.setResourceURL(resourceURL);
                reportmetadata.setDateTime(dateTime);
                reportmetadata.setStatus(status);
                reportmetadata.setIsHtmlReport("true".equals(isHtmlreport));
                reportmetadata.setIsParallelRunnerReport("true".equals(isParallelRunnerReport));
                reportmetadata.setArchiveUrl(archiveUrl);
                listReport.add(reportmetadata);
            }
        }

        return listReport;
    }



}
