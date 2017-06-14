package com.hpe.application.automation.tools.results.service.almentities;

public class AlmTestConfigImpl extends AlmEntityImpl implements AlmTestConfig {

	private static String restPrefix = "test-configs"; 
	public String getRestPrefix() {
		return restPrefix;
	}
}
