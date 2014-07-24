package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

public class PcErrorResponse {

	@SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;
	
	public String ExceptionMessage;
	
	public int ErrorCode;
		
	public PcErrorResponse(String exceptionMessage, int errorCode) {
		ExceptionMessage = exceptionMessage;
		ErrorCode = errorCode;
	}

	public static PcErrorResponse xmlToObject(String xml) {
		XStream xstream = new XStream();
		xstream.setClassLoader(PcErrorResponse.class.getClassLoader());
		xstream.alias("Exception", PcErrorResponse.class);
		return (PcErrorResponse) xstream.fromXML(xml);

	}
	
}
