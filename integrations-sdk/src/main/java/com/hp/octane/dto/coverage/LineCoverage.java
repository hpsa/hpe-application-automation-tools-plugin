package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineCoverage {
	private int number;
	private int count;

	public LineCoverage() {
	}

	public LineCoverage(int number, int count) {
		this.number = number;
		this.count = count;
	}

	@JsonProperty("n")
	public int getNumber() {
		return number;
	}

	@JsonProperty("n")
	public void setNumber(int number) {
		this.number = number;
	}

	@JsonProperty("c")
	public int getCount() {
		return count;
	}

	@JsonProperty("c")
	public void setCount(int count) {
		this.count = count;
	}
}
