/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.model.processors.parameters;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hpe.application.automation.tools.octane.model.ModelFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;

import java.util.ArrayList;

/**
 * Created by gullery on 19/02/2015.
 */

public class NodeLabelParameterProcessor extends AbstractParametersProcessor {
    NodeLabelParameterProcessor() {
    }

    @Override
    public CIParameter createParameterConfig(ParameterDefinition pd) {
        if (pd instanceof NodeParameterDefinition) {
            NodeParameterDefinition nodePd = (NodeParameterDefinition) pd;
            return ModelFactory.createParameterConfig(pd, CIParameterType.STRING, new ArrayList<Object>(nodePd.allowedSlaves));
        } else if (pd instanceof LabelParameterDefinition) {
            LabelParameterDefinition labelPd = (LabelParameterDefinition) pd;
            return ModelFactory.createParameterConfig(pd, CIParameterType.STRING);
        } else {
            return ModelFactory.createParameterConfig(pd);
        }
    }

    @Override
    public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
        Object value = null;
        if (pv instanceof NodeParameterValue) {
            value = ((NodeParameterValue) pv).getLabel();
        } else if (pv instanceof LabelParameterValue) {
            value = ((LabelParameterValue) pv).getLabel();
        }
        return ModelFactory.createParameterInstance(createParameterConfig(pd), value);
    }
}
