package com.hp.application.automation.tools.model;

import com.hp.application.automation.tools.sse.common.EnvironmnetVariablesUtils;
import com.hp.application.automation.tools.sse.sdk.Logger;
import hudson.util.VariableResolver;

/**
 * Created by barush on 09/11/2014.
 */
public class AUTEnvironmentModelResolver {
    
    public static AUTEnvironmentResolvedModel resolveModel(
            AutEnvironmentModel autEnvironmentModel,
            VariableResolver<String> buildResolver,
            Logger logger) {
        
        String resolvedUserName =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getAlmUserName(),
                        buildResolver,
                        logger);
        String resolvedAlmDomain =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getAlmDomain(),
                        buildResolver,
                        logger);
        String resolvedAlmProject =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getAlmProject(),
                        buildResolver,
                        logger);
        String resolvedAutEnvironmentId =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getAutEnvironmentId(),
                        buildResolver,
                        logger);
        String resolvedAutEnvConfId =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getExistingAutEnvConfId(),
                        buildResolver,
                        logger);
        String resolvedAutEnvConfName =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getNewAutEnvConfName(),
                        buildResolver,
                        logger);
        String resolvedJsonPath =
                EnvironmnetVariablesUtils.reolveVariable(
                        autEnvironmentModel.getPathToJsonFile(),
                        buildResolver,
                        logger);
        
        AUTEnvironmentResolvedModel autEnvironmentResolvedModel =
                new AUTEnvironmentResolvedModel(
                        autEnvironmentModel.getAlmServerName(),
                        autEnvironmentModel.getAlmServerUrl(),
                        resolvedUserName,
                        autEnvironmentModel.getAlmPassword(),
                        resolvedAlmDomain,
                        resolvedAlmProject,
                        resolvedAutEnvironmentId,
                        autEnvironmentModel.isUseExistingAutEnvConf(),
                        resolvedAutEnvConfId,
                        autEnvironmentModel.isCreateNewAutEnvConf(),
                        resolvedAutEnvConfName,
                        autEnvironmentModel.getAutEnvironmentParameters(),
                        resolvedJsonPath,
                        autEnvironmentModel.getOutputParameter());
        return autEnvironmentResolvedModel;
        
    }
}
