package com.hp.octane.plugins.jenkins.model.pipeline;

import hudson.model.*;
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
public class ParameterConfig {
	private String name;
	private ParameterType type;
	private String description;
	private Object defaultValue;

	public ParameterConfig(ParameterDefinition pb) {
		name = pb.getName();
		if (pb instanceof SimpleParameterDefinition) {
			if (pb instanceof BooleanParameterDefinition) {
				type = ParameterType.BOOLEAN;
			} else {
				type = ParameterType.STRING;
			}
		}
		description = pb.getDescription();
		defaultValue = pb.getDefaultParameterValue().getValue();
	}

	public ParameterConfig(ParameterConfig config) {
		name = config.getName();
		type = ParameterType.getByValue(config.getType());
		description = config.getDescription();
		defaultValue = config.getDefaultValue();
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public String getType() {
		return type.toString();
	}

	@Exported(inline = true)
	public String getDescription() {
		return description;
	}

	@Exported(inline = true)
	public Object getDefaultValue() {
		return defaultValue;
	}
}
