package com.hpe.application.automation.tools.octane.buildLogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class OctaneLog {

	final private Long fileLength;

	final private InputStream logStream;

	OctaneLog(File logFile) throws FileNotFoundException {
		this.fileLength = logFile.length();
		this.logStream = new FileInputStream(logFile);
	}

	public Long getFileLength() {
		return this.fileLength;
	}

	public InputStream getLogStream() {
		return this.logStream;
	}

}
