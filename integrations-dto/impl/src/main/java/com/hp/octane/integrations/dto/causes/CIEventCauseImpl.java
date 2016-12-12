package com.hp.octane.integrations.dto.causes;

import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 16/02/2016.
 */

class CIEventCauseImpl implements CIEventCause {
	private CIEventCauseType type;
	private String user;
	private String project;
	private String buildCiId;
	private List<CIEventCause> causes = new ArrayList<>();

	public CIEventCauseType getType() {
		return type;
	}

	public CIEventCause setType(CIEventCauseType type) {
		this.type = type;
		return this;
	}

	public String getUser() {
		return user;
	}

	public CIEventCause setUser(String user) {
		this.user = user;
		return this;
	}

	public String getProject() {
		return project;
	}

	public CIEventCause setProject(String ciJobRefId) {
		this.project = ciJobRefId;
		return this;
	}

	public String getBuildCiId() {
		return buildCiId;
	}

	public CIEventCause setBuildCiId(String buildCiId) {
		this.buildCiId = buildCiId;
		return this;
	}

	public List<CIEventCause> getCauses() {
		return causes;
	}

	public CIEventCause setCauses(List<CIEventCause> causes) {
		this.causes = causes;
		return this;
	}
}
