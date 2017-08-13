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

package com.hpe.application.automation.tools.octane.model.processors.projects;

import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */

class UnsupportedProjectProcessor extends AbstractProjectProcessor<Job> {
	UnsupportedProjectProcessor(Job job) {
		super(job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return new ArrayList<>();
	}

	@Override
	public void scheduleBuild(String parametersBody) {
		throw new IllegalStateException("unsupported job MAY NOT be run");
	}
}