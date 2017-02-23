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

package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.matrix.MatrixProject;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */

class MatrixProjectProcessor extends AbstractProjectProcessor<MatrixProject> {

	MatrixProjectProcessor(Job project) {
		super((MatrixProject) project);

		//  Internal phases
		//
		super.processBuilders(this.job.getBuilders(), this.job);

		//  Post build phases
		//
		super.processPublishers(this.job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return job.getBuilders();
	}

	@Override
	public void scheduleBuild(String parametersBody) {
		throw new RuntimeException("non yet implemented");
	}
}
