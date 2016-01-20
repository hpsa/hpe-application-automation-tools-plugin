package com.hp.nga.rest;

/**
 * Created by gullery on 07/01/2016.
 */

public class NGARestHandlerasd {
	private static final NGARestHandlerasd instance = new NGARestHandlerasd();

	private NGARestHandlerasd() {
	}

	public static NGARestHandlerasd getInstance() {
		return instance;
	}

	public NGAResponse handle(NGARequest request) {
		NGAResponse response = null;
		return response;
	}

	private void doRoute(NGARequest request) {
		String[] pathNodes = request.getUrl().split("/");
	}
}
