package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;

public class AlmServerCapabilityHelper {
	
	public static final String CAPABILITY_PREFIX = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".hpAlmServer";
	
	public static String GetCapabilityKey(String name)
	{
		return CAPABILITY_PREFIX + "." + name;
	}
	
}
