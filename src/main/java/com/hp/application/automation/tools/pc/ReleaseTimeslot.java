package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

public class ReleaseTimeslot {

	@SuppressWarnings("unused")
	private String xmlns = PcRestProxy.PC_API_XMLNS;

	private boolean ReleaseTimeslot;

	private String PostRunAction;

	public ReleaseTimeslot(boolean releaseTimeslot, String postRunAction) {
		ReleaseTimeslot = releaseTimeslot;
		PostRunAction = postRunAction;
	}

	public String objectToXML() {
		XStream obj = new XStream();
		obj.alias("PostRunActions", ReleaseTimeslot.class);
		obj.useAttributeFor(ReleaseTimeslot.class, "xmlns");
		return obj.toXML(this);
	}

	public boolean isReleaseTimeslot() {
		return ReleaseTimeslot;
	}

	public String getPostRunAction() {
		return PostRunAction;
	}

	
}