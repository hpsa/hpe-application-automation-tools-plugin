package com.hp.application.automation.tools.results;


import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
//import hudson.model.ProminentProjectAction;
import hudson.remoting.Channel;

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
 * Created by betzalel on 28/06/2015.
 */

public class HtmlBuildReportAction implements Action {
    //private HtmlPublisherTarget actualHtmlPublisherTarget;

    private static final String REPORTMETADATE_XML = "report_metadata.xml";

    private AbstractBuild build;
    //private BuildListener listener;
    private List<ReportMetaData> reportMetaDataList = new ArrayList<ReportMetaData>();


    //public HtmlBuildReportAction(AbstractBuild<?, ?> build, BuildListener listener, List<ReportMetaData> reportMetaData)
    //NOTE: if parameter has BuildListener, the build cannot be serilize normally.

    public HtmlBuildReportAction(AbstractBuild<?, ?> build) {
        //super(actualHtmlPublisherTarget);
        this.build = build;

        File reportMetaData_XML = new File(build.getArtifactsDir().getParent(), REPORTMETADATE_XML);
        if (reportMetaData_XML.exists())
        {
            readReportFromXMLFile(reportMetaData_XML.getAbsolutePath(),this.reportMetaDataList );
        }

    }

    public final AbstractBuild<?, ?> getBuild() {
        return build;
    }

//    protected String getTitle() {
//        return this.build.getDisplayName() + " UFTReport";
//    }

    protected File reportFile() {
        return getBuildHtmlReport(this.build);
    }

    private File getBuildHtmlReport(Run run) {

        File reportFile = new File(new File(run.getArtifactsDir(), "UFTReport"), "index.html");

        //listener.getLogger().println("HtmlReportFile: " + reportFile.toString());
        return reportFile;
    }

    public String getDisplayName() {
        String displayName = "UFT Report";//this.build.getDisplayName() +
        //return reportFile().exists() ? displayName : null;
        return displayName;
    }

    public String getUrlName() {


        //test
        return "uft-report";
        //testend
//        //String url = this.build.getUrl() + "/UFTReport";
//        String url = "artifact/UFTReport/index.html";
//        return reportFile().exists() ? url : null;
//        //return url;
    }

    public String getIconFileName() {

        //return reportFile().exists() ? "graph.gif" : null;
        return "graph.gif";
    }

    // other property of the report
    public List<ReportMetaData> getAllReports(){
        return this.reportMetaDataList;
    }


    private void readReportFromXMLFile(String filename, List<ReportMetaData> listReport)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            builder = dbf.newDocumentBuilder();
            doc = builder.parse(filename);
        }
        catch (Exception e) {
        }

        Element root = doc.getDocumentElement();
        NodeList reportList = root.getElementsByTagName("report");
        for (int i = 0; i < reportList.getLength(); i++)
        {
            ReportMetaData reportmetadata = new ReportMetaData();
            Element report = (Element) reportList.item(i);
            String disPlayName = report.getAttribute("disPlayName");
            String urlName = report.getAttribute("urlName");
            String resourceURL = report.getAttribute("resourceURL");
            String dateTime = report.getAttribute("dateTime");
            String status = report.getAttribute("status");
            String isHtmlreport = report.getAttribute("isHtmlreport");

            reportmetadata.setDisPlayName(disPlayName);
            reportmetadata.setUrlName(urlName);
            reportmetadata.setResourceURL(resourceURL);
            reportmetadata.setDateTime(dateTime);
            reportmetadata.setStatus(status);
            reportmetadata.setIsHtmlReport( isHtmlreport.equals("true") ? true: false);
            listReport.add(reportmetadata);

        }
    }
//
//    /**
//     * Serves HTML reports.
//     */
//    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
//        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(), "graph.gif", false);
//        dbs.setIndexFileName(HtmlPublisherTarget.this.wrapperName); // Hudson >= 1.312
//        dbs.generateResponse(req, rsp, this);
//    }
}
