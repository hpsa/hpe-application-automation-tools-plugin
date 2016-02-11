package com.hp.nga.integrations.dto.scm.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by gullery on 08/12/2015.
 * SCM Change descriptor
 */

//  [YG]    TODO: check the usages of this class in NGA

@JsonIgnoreProperties(ignoreUnknown = true)
public class SCMChange implements Serializable {
	private String type;
	private String file;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
