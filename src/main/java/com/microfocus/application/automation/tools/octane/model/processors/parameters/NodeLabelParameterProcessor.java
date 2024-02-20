/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.model.processors.parameters;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.microfocus.application.automation.tools.octane.model.ModelFactory;
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
