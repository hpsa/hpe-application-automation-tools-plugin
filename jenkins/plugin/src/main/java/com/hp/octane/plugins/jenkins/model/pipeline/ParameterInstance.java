package com.hp.octane.plugins.jenkins.model.pipeline;

import hudson.model.BooleanParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 11/01/15
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class ParameterInstance extends ParameterConfig {
	private Object value;

	public ParameterInstance(ParameterValue value, ParameterConfig config) {
		super(config);
		this.value = value.getValue();
	}

	@Exported(inline = true)
	public Object getValue() {
		return value;
	}
}
