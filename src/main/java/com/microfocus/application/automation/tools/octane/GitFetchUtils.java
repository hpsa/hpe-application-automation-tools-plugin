package com.microfocus.application.automation.tools.octane;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.hp.octane.integrations.services.pullrequestsandbranches.rest.authentication.AuthenticationStrategy;
import com.hp.octane.integrations.services.pullrequestsandbranches.rest.authentication.BasicAuthenticationStrategy;
import com.hp.octane.integrations.services.pullrequestsandbranches.rest.authentication.NoCredentialsStrategy;
import com.hp.octane.integrations.services.pullrequestsandbranches.rest.authentication.PATStrategy;
import hudson.model.Run;
import hudson.model.User;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

public class GitFetchUtils {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private GitFetchUtils() {
        //codacy recommendation : Add a private constructor to hide the implicit public one.
    }

    /**
     * Generate date format for template : yyyy-MM-dd HH:mm
     *
     * @return
     */
    public static SimpleDateFormat generateDateFormat() {
        SimpleDateFormat temp = new SimpleDateFormat(DATE_TIME_FORMAT);
        TimeZone utc = TimeZone.getTimeZone("UTC");
        temp.setTimeZone(utc);
        return temp;
    }

    /**
     * Get user id by email and login. This method is used to return the same user Id for commits/pull request/branches
     *
     * @param email
     * @param login
     * @return
     */
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

    /**
     * Get user name password credentials by id.
     */
    public static StandardCredentials getCredentialsById(String credentialsId, Run<?, ?> run, PrintStream logger) {

        StandardCredentials credentials = null;
        if (!StringUtils.isEmpty(credentialsId)) {
            credentials = CredentialsProvider.findCredentialById(credentialsId,
                    StandardCredentials.class,
                    run,
                    URIRequirementBuilder.create().build());
            if (credentials == null) {
                logger.println("Can not find credentials with the credentialsId:" + credentialsId);
            }
        }

        return credentials;
    }

    public static AuthenticationStrategy getAuthenticationStrategy(StandardCredentials credentials) {
        AuthenticationStrategy authenticationStrategy;
        if (credentials == null) {
            authenticationStrategy = new NoCredentialsStrategy();
        } else if (credentials instanceof StringCredentials) {
            Secret secret = ((StringCredentials) credentials).getSecret();
            authenticationStrategy = new PATStrategy(secret.getPlainText());
        } else if (credentials instanceof StandardUsernamePasswordCredentials) {
            StandardUsernamePasswordCredentials cr = (StandardUsernamePasswordCredentials) credentials;
            authenticationStrategy = new BasicAuthenticationStrategy(cr.getUsername(), cr.getPassword().getPlainText());
        } else {
            throw new IllegalArgumentException("Credentials type is not supported : " + credentials.getClass().getCanonicalName());
        }

        return authenticationStrategy;
    }

}
