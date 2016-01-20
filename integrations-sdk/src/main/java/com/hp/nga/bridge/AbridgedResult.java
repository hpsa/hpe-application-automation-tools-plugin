package com.hp.nga.bridge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.rest.NGAResponse;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbridgedResult extends NGAResponse {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
