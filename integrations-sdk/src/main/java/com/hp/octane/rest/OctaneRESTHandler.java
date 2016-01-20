package com.hp.octane.rest;

/**
 * Created by gullery on 07/01/2016.
 */

public class OctaneRESTHandler {
	private static final OctaneRESTHandler instance = new OctaneRESTHandler();

	private OctaneRESTHandler() {
	}

	public static OctaneRESTHandler getInstance() {
		return instance;
	}

	public OctaneResponse handle(OctaneRequest request) {
		OctaneResponse response = null;
		return response;
	}

	private void doRoute(OctaneRequest request) {
		String[] pathNodes = request.getUrl().split("/");
	}
}
