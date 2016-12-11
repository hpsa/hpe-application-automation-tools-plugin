package com.hp.octane.integrations.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.coverage.FileCoverage;
import com.hp.octane.integrations.dto.coverage.LineCoverage;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class FileCoverageImpl implements FileCoverage {
	private String file;
	private LineCoverage[] lines;

	public String getFile() {
		return file;
	}

	public FileCoverage setFile(String file) {
		this.file = file;
		return this;
	}

	public LineCoverage[] getLines() {
		return lines;
	}

	public FileCoverage setLines(LineCoverage[] lines) {
		this.lines = lines;
		return this;
	}
}
