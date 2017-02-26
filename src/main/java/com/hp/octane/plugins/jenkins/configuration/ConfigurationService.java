package com.hp.octane.plugins.jenkins.configuration;

import com.hp.application.automation.tools.model.OctaneServerSettingsModel;
import com.hp.application.automation.tools.settings.OctaneServerSettingsBuilder;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.Hudson;
import jenkins.model.Jenkins;

@Extension
public class ConfigurationService {

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
		return  Hudson.getInstance().getDescriptorByType(OctaneServerSettingsBuilder.OctaneDescriptorImpl.class);
	}

	public static String getPluginVersion(){
		Plugin plugin = Jenkins.getInstance().getPlugin("hp-application-automation-tools-plugin");
		return plugin.getWrapper().getVersion();
	}

}
