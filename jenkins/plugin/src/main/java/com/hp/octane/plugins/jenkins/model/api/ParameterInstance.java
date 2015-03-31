package com.hp.octane.plugins.jenkins.model.api;

import hudson.model.ParameterValue;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nonnull;

/**
 * Created by gullery on 19/02/2015.
 */

@ExportedBean
public class ParameterInstance extends ParameterConfig {
	private Object value;

	public ParameterInstance(ParameterConfig pc) {
		super(pc);
	}

	public ParameterInstance(ParameterConfig pc, String value) {
		super(pc);
		this.value = value;
	}

	public ParameterInstance(ParameterConfig pc, ParameterValue value) {
		super(pc);
		this.value = value == null ? null : value.getValue();
	}

	@Exported(inline = true)
	public Object getValue() {
		return value;
	}
}
