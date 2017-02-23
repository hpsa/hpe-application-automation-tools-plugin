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

package com.hp.octane.plugins.jenkins.tests.build;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.Extension;
import hudson.model.AbstractBuild;

import java.util.List;

@Extension
public class MatrixBuildExtension extends BuildHandlerExtension {

	@Override
	public boolean supports(AbstractBuild<?, ?> build) {
		return "hudson.matrix.MatrixRun".equals(build.getClass().getName());
	}

	@Override
	public BuildDescriptor getBuildType(AbstractBuild<?, ?> build) {
		List<CIParameter> parameters = ParameterProcessors.getInstances(build);
		String subBuildName = ModelFactory.generateSubBuildName(parameters);
		return new BuildDescriptor(
				build.getRootBuild().getProject().getName(),
				build.getRootBuild().getProject().getName(),
				String.valueOf(build.getNumber()),
				String.valueOf(build.getNumber()),
				subBuildName);
	}

	@Override
	public String getProjectFullName(AbstractBuild<?, ?> build) {
		return build.getRootBuild().getProject().getName() + "/" + build.getProject().getName();
	}
}
