package com.hpe.application.automation.tools.results.service.almentities;

public class AlmTestInstanceImpl extends AlmEntityImpl implements
		AlmTestInstance {
	private static String restPrefix = "test-instances"; 
	public String getRestPrefix() {
		return restPrefix;
	}
	
	public String getKey() {
		
		return getFieldValue(AlmTestInstance.TEST_INSTANCE_TESTSET_ID) + "_"
				+ getFieldValue(AlmTestInstance.TEST_INSTANCE_CONFIG_ID) + "_"
				+ getFieldValue(AlmTestInstance.TEST_INSTANCE_TEST_ID);		
		
	}
}
