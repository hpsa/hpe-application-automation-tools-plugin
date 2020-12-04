/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.configuration;

import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.settings.OctaneServerSettingsBuilder;
import hudson.Plugin;
import jenkins.model.Jenkins;

import java.util.Arrays;
import java.util.List;

/***
 * Octane plugin configuration service -
 * 1. helps to change Octane configuration
 * 2. helps to get Octane configuration and model
 */
public class ConfigurationService {

	/**
	 * provides all available configurations
	 *
	 * @return list of all available configurations
	 */
	public static List<OctaneServerSettingsModel> getAllSettings() {
		return Arrays.asList(OctaneServerSettingsBuilder.getOctaneSettingsManager().getServers());
	}

	/**
	 * Get current {@see OctaneServerSettingsModel} model
	 *
	 * @return current configuration
	 */
	public static OctaneServerSettingsModel getSettings(String instanceId) {
		return OctaneServerSettingsBuilder.getOctaneSettingsManager().getSettings(instanceId);
	}

	/**
	 * Change model (used by tests)
	 *
	 * @param newModel new configuration
	 */
	public static void configurePlugin(OctaneServerSettingsModel newModel) {
		OctaneServerSettingsBuilder.getOctaneSettingsManager().setModel(newModel);
	}

	/**
	 * Get plugin version
	 *
	 * @return plugin version
	 */
	public static String getPluginVersion() {
		Plugin plugin = Jenkins.getInstanceOrNull().getPlugin("hp-application-automation-tools-plugin");
		if(plugin == null){
			return "na";
		}
		return plugin.getWrapper().getVersion();
	}
}
