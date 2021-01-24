package com.microfocus.application.automation.tools.octane;

import com.hp.octane.integrations.services.pullrequestsandbranches.factory.CommitUserIdPicker;
import hudson.model.User;

import java.util.Collections;

public class GeneralUtils {

    public static CommitUserIdPicker getUserIdForCommit;

    public static String getUserIdForCommit(String email, String login) {
        if (login != null) {
            User user = User.get(login, false, Collections.emptyMap());
            if (user != null) {
                return user.getId();
            }
        }
        if (email != null && email.contains("@")) {
            String[] emailParts = email.split("@");
            return emailParts[0];

        }
        return login;
    }
}
