/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.configuration;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.Messages;
import hudson.ProxyConfiguration;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationValidator {
    private final static Logger logger = LogManager.getLogger(ConfigurationValidator.class);

    private static final String PARAM_SHARED_SPACE = "p"; // NON-NLS

    private ConfigurationValidator(){
        //hiding public constructor
    }


    public static MqmProject parseUiLocation(String uiLocation) throws FormValidation {
        try {
            URL url = new URL(uiLocation);
            String location;
            int contextPos = uiLocation.indexOf("/ui");
            if (contextPos < 0) {
                throw wrapWithFormValidation(false, Messages.ApplicationContextNotFound());
            } else {
                location = uiLocation.substring(0, contextPos);
            }
            List<NameValuePair> params = URLEncodedUtils.parse(url.toURI(), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName().equals(PARAM_SHARED_SPACE)) {
                    String[] sharedSpaceAndWorkspace = param.getValue().split("/");
                    // we are relaxed and allow parameter without workspace in order not to force user to makeup
                    // workspace value when configuring manually or via config API and not via copy & paste
                    if (sharedSpaceAndWorkspace.length < 1 || StringUtils.isEmpty(sharedSpaceAndWorkspace[0])) {
                        throw wrapWithFormValidation(false, Messages.UnexpectedSharedSpace());
                    }
                    return new MqmProject(location, sharedSpaceAndWorkspace[0]);
                }
            }
            throw wrapWithFormValidation(false, Messages.MissingSharedSpace());
        } catch (MalformedURLException e) {
            throw wrapWithFormValidation(false, Messages.ConfigurationUrInvalid());
        } catch (URISyntaxException e) {
            throw wrapWithFormValidation(false, Messages.ConfigurationUrInvalid());
        }
    }

    /**
     * Used by tests only
     * @param location
     * @param sharedSpace
     * @param username
     * @param password
     * @return
     */
    public static FormValidation checkConfigurationAndWrapWithFormValidation(String location, String sharedSpace, String username, Secret password) {
        List<String> errors = new ArrayList<>();
        checkConfiguration(errors, location, sharedSpace, username, password);
        return wrapWithFormValidation(errors.isEmpty(), errors.isEmpty() ? Messages.ConnectionSuccess() : errors.get(0));
    }

    public static void checkConfiguration(List<String> errorMessages, String location, String sharedSpace, String username, Secret password) {
        OctaneResponse checkResponse;
        try {
            checkResponse = OctaneSDK.testOctaneConfiguration(location, sharedSpace, username, password.getPlainText(), CIJenkinsServicesImpl.class);

            if (checkResponse.getStatus() == 401) {
                errorMessages.add(Messages.AuthenticationFailure());
            } else if (checkResponse.getStatus() == 403) {
                errorMessages.add(Messages.AuthorizationFailure());
            } else if (checkResponse.getStatus() == 404) {
                errorMessages.add(Messages.ConnectionSharedSpaceInvalid());
            } else if (checkResponse.getStatus() != 200) {
                errorMessages.add(Messages.UnexpectedFailure() + ": " + checkResponse.getStatus());
            }
        } catch (IOException ioe) {
            logger.warn("Connection check failed due to communication problem", ioe);
            errorMessages.add(Messages.ConnectionFailure());
        }
    }

    public static void checkImpersonatedUser(List<String> errorMessages, String impersonatedUser) {

        //start impersonation
        Jenkins jenkins = Jenkins.getInstance();

        User jenkinsUser = null;
        if (StringUtils.isNotEmpty(impersonatedUser)) {
            jenkinsUser = User.get(impersonatedUser, false, Collections.emptyMap());
            if (jenkinsUser == null) {
                errorMessages.add(Messages.JenkinsUserMisconfiguredFailure());
                return;
            }
        }

        ACLContext impersonatedContext = ACL.as(jenkinsUser);

        //test permissions
        Map<Permission, String> requiredPermissions = new HashMap<>();
        requiredPermissions.put(Item.BUILD, "Job.BUILD");
        requiredPermissions.put(Item.READ, "Job.READ");
        Set<String> missingPermissions = requiredPermissions.keySet().stream().filter(p -> !jenkins.hasPermission(p)).map(p -> requiredPermissions.get(p)).collect(Collectors.toSet());
        if (!missingPermissions.isEmpty()) {
            errorMessages.add(String.format(Messages.JenkinsUserPermissionsFailure(), StringUtils.join(missingPermissions, ", ")));
        }

        //depersonate
        impersonatedContext.close();
    }

    public static FormValidation wrapWithFormValidation(boolean success, String message) {
        String color = success ? "green" : "red";
        String msg = "<font color=\"" + color + "\">" + message + "</font>";
        if (success) {
            return FormValidation.okWithMarkup(msg);
        } else {
            return FormValidation.errorWithMarkup(msg);
        }
    }

    public static void checkHoProxySettins(List<String> errorMessages) {
        ProxyConfiguration proxy = Jenkins.getInstance().proxy;
        boolean containsHttp = (proxy != null && proxy.getNoProxyHostPatterns().stream().anyMatch(p -> p.pattern().toLowerCase().startsWith("http")));
        if (containsHttp) {
            errorMessages.add("In the HTTP Proxy Configuration area, the No Proxy Host field must contain a host name only. Remove the http:// prefix before the host name.");
        }
    }
}
