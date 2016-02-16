package com.hp.nga.integrations.dto.coverage.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.nga.integrations.dto.coverage.LineCoverage;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class LineCoverageImpl implements LineCoverage {
	private Integer number;
	private Integer count;

	@JsonProperty("n")
	public Integer getNumber() {
		return number;
	}

	@JsonProperty("n")
	public LineCoverage setNumber(int number) {
		this.number = number;
		return this;
	}

	@JsonProperty("c")
	public Integer getCount() {
		return count;
	}

	@JsonProperty("c")
	public LineCoverage setCount(int count) {
		this.count = count;
		return this;
	}
}
