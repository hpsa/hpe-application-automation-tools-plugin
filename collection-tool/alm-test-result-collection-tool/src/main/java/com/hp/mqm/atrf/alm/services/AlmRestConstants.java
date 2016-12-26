package com.hp.mqm.atrf.alm.services;

/**
 */
public interface AlmRestConstants {

    String ALM_AUTH_XML = "<alm-authentication>\n\t<user>%s</user>\n\t<password>%s</password>\n</alm-authentication>";

    String ALM_REST_AUTHENTICATION = "/authentication-point/alm-authenticate";
    String ALM_REST_SESSION = "/rest/site-session";


    String ALM_REST_DOMAINS = "/rest/domains";
    String ALM_REST_PROJECTS = ALM_REST_DOMAINS + "/%s/projects";
    String ALM_REST_PROJECT = ALM_REST_PROJECTS +"/%s";
    String ALM_REST_PROJECT_ENTITIES_FORMAT = AlmRestConstants.ALM_REST_PROJECT + "/%s";




}
