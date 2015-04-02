package com.hp.octane.plugins.jenkins.model.processors.scm;

/**
 * Created by gullery on 31/03/2015.
 */

public enum SCMProcessors {
	GIT("hudson.plugins.git.GitSCM", GitSCMProcessor.class);

	private String targetPluginClassName;
	private Class<? extends AbstractSCMProcessor> processorClass;

	SCMProcessors(String targetPluginClassName, Class<? extends AbstractSCMProcessor> processorClass) {
		this.targetPluginClassName = targetPluginClassName;
		this.processorClass = processorClass;
	}

	public static AbstractSCMProcessor getAppropriate(String className) {
		for (SCMProcessors p : values()) {
			if (className.startsWith(p.targetPluginClassName))
				try {
					return p.processorClass.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
		}
		return new UnsupportedSCMProcessor();
	}
}
