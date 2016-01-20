package com.hp.nga.integrations.bridge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.rest.NGARequest;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbridgedTask extends NGARequest {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
