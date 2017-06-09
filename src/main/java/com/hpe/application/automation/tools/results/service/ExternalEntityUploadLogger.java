package com.hpe.application.automation.tools.results.service;

import java.io.PrintStream;

import com.hpe.application.automation.tools.sse.sdk.Logger;

public class ExternalEntityUploadLogger implements Logger {
	
	private PrintStream printStream;

	public ExternalEntityUploadLogger(PrintStream printStream) {
		this.printStream = printStream;
	}
	@Override
	public void log(String message) {
		if(printStream != null) {
			printStream.println(message);
		}
	}

}
