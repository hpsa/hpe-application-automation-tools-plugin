package com.hp.application.automation.tools.sse.common;

import com.hp.application.automation.tools.rest.RestClient;

public class RestClient4Test extends RestClient {
	private final static String AUTH_INFO_FRONT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><AuthenticationInfo><Username>";
	private final static String AUTH_INFO_BACK = "</Username></AuthenticationInfo>";

	public RestClient4Test(String url, String domain, String project, String username) {
		super(url, domain, project, username);
	}

    public byte[] getExpectAuthInfo() {
    	
    	String authInfo = AUTH_INFO_FRONT + getUsername() + AUTH_INFO_BACK;
		
    	return  authInfo.getBytes();
	}
}
