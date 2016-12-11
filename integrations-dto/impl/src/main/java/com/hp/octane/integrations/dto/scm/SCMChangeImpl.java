package com.hp.octane.integrations.dto.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.scm.SCMChange;

/**
 * Created by gullery on 08/12/2015.
 * SCM Change descriptor
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SCMChangeImpl implements SCMChange {
	private String type;
	private String file;

	public String getType() {
		return type;
	}

	public SCMChange setType(String type) {
		this.type = type;
		return this;
	}

	public String getFile() {
		return file;
	}

	public SCMChange setFile(String file) {
		this.file = file;
		return this;
	}
}
