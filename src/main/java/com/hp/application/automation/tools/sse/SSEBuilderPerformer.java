package com.hp.application.automation.tools.sse;

import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.sse.result.model.junit.Testsuites;
import com.hp.application.automation.tools.sse.sdk.Args;
import com.hp.application.automation.tools.sse.sdk.Logger;
import com.hp.application.automation.tools.sse.sdk.RunManager;
import hudson.util.VariableResolver;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class SSEBuilderPerformer {
    
    private final RunManager _runManager = new RunManager();
    
    public Testsuites start(
            SseModel model,
            Logger logger,
            VariableResolver<String> buildVariableResolver) throws InterruptedException {
        
        Testsuites ret = new Testsuites();
        try {
            //Args args = new ArgsFactory().create(model);
            Args args = new ArgsFactory().createResolved(model, buildVariableResolver, logger);
            RestClient restClient =
                    new RestClient(
                            args.getUrl(),
                            args.getDomain(),
                            args.getProject(),
                            args.getUsername());
            ret = _runManager.execute(restClient, args, logger);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Throwable cause) {
            logger.log(String.format("Failed to execute ALM tests. Cause: %s", cause.getMessage()));
        }
        
        return ret;
    }
    
    public void stop() {
        
        _runManager.stop();
    }
}
