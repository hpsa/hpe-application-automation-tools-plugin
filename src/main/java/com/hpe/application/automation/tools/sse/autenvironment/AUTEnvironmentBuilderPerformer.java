package com.hpe.application.automation.tools.sse.autenvironment;

import java.util.Collection;
import java.util.List;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.model.AUTEnvironmentResolvedModel;
import com.hpe.application.automation.tools.model.AutEnvironmentParameterModel;
import com.hpe.application.automation.tools.rest.RestClient;
import com.hpe.application.automation.tools.sse.common.StringUtils;
import com.hpe.application.automation.tools.sse.sdk.*;
import com.hpe.application.automation.tools.sse.sdk.Response;
import com.hpe.application.automation.tools.sse.sdk.authenticator.RestAuthenticator;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import com.hpe.application.automation.tools.sse.sdk.ResourceAccessLevel;
import hudson.util.VariableResolver;

/**
 * Created by barush on 29/10/2014.
 */
public class AUTEnvironmentBuilderPerformer {
    
    private Logger logger;
    private AUTEnvironmentResolvedModel model;
    private RestClient restClient;
    private VariableResolver<String> buildVariableResolver;
    private String autEnvironmentConfigurationIdToReturn;
    
    public AUTEnvironmentBuilderPerformer(
            AUTEnvironmentResolvedModel model,
            VariableResolver<String> buildVariableResolver,
            Logger logger) {
        
        this.model = model;
        this.logger = logger;
        this.buildVariableResolver = buildVariableResolver;
    }
    
    public void start() throws Throwable {
        
        try {
            if (login(getClient())) {
                appendQCSessionCookies(getClient());
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
    
    public String getAutEnvironmentConfigurationIdToReturn() {
        return autEnvironmentConfigurationIdToReturn;
    }
    
    private void performAutOperations() {
        
        String autEnvironmentId = model.getAutEnvironmentId();
        AUTEnvironmentManager autEnvironmentManager =
                new AUTEnvironmentManager(getClient(), logger);
        String parametersRootFolderId =
                autEnvironmentManager.getParametersRootFolderIdByAutEnvId(autEnvironmentId);
        String autEnvironmentConfigurationId =
                getAutEnvironmentConfigurationId(autEnvironmentManager, autEnvironmentId);
        
        assignValuesToAutParameters(autEnvironmentConfigurationId, parametersRootFolderId);
        autEnvironmentConfigurationIdToReturn = autEnvironmentConfigurationId;
        
    }
    
    private void assignValuesToAutParameters(
            String autEnvironmentConfigurationId,
            String parametersRootFolderId) {
        
        List<AutEnvironmentParameterModel> autEnvironmentParameters =
                model.getAutEnvironmentParameters();
        if (autEnvironmentParameters == null || autEnvironmentParameters.size() == 0) {
            logger.log("There's no AUT Environment parameters to assign for this build...");
            return;
        }
        
        AUTEnvironmentParametersManager parametersManager =
                new AUTEnvironmentParametersManager(
                        getClient(),
                        autEnvironmentParameters,
                        parametersRootFolderId,
                        autEnvironmentConfigurationId,
                        buildVariableResolver,
                        model.getPathToJsonFile(),
                        logger);
        
        Collection<AUTEnvironmnentParameter> parametersToUpdate =
                parametersManager.getParametersToUpdate();
        parametersManager.updateParametersValues(parametersToUpdate);
    }
    
    private boolean login(Client client) {
        
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
    
    private void appendQCSessionCookies(RestClient client) {
        
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
    
    private String getAutEnvironmentConfigurationId(
            AUTEnvironmentManager autEnvironmentManager,
            String autEnvironmentId) {
        
        String autEnvironmentConfigurationId =
                autEnvironmentManager.shouldUseExistingConfiguration(model)
                        ? model.getExistingAutEnvConfId()
                        : autEnvironmentManager.createNewAutEnvironmentConfiguration(
                                autEnvironmentId,
                                model);
        
        if (StringUtils.isNullOrEmpty(autEnvironmentConfigurationId)) {
            throw new SSEException("There's no AUT Environment Configuration in order to proceed");
        }
        return autEnvironmentConfigurationId;
        
    }
    
    private RestClient getClient() {
        if (restClient == null) {
            restClient =
                    new RestClient(
                            model.getAlmServerUrl(),
                            model.getAlmDomain(),
                            model.getAlmProject(),
                            model.getAlmUserName());
        }
        
        return restClient;
    }
}
