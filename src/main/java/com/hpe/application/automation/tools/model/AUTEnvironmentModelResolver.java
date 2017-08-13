package com.hpe.application.automation.tools.model;

import hudson.Util;
import hudson.util.VariableResolver;

/**
 * Created by barush on 09/11/2014.
 */
public class AUTEnvironmentModelResolver {
    
    public static AUTEnvironmentResolvedModel resolveModel(
            AutEnvironmentModel autEnvironmentModel,
            VariableResolver<String> buildResolver) {
        
        String resolvedUserName =
                Util.replaceMacro(autEnvironmentModel.getAlmUserName(), buildResolver);
        String resolvedAlmDomain =
                Util.replaceMacro(autEnvironmentModel.getAlmDomain(), buildResolver);
        String resolvedAlmProject =
                Util.replaceMacro(autEnvironmentModel.getAlmProject(), buildResolver);
        String resolvedAutEnvironmentId =
                Util.replaceMacro(autEnvironmentModel.getAutEnvironmentId(), buildResolver);
        String resolvedAutEnvConfId =
                Util.replaceMacro(autEnvironmentModel.getExistingAutEnvConfId(), buildResolver);
        String resolvedAutEnvConfName =
                Util.replaceMacro(autEnvironmentModel.getNewAutEnvConfName(), buildResolver);
        String resolvedJsonPath =
                Util.replaceMacro(autEnvironmentModel.getPathToJsonFile(), buildResolver);
        
        return new AUTEnvironmentResolvedModel(
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
        
    }
}
