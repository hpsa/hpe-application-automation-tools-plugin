package com.hp.octane.dto.bridge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.rest.OctaneRequest;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbridgedTask extends OctaneRequest {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
