package com.hp.mqm.atrf.octane.services;

import com.hp.mqm.atrf.core.rest.RestConnector;
import com.hp.mqm.atrf.octane.core.OctaneTestResultOutput;
import com.hp.mqm.atrf.octane.entities.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by berkovir on 05/12/2016.
 */
public class OctaneWrapperService {

    static final Logger logger = LogManager.getLogger();
    RestConnector restConnector;
    OctaneEntityService octaneEntityService;


    public OctaneWrapperService(String baseUrl, long sharedSpaceId, long workspaceId) {

        restConnector = new RestConnector();
        restConnector.setBaseUrl(baseUrl);

        octaneEntityService = new OctaneEntityService(restConnector);
        octaneEntityService.setSharedSpaceId(sharedSpaceId);
        octaneEntityService.setWorkspaceId(workspaceId);
    }

    public boolean login(String user, String password) {
        return octaneEntityService.login(user, password);
    }

    public boolean validateConnectionToWorkspace() {
        try {
            octaneEntityService.getEntities(Test.COLLECTION_NAME, null);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public OctaneTestResultOutput postTestResults(String xml) {
        return octaneEntityService.postTestResults(xml);
    }

    public OctaneTestResultOutput getTestResultStatus(OctaneTestResultOutput output) {
        return octaneEntityService.getTestResultStatus(output);
    }
}
