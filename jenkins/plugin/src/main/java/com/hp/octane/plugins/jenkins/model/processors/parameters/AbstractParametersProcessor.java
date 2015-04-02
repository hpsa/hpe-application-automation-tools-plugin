package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import hudson.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 19/02/2015.
 */
public abstract class AbstractParametersProcessor {
	public abstract ParameterConfig createParameterConfig(ParameterDefinition pd);

	public abstract ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv);
}
