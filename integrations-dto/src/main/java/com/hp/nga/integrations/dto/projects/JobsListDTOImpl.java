package com.hp.nga.integrations.dto.projects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobsListDTOImpl implements JobsListDTO{
	private ProjectConfig[] jobs = new ProjectConfig[0];

	public void setJobs(ProjectConfig[] jobs) {
		this.jobs = jobs == null ? new ProjectConfig[0] : jobs.clone();
	}

	public ProjectConfig[] getJobs() {
		return jobs.clone();
	}

//	@JsonIgnoreProperties(ignoreUnknown = true)
//	public static final class ProjectConfig {
//		private String name;
//		private ParameterConfig[] parameters;
//		private String ciId;
//
//		public void setName(String value) {
//			name = value;
//		}
//
//		public String getName() {
//			return name;
//		}
//
//		public void setCiId(String ciId){
//			this.ciId= ciId;
//		}
//
//		public String getCiId(){
//			return ciId;
//		}
//
//		public void setParameters(ParameterConfig[] parameters) {
//			this.parameters = parameters == null ? null : parameters.clone();
//		}
//
//		@JsonInclude(JsonInclude.Include.NON_NULL)
//		public ParameterConfig[] getParameters() {
//			return parameters == null ? null : parameters.clone();
//		}
//	}
}
