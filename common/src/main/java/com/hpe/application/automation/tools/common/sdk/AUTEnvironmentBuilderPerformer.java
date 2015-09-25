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

/**
 * Created by mprilepina on 14/08/2015.
 */
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
