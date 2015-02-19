package com.hp.octane.plugins.jenkins.model.api;

import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

/**
 * Created by gullery on 19/02/2015.
 */

@ExportedBean
public class ParameterConfig {
	private String name;
	private ParameterType type;
	private String description;
	private Object defaultValue;
	private List<Object> choices;

	public ParameterConfig(ParameterDefinition pd) {
		this(pd, ParameterType.UNAVAILABLE, null);
	}

	public ParameterConfig(ParameterDefinition pd, ParameterType type) {
		this(pd, type, null);
	}

	public ParameterConfig(ParameterDefinition pd, ParameterType type, List<Object> choices) {
		ParameterValue tmp;
		this.name = pd.getName();
		this.type = type;
		this.description = pd.getDescription();
		if (type != ParameterType.UNAVAILABLE) {
			tmp = pd.getDefaultParameterValue();
			this.defaultValue = tmp == null ? "" : tmp.getValue();
			this.choices = choices;
		}
	}

	public ParameterConfig(ParameterConfig pc) {
		this.name = pc.getName();
		this.type = ParameterType.getByValue(pc.getType());
		this.description = pc.getDescription();
		this.defaultValue = pc.getDefaultValue();
		this.choices = pc.getChoices();
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

	@Exported(inline = true)
	public List<Object> getChoices() {
		return choices;
	}
}
