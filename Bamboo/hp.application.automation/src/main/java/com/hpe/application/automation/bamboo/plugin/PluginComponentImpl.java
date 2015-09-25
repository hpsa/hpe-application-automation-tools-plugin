package com.hpe.application.automation.bamboo.plugin;

import com.atlassian.sal.api.ApplicationProperties;

public class PluginComponentImpl implements PluginComponent {

	private final ApplicationProperties applicationProperties;

    public PluginComponentImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    private final String pluginName =  "HP Application Automation Tools Plugin";
    
    public String getName()
    {
        if(null != applicationProperties)
        {
            return pluginName + applicationProperties.getDisplayName();
        }
        
        return pluginName;
    }
}
