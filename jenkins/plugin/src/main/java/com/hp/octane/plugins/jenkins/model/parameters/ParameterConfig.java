package com.hp.octane.plugins.jenkins.model.parameters;

import hudson.model.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

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
	private List<String> choices;

	public ParameterConfig(ParameterDefinition pd) {
		name = pd.getName();
		description = pd.getDescription();
		if (pd instanceof SimpleParameterDefinition) {
			if (pd instanceof BooleanParameterDefinition) {
				BooleanParameterDefinition booleanPd = (BooleanParameterDefinition) pd;
				type = ParameterType.BOOLEAN;
				defaultValue = booleanPd.getDefaultParameterValue().getValue();
			} else if (pd instanceof TextParameterDefinition) {
				TextParameterDefinition textPd = (TextParameterDefinition) pd;
				type = ParameterType.STRING;
				defaultValue = textPd.getDefaultParameterValue().getValue();
			} else if (pd instanceof StringParameterDefinition) {
				StringParameterDefinition stringPd = (StringParameterDefinition) pd;
				type = ParameterType.STRING;
				defaultValue = stringPd.getDefaultParameterValue().getValue();
			} else if (pd instanceof ChoiceParameterDefinition) {
				ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
				type = ParameterType.STRING;
				defaultValue = choicePd.getDefaultParameterValue().getValue();
				choices = choicePd.getChoices();
			}
		} else if (pd instanceof FileParameterDefinition) {
			type = ParameterType.FILE;
			defaultValue = "";
		} else {
			type = ParameterType.UNAVAILABLE;
		}
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

	@Exported(inline = true)
	public List<String> getChoices() {
		return choices;
	}
}
