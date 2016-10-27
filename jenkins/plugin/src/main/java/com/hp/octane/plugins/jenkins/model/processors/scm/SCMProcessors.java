package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.plugins.jenkins.OctanePlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 31/03/2015.
 */

public enum SCMProcessors {
	GIT("hudson.plugins.git.GitSCM", GitSCMProcessor.class),
	SVN("hudson.scm.SubversionSCM", SvnSCMProcessor.class);

	private static Logger logger = LogManager.getLogger(OctanePlugin.class);
	private String targetSCMPluginClassName;
	private Class<? extends SCMProcessor> processorClass;

	SCMProcessors(String targetSCMPluginClassName, Class<? extends SCMProcessor> processorClass) {
		this.targetSCMPluginClassName = targetSCMPluginClassName;
		this.processorClass = processorClass;
	}

	public static SCMProcessor getAppropriate(String className) {
		SCMProcessor result = null;
		for (SCMProcessors p : values()) {
			if (className.startsWith(p.targetSCMPluginClassName))
				try {
					result = p.processorClass.newInstance();
					break;
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error("failed to instantiate SCM processor of type '" + p.targetSCMPluginClassName, e);
				}
		}

		if (result == null) {
			result = getGenericSCMProcessor(className);
		}

		return result;
	}

	private static SCMProcessor getGenericSCMProcessor(String className) {
		SCMProcessor genericSCMProcessor = null;
		try {
			genericSCMProcessor = (GenericSCMProcessor.class).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("failed to instantiate SCM processor of type '" + className, e);
		}
		return genericSCMProcessor;
	}
}
