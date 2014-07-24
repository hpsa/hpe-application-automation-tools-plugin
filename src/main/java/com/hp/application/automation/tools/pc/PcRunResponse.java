package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

public class PcRunResponse extends PcRunRequest {
	
	private int ID;
	
	private int Duration;

	private String RunState;

	private String RunSLAStatus;

	public static PcRunResponse xmlToObject(String xml) {
		XStream xstream = new XStream();
		xstream.setClassLoader(PcRunResponse.class.getClassLoader());
		xstream.alias("Run", PcRunResponse.class);
		return (PcRunResponse) xstream.fromXML(xml);

	}

	public int getID() {
		return ID;
	}

	public int getDuration() {
		return Duration;
	}

	public String getRunState() {
		return RunState;
	}

	public String getRunSLAStatus() {
		return RunSLAStatus;
	}

}
