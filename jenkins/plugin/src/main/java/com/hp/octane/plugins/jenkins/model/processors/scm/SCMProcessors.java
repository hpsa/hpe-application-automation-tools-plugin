package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.plugins.jenkins.OctanePlugin;

import java.util.logging.Logger;

/**
 * Created by gullery on 31/03/2015.
 */

public enum SCMProcessors {
	GIT("hudson.plugins.git.GitSCM", GitSCMProcessor.class);

	private static Logger logger = Logger.getLogger(OctanePlugin.class.getName());
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
				} catch (InstantiationException ie) {
					logger.severe("failed to instantiate SCM processor of type '" + p.targetSCMPluginClassName + "'; error: " + ie.getMessage());
				} catch (IllegalAccessException iae) {
					logger.severe("failed to instantiate SCM processor of type '" + p.targetSCMPluginClassName + "'; error: " + iae.getMessage());
				}
		}
		return result;
	}
}
