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

package com.hpe.application.automation.tools.octane.actions.project;

import hudson.Extension;
import hudson.model.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */

@Extension
final public class CustomProject extends Project<FreeStyleProject, FreeStyleBuild> implements TopLevelItem {

	@Extension(ordinal = 1000)
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends AbstractProject.AbstractProjectDescriptor {
		public String getDisplayName() {
			return "Custom Project";
		}

		public CustomProject newInstance(ItemGroup itemGroup, String name) {
			return new CustomProject(itemGroup, name);
		}
	}

	public CustomProject() {
		super(null, null);
	}

	public CustomProject(ItemGroup group, String name) {
		super(group, name);
	}

	@Override
	public TopLevelItemDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	protected Class<FreeStyleBuild> getBuildClass() {
		return FreeStyleBuild.class;
	}
}