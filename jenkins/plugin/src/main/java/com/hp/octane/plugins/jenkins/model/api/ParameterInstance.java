package com.hp.octane.plugins.jenkins.model.api;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created by gullery on 19/02/2015.
 */

@ExportedBean
public class ParameterInstance extends ParameterConfig {
	private Object value;

	public ParameterInstance(ParameterConfig pc, Object value) {
		super(pc);
		this.value = value;
	}

	@Exported(inline = true)
	public Object getValue() {
		return value;
	}
}
