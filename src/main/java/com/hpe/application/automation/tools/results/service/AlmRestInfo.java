package com.hpe.application.automation.tools.results.service;

public class AlmRestInfo {
	
	private String serverUrl;
	private String userName;
	private String password;
	private String domain;
	private String project;
	private String timeout;
	
	public AlmRestInfo(String serverUrl, String domain, String project,  String userName, String password, String timeout) {
		this.serverUrl = serverUrl;
		this.userName = userName;
		this.password = password;
		this.domain = domain;
		this.project = project;
		this.timeout = timeout;
		
	}
	
	public String getServerUrl(){
		return serverUrl;
	}
	
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDomain(){
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getProject(){
		return project;
	}

	public void setProject(String project){
		this.project = project;
	}
	
	public String getTimeout() {
		return timeout;
	}
	
	public void setTimeout(String timeout){
		this.timeout = timeout;
	}
}
