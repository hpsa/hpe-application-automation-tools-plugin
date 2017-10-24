package com.hpe.application.automation.tools.results.parser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hpe.application.automation.tools.results.parser.antjunit.AntJUnitReportParserImpl;
import com.hpe.application.automation.tools.results.parser.jenkinsjunit.JenkinsJUnitReportParserImpl;
import com.hpe.application.automation.tools.results.parser.mavensurefire.MavenSureFireReportParserImpl;
import com.hpe.application.automation.tools.results.parser.nunit.NUnitReportParserImpl;
import com.hpe.application.automation.tools.results.parser.testngxml.TestNGXmlReportParserImpl;
import com.hpe.application.automation.tools.results.service.almentities.AlmTestSet;

public class ReportParserManager {
	
	private static ReportParserManager instance = new ReportParserManager();
	
	List<ReportParser> parserList = new ArrayList<ReportParser> ();
	
	private ReportParserManager() {
		parserList.add(new JenkinsJUnitReportParserImpl());
		parserList.add(new MavenSureFireReportParserImpl());
		parserList.add(new TestNGXmlReportParserImpl());
		parserList.add(new NUnitReportParserImpl());
		parserList.add(new AntJUnitReportParserImpl());
	}
	
	public static ReportParserManager getInstance() {
		return instance;
	}
	
	public List<AlmTestSet> parseTestSets(String reportFilePath, String testingFramework, String testingTool) {
		List<AlmTestSet> testsets = null;

		for(ReportParser reportParser : parserList) {
			try {
				InputStream in = new FileInputStream(reportFilePath);

				testsets = reportParser.parseTestSets(in, testingFramework, testingTool);
				break;
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
		}

		return testsets;
	}


}
