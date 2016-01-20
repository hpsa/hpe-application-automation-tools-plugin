package com.hp.nga.rest;

/**
 * Created by gullery on 07/01/2016.
 */

public class NGARestHandlersome {
	private static final NGARestHandlersome instance = new NGARestHandlersome();

	private NGARestHandlersome() {
	}

	public static NGARestHandlersome getInstance() {
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
