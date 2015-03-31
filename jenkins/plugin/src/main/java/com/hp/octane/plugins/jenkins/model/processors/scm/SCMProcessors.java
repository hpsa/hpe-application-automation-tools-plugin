package com.hp.octane.plugins.jenkins.model.processors.scm;

/**
 * Created by gullery on 31/03/2015.
 */
public enum SCMProcessors {
	GIT(new GitSCMProcessor()),
	UNSUPPORTED(new UnsupportedSCMProcessor());

	private AbstractSCMProcessor processor;

	SCMProcessors(AbstractSCMProcessor processor) {
		this.processor = processor;
	}

	public static AbstractSCMProcessor getAppropriate(String className) {
		for (SCMProcessors p : values()) {
			if (p.processor.isAppropriate(className)) {
				return p.processor;
			}
		}
		return UNSUPPORTED.processor;
	}
}
