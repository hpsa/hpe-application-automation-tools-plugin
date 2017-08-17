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

package com.hpe.application.automation.tools.octane.actions;

import hudson.model.AbstractProject;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */

public class Utils {
	public static CommandInterpreter getSleepScript(int seconds) {
		if (Configuration.OS.getCurrent() == Configuration.OS.WINDOWS) {
			return new BatchFile("ping -n " + seconds + " 127.0.0.1 >nul");
		} else if (Configuration.OS.getCurrent() == Configuration.OS.LINUX) {
			return new Shell("sleep " + seconds);
		} else {
			return null;
		}
	}

	public static void buildProject(JenkinsRule.WebClient client, AbstractProject project) throws IOException, SAXException {
		client.goTo("job/" + project.getName() + "/build", "");
	}

	public static void buildProjectWithParams(JenkinsRule.WebClient client, AbstractProject project, String params) throws IOException, SAXException {
		client.goTo("job/" + project.getName() + "/buildWithParameters?" + params, "");
	}
}
