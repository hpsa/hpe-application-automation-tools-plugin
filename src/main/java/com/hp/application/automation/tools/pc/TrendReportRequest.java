package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

/**
 *
 */
public class TrendReportRequest {

    @SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;

    private String project;
    private int runId;
    private TrendedRange trandedRange;

    public TrendReportRequest(String project, int runId, TrendedRange trandedRange) {
        this.project = project;
        this.runId = runId;
        this.trandedRange = trandedRange;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.useAttributeFor(TrendReportRequest.class, "xmlns");
        xstream.alias("TrendReport", TrendReportRequest.class);
        xstream.aliasField("Project", TrendReportRequest.class, "project");
        xstream.aliasField("RunId", TrendReportRequest.class, "runId");
        xstream.aliasField("TrendedRange", TrendReportRequest.class, "trendedRange");
        return xstream.toXML(this);
    }
}
