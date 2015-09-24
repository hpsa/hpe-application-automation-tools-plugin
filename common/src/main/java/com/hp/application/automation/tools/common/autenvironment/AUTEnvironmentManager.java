/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package com.hp.application.automation.tools.common.autenvironment;

import com.hp.application.automation.tools.common.SSEException;
import com.hp.application.automation.tools.common.StringUtils;
import com.hp.application.automation.tools.common.XPathUtils;
import com.hp.application.automation.tools.common.autenvironment.request.get.GetAutEnvironmentByIdOldApiRequest;
import com.hp.application.automation.tools.common.autenvironment.request.get.GetAutEnvironmentByIdRequest;
import com.hp.application.automation.tools.common.autenvironment.request.get.GetAutEnvironmentConfigurationByIdRequest;
import com.hp.application.automation.tools.common.autenvironment.request.post.CreateAutEnvConfRequest;
import com.hp.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.Response;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AUTEnvironmentManager {
    
    public final static String ALM_AUT_ENVIRONMENT_CONFIGURATION_ID_FIELD = "id";
    
    private Logger logger;
    private Client client;
    
    public AUTEnvironmentManager(Client client, Logger logger) {
        
        this.client = client;
        this.logger = logger;
    }
    
    public String getParametersRootFolderIdByAutEnvId(String autEnvironmentId) {
        
        String parametersRootFolderId = null;
        Response response = new GetAutEnvironmentByIdRequest(client, autEnvironmentId).execute();
        /**
         * This if here for backward compatibility to ALM 11.52. After the support for version <
         * 12.00 will be removed this 'if' statement can be removed
         * **/
        if (!response.isOk() && response.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            response = new GetAutEnvironmentByIdOldApiRequest(client, autEnvironmentId).execute();
        }
        try {
            List<Map<String, String>> entities = XPathUtils.toEntities(response.toString());
            if (!response.isOk() || entities.size() != 1) {
                throw new SSEException(String.format(
                        "Failed to get AUT Environment with ID: [%s]",
                        autEnvironmentId), response.getFailure());
            }
            
            Map<String, String> autEnvironment = entities.get(0);
            parametersRootFolderId = //autEnvironment == null ? null : autEnvironment.get("parent-id");
                    autEnvironment == null ? null : autEnvironment.get("root-app-param-folder-id");
        } catch (Throwable e) {
            logger.log(String.format("Failed to parse response: %s", response));
        }
        
        return parametersRootFolderId;
    }
    
    public String createNewAutEnvironmentConfiguration(
            String autEnvironmentId,
            AutEnvironmentConfigModel autEnvironmentModel) {
        
        String newConfigurationName =
                autEnvironmentModel.isUseExistingAutEnvConf()
                        || StringUtils.isNullOrEmpty(autEnvironmentModel.getAutEnvConf())
                        ? createTempConfigurationName()
                        : autEnvironmentModel.getAutEnvConf();
        return createNewAutEnvironmentConfiguration(autEnvironmentId, newConfigurationName);
        
    }
    
    private String createNewAutEnvironmentConfiguration(
            String autEnvironmentId,
            String newAutEnvConfigurationName) {
        
        String newAutEnvironmentConfigurationId = null;
        Response response =
                new CreateAutEnvConfRequest(client, autEnvironmentId, newAutEnvConfigurationName).execute();
        if (!response.isOk()) {
            logger.log(String.format(
                    "Failed to create new AUT Environment Configuration named: [%s] for AUT Environment with id: [%s]",
                    newAutEnvConfigurationName,
                    autEnvironmentId));
            return null;
        }
        try {
            newAutEnvironmentConfigurationId =
                    XPathUtils.getAttributeValue(
                            response.toString(),
                            ALM_AUT_ENVIRONMENT_CONFIGURATION_ID_FIELD);
        } catch (Throwable e) {
            logger.log(String.format("Failed to parse response: %s", response));
        }
        
        return newAutEnvironmentConfigurationId;
    }
    
    public boolean shouldUseExistingConfiguration(AutEnvironmentConfigModel autEnvironmentModel) {
        
        return autEnvironmentModel.isUseExistingAutEnvConf()
               && isAutEnvironmentConfigurationExists(autEnvironmentModel.getAutEnvConf());

    }

    private boolean isAutEnvironmentConfigurationExists(String existingAutEnvConfId) {

        Response response =
                new GetAutEnvironmentConfigurationByIdRequest(client, existingAutEnvConfId).execute();
        if (!response.isOk() || XPathUtils.toEntities(response.toString()).size() != 1) {
            logger.log(String.format(
                    "Failed to get AUT Environment Configuration with ID: [%s]. Will try to create a new one",
                    existingAutEnvConfId));
            return false;
        }
        return true;

    }
    
    private String createTempConfigurationName() {
        
        return "Configuration_" + Calendar.getInstance().getTime().toString();
    }
}
