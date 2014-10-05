package com.hp.application.automation.tools.pc;

import java.util.ArrayList;
import com.thoughtworks.xstream.XStream;

public class PcRunResults {

	private ArrayList<PcRunResult> ResultsList;

	public PcRunResults() {
		ResultsList = new ArrayList<PcRunResult>();
	}
		
	public static PcRunResults xmlToObject(String xml)
    {   	  
  	  XStream xstream = new XStream();
  	  xstream.alias("RunResult" , PcRunResult.class);
  	  xstream.alias("RunResults" , PcRunResults.class);
  	  xstream.addImplicitCollection(PcRunResults.class, "ResultsList");
  	  xstream.setClassLoader(PcRunResults.class.getClassLoader());
  	  return (PcRunResults)xstream.fromXML(xml);	
    }

	public ArrayList<PcRunResult> getResultsList() {
		return ResultsList;
	}
}