package com.hpe.application.automation.tools.sse;

import com.hpe.application.automation.tools.model.SseModel;
import com.hpe.application.automation.tools.model.SseProxySettings;
import com.hpe.application.automation.tools.rest.RestClient;
import com.hpe.application.automation.tools.sse.result.model.junit.Testsuites;
import com.hpe.application.automation.tools.sse.sdk.Args;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import com.hpe.application.automation.tools.sse.sdk.RunManager;
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
        
        Testsuites ret;

        Args args = new ArgsFactory().createResolved(model, buildVariableResolver);
        SseProxySettings proxySettings = model.getProxySettings();

        RestClient restClient;

        if (proxySettings != null) {
            // Construct restClient with proxy.
            String username = proxySettings.getFsProxyUserName();
            String password = proxySettings.getFsProxyPassword() == null ? null : proxySettings.getFsProxyPassword().getPlainText();
            String passwordCrypt = proxySettings.getFsProxyPassword() == null ? null : proxySettings.getFsProxyPassword().getEncryptedValue();

            restClient = new RestClient(args.getUrl(),
                            args.getDomain(),
                            args.getProject(),
                            args.getUsername(),
                            RestClient.setProxyCfg(proxySettings.getFsProxyAddress(), username, password));
            logger.log(String.format("Connect with proxy. Address: %s, Username: %s, Password: %s",
                    proxySettings.getFsProxyAddress(), username, passwordCrypt));
        }
        else {
            // Construct restClient without proxy.
            restClient = new RestClient(args.getUrl(),
                            args.getDomain(),
                            args.getProject(),
                            args.getUsername());
        }
        ret = _runManager.execute(restClient, args, logger);
        return ret;
    }
    
    public void stop() {
        _runManager.stop();
    }
}
