package com.hp.nga.integrations.dto.projects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectsList {
	private ProjectConfig[] jobs = new ProjectConfig[0];

	public void setJobs(ProjectConfig[] jobs) {
		this.jobs = jobs == null ? new ProjectConfig[0] : jobs.clone();
	}

	public ProjectConfig[] getJobs() {
		return jobs.clone();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class ProjectConfig {
		private String name;
		private ParameterConfig[] parameters;
		private String id;

		public void setName(String value) {
			name = value;
		}

		public String getName() {
			return name;
		}

		public void setId(String id){
			this.id= id;
		}

		public String getId(){
			return id;
		}

		public void setParameters(ParameterConfig[] parameters) {
			this.parameters = parameters == null ? null : parameters.clone();
		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public ParameterConfig[] getParameters() {
			return parameters == null ? null : parameters.clone();
		}
	}
}
