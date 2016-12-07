package com.hp.mqm.atrf.octane.services;


/**
 */
public interface OctaneRestConstants {

    String AUTHENTICATION_URL = "/authentication/sign_in";

    String PUBLIC_API = "/api";
    String PUBLIC_API_SHAREDSPACE_FORMAT = PUBLIC_API + "/shared_spaces/%s";
    String PUBLIC_API_WORKSPACE_FORMAT = PUBLIC_API_SHAREDSPACE_FORMAT + "/workspaces/%s";

    String PUBLIC_API_WORKSPACE_LEVEL_ENTITIES = PUBLIC_API_WORKSPACE_FORMAT + "/%s";

    String CLIENTTYPE_HEADER = "HPECLIENTTYPE";
    String CLIENTTYPE_INTERNAL = "HPE_MQM_UI";




}
