package com.hp.application.automation.tools.results.service;

import com.hp.application.automation.tools.sse.sdk.Logger;

public class SystemOutLogger implements Logger {

	@Override
	public void log(String message) {
		System.out.println(message);

	}

}
