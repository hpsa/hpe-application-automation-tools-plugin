package com.hp.octane.plugins.jenkins.model.parameters;

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

	public ParameterInstance(ParameterConfig config) {
		super(config);
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Exported(inline = true)
	public Object getValue() {
		return value;
	}
}
