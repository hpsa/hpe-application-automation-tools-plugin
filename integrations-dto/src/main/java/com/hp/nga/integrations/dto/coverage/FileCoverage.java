package com.hp.nga.integrations.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileCoverage {
	private String file;
	private LineCoverage[] lines = new LineCoverage[0];

	public FileCoverage() {
	}

	public FileCoverage(String file, LineCoverage[] lines) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("file MUST NOT be null nor empty");
		}
		if (lines == null) {
			throw new IllegalArgumentException("covered lines MUST NOT be null");
		}

		this.file = file;
		this.lines = lines.clone();
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public LineCoverage[] getLines() {
		return lines.clone();
	}

	public void setLines(LineCoverage[] lines) {
		this.lines = lines == null ? new LineCoverage[0] : lines.clone();
	}
}
