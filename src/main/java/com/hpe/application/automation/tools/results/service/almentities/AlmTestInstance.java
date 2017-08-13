package com.hpe.application.automation.tools.results.service.almentities;

public interface AlmTestInstance extends AlmEntity{
	
	public final String TEST_INSTANCE_SUBTYPE_ID = "subtype-id";
	public final String TEST_INSTANCE_EXEC_DATE = "exec-date";
	public final String TEST_INSTANCE_EXEC_TIME = "exec-time";
	public final String TEST_INSTANCE_TESTSET_ID = "cycle-id";
	public final String TEST_INSTANCE_CONFIG_ID= "test-config-id";
	public final String TEST_INSTANCE_TEST_ID = "test-id";
	public final String TEST_INSTANCE_TESTER_NAME = "owner";

	public String getKey();
}
