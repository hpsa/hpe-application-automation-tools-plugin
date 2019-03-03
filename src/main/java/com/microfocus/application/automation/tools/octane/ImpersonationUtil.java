package com.microfocus.application.automation.tools.octane;

import com.hp.octane.integrations.exceptions.PermissionException;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

public class ImpersonationUtil {

    private static final Logger logger = LogManager.getLogger(ImpersonationUtil.class);

    public static ACLContext startImpersonation(String instanceId) {
        OctaneServerSettingsModel settings = ConfigurationService.getSettings(instanceId);
        if (settings == null) {
            throw new IllegalStateException("failed to retrieve configuration settings by instance ID " + instanceId);
        }
        String user = settings.getImpersonatedUser();
        User jenkinsUser = null;
        if (user != null && !user.isEmpty()) {
            jenkinsUser = User.get(user, false, Collections.emptyMap());
            if (jenkinsUser == null) {
                throw new PermissionException(401);
            }
        } else {
            logger.info("No user set to impersonating to. Operations will be done using Anonymous user");
        }

        ACLContext impersonatedContext = ACL.as(jenkinsUser);
        return impersonatedContext;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void stopImpersonation(ACLContext impersonatedContext) {
        impersonatedContext.close();
    }
}
