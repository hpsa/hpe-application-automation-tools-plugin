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

package com.hpe.application.automation.tools.common.sdk;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.common.StringUtils;
import com.hpe.application.automation.tools.common.autenvironment.AUTEnvironmentManager;
import com.hpe.application.automation.tools.common.autenvironment.AUTEnvironmentParametersManager;
import com.hpe.application.automation.tools.common.autenvironment.request.put.AUTEnvironmnentParameter;
import com.hpe.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hpe.application.automation.tools.common.model.AutEnvironmentParameterModel;

import java.util.Collection;
import java.util.List;

public class AUTEnvironmentBuilderPerformer {

    private Client client;
    private Logger logger;
    private AutEnvironmentConfigModel model;

    public AUTEnvironmentBuilderPerformer(Client client, Logger logger, AutEnvironmentConfigModel model) {
        this.client = client;
        this.logger = logger;
        this.model = model;
    }

    public void start() throws Throwable {

        try {
            if (login()) {
                appendQCSessionCookies();
                performAutOperations();
            } else {
                throw new SSEException("Failed to login to ALM");
            }
        } catch (Throwable cause) {
            logger.log(String.format(
                    "Failed to update ALM AUT Environment. Cause: %s",
                    cause.getMessage()));
            throw cause;
        }
    }

    private boolean login() {

        boolean ret;
        try {
            ret =
                    new RestAuthenticator().login(
                            client,
                            model.getAlmUserName(),
                            model.getAlmPassword(),
                            logger);
        } catch (Throwable cause) {
            ret = false;
            logger.log(String.format(
                    "Failed login to ALM Server URL: %s. Exception: %s",
                    model.getAlmServerUrl(),
                    cause.getMessage()));
        }

        return ret;
    }

    private void appendQCSessionCookies() {

        Response response =
                client.httpPost(
                        client.build("rest/site-session"),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        if (!response.isOk()) {
            throw new SSEException("Cannot append QCSession cookies", response.getFailure());
        }
    }

    private void performAutOperations() {

        String autEnvironmentId = model.getAutEnvID();
        AUTEnvironmentManager autEnvironmentManager = new AUTEnvironmentManager(client, logger);
        String parametersRootFolderId = autEnvironmentManager.getParametersRootFolderIdByAutEnvId(autEnvironmentId);
        String autEnvironmentConfigurationId = getAutEnvironmentConfigurationId(autEnvironmentManager, autEnvironmentId);
        model.setCurrentConfigID(autEnvironmentConfigurationId);

        assignValuesToAutParameters(autEnvironmentConfigurationId, parametersRootFolderId);
    }

    private String getAutEnvironmentConfigurationId(
            AUTEnvironmentManager autEnvironmentManager,
            String autEnvironmentId) {

        String autEnvironmentConfigurationId =
                        autEnvironmentManager.shouldUseExistingConfiguration(model)
                        ? model.getAutEnvConf()
                        : autEnvironmentManager.createNewAutEnvironmentConfiguration(autEnvironmentId, model);

        if (StringUtils.isNullOrEmpty(autEnvironmentConfigurationId)) {
            throw new SSEException("There's no AUT Environment Configuration in order to proceed");
        }
        return autEnvironmentConfigurationId;

    }

    private void assignValuesToAutParameters(
            String autEnvironmentConfigurationId,
            String parametersRootFolderId) {

        List<AutEnvironmentParameterModel> confParams = model.getAutEnvironmentParameters();
        if (confParams == null || confParams.size() == 0) {
            logger.log("There's no AUT Environment parameters to assign for this build...");
            return;
        }

        AUTEnvironmentParametersManager parametersManager = new AUTEnvironmentParametersManager(
                        client,
                        confParams,
                        parametersRootFolderId,
                        autEnvironmentConfigurationId,
                        model.getPathToJsonFile(),
                        logger);

        Collection<AUTEnvironmnentParameter> parametersToUpdate = parametersManager.getParametersToUpdate();
        parametersManager.updateParametersValues(parametersToUpdate);
    }
}
