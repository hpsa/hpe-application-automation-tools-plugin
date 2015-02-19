package com.hp.octane.plugins.jenkins.model.api;

import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created by gullery on 19/02/2015.
 */

@ExportedBean
public class AbstractParameter {
	private String name;
	private String description;
	private ParameterType type;
	private Object defaultValue;



	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public String getDescription() {
		return description;
	}

	@Exported(inline = true)
	public ParameterType getType() {
		return type;
	}

	@Exported(inline = true)
	public Object getDefaultValue() {
		return defaultValue;
	}
}
