package com.hp.octane.plugins.common.bridge.tasks;

/**
 * Created by linsha on 07/01/2016.
 */
public class CITaskServiceFactory {


    public static CITaskService create(String type){
        if(type.equals("HPE_TEAMCITY_PLUGIN")){
            return new TeamcityCITaskService();
        }else {
            return new JenkinsCITaskService();
        }
    }
}
