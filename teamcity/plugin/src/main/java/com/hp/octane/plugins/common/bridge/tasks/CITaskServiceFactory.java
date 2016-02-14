package com.hp.octane.plugins.common.bridge.tasks;

import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;

/**
 * Created by linsha on 07/01/2016.
 */
public class CITaskServiceFactory {


    public static CITaskService create(String type){
        if(type.equals(ConfigurationService.CLIENT_TYPE)){
            return new TeamcityCITaskService();
        }else {
            return new JenkinsCITaskService();
        }
    }
}
