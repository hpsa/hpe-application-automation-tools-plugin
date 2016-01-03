package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileCoverage {
	private String file;
	private int[] lines = new int[0];

	public FileCoverage() {
	}

	public FileCoverage(String file, int[] lines) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("file MUST NOT be null nor empty");
		}
		if (lines == null) {
			throw new IllegalArgumentException("coverage lines MUST NOT be null");
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

	public int[] getLines() {
		return lines.clone();
	}

	public void setLines(int[] lines) {
		this.lines = lines == null ? new int[0] : lines.clone();
	}
}
