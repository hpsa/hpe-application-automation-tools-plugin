package com.hp.octane.plugins.jenkins.configuration;

import com.hp.application.automation.tools.model.OctaneServerSettingsModel;
import com.hp.application.automation.tools.settings.OctaneServerSettingsBuilder;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.Hudson;
import jenkins.model.Jenkins;

@Extension
public class ConfigurationService {


	private static OctaneServerSettingsBuilder.OctaneDescriptorImpl octaneDescriptorImpl;

	public static OctaneServerSettingsModel getModel() {
		return getOctaneDescriptor().getModel();
	}

	public static ServerConfiguration getServerConfiguration() {
		return getOctaneDescriptor().getServerConfiguration();
	}

	public static  void configurePlugin(OctaneServerSettingsModel newModel){
		getOctaneDescriptor().setModel(newModel);
	}

	private static OctaneServerSettingsBuilder.OctaneDescriptorImpl getOctaneDescriptor(){
		if(octaneDescriptorImpl == null){
			octaneDescriptorImpl = Hudson.getInstance().getDescriptorByType(OctaneServerSettingsBuilder.OctaneDescriptorImpl.class);
		}
		return  octaneDescriptorImpl;
	}

	public static String getPluginVersion(){
		Plugin plugin = Jenkins.getInstance().getPlugin("hp-application-automation-tools-plugin");
		return plugin.getWrapper().getVersion();
	}

}
