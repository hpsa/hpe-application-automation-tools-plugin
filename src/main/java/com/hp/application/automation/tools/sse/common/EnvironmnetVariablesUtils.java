package com.hp.application.automation.tools.sse.common;

import com.hp.application.automation.tools.sse.sdk.Logger;

import hudson.Util;
import hudson.util.VariableResolver;

/**
 * Created by barush on 04/11/2014.
 */
public class EnvironmnetVariablesUtils {
    
    public static String reolveVariable(
            String rawValueToResolve,
            VariableResolver<String> buildResolver,
            Logger logger) {
        
        String resolvedValue = Util.replaceMacro(rawValueToResolve, buildResolver);
        if (StringUtils.isNullOrEmpty(resolvedValue)) {
            logger.log(String.format(
                    "Couldn't resolve the value of: [%s]. Empty sting will be used instead.",
                    resolvedValue));
        }
        
        return resolvedValue;
    }
}
