package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;
import com.hp.application.automation.tools.model.TimeslotDuration;

public class PcRunRequest {

	@SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;

	private int TestID;

	private int TestInstanceID;

	private int TimeslotID;

	private TimeslotDuration TimeslotDuration;

	private String PostRunAction;

	private boolean VudsMode;
		

	public PcRunRequest(
			int testID,
			int testInstanceID,
			int timeslotID,
			TimeslotDuration timeslotDuration,
			String postRunAction,
			boolean vudsMode) {
		
		TestID = testID;
		TestInstanceID = testInstanceID;
		TimeslotID = timeslotID;
		TimeslotDuration = timeslotDuration;
		PostRunAction = postRunAction;
		VudsMode = vudsMode;
	}
	
	public PcRunRequest() {}

	public String objectToXML() {
		XStream obj = new XStream();
		obj.alias("Run", PcRunRequest.class);
		obj.alias("TimeslotDuration", TimeslotDuration.class);
		obj.useAttributeFor(PcRunRequest.class, "xmlns");
		return obj.toXML(this);
	}
	
	public int getTestID() {
		return TestID;
	}
	
	public int getTestInstanceID() {
		return TestInstanceID;
	}
	
	public int getTimeslotID() {
		return TimeslotID;
	}
	
	public TimeslotDuration getTimeslotDuration() {
		return TimeslotDuration;
	}
	
	public String getPostRunAction() {
		return PostRunAction;
	}
	
	public boolean isVudsMode() {
		return VudsMode;
	}
}
