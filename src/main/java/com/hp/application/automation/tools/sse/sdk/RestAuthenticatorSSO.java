/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hp.application.automation.tools.sse.sdk;

import com.hp.application.automation.tools.common.SSEException;
import com.hp.idm.client.sdk.java.api.*;
import com.hp.idm.client.sdk.java.connectionsetting.ConnectionSettings;
import jenkins.model.Jenkins;

import java.net.*;
import java.util.List;
import java.util.Set;

/**
 * Rest Authenticator via SSO(Alm server pointed to ALM14)
 * Created by llu4 on 1/16/2017.
 */
public class RestAuthenticatorSSO implements Authenticator {
    private static final int NUMBER_OF_LOGIN_COOKIES = 2;
    private final String SESSION_COOKIE_PARAM_NAME = "SiteSession-";

    private SSOService ssoService;
    private SSOToken ssoToken;

    public RestAuthenticatorSSO() {
        ssoService = SSOClientSdkApi.getSSOService();
    }

    /**
     * Login
     * @param client client
     * @param username username
     * @param password password
     * @param logger logger
     * @return is login success
     */
    @Override
    public boolean login(Client client, String username, String password, Logger logger) {
        boolean result = false;

        URI url = null;
        try {
            url = new URI(client.getServerUrl());
        } catch (URISyntaxException e) {
            throw new SSEException(e);
        }

        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.ignoreServerCertificate(true);
        connectionSettings.turnOffHostVerifier(true);

        // Use the Jenkins' uberClassLoader otherwise the implements of Initializer can't be found.
        Thread.currentThread().setContextClassLoader(Jenkins.getInstance().getPluginManager().uberClassLoader);
        SSOConnector ssoConnector = ssoService.create(url, connectionSettings);
        if (ssoConnector != null) {
            SSOCredentials credentials = new UserNameSSOCredentials(username, password.toCharArray());
            ssoToken = ssoConnector.authenticate(credentials);
            if (ssoToken != null) {
                result = true;
                applyCookie(ssoToken, client);
                logger.log(String.format(
                        "Logged in successfully to ALM Server %s using %s",
                        client.getServerUrl(),
                        username));
            } else {
                throw new SSEException("Failed to login: can't create SSO connector.");
            }
        }

        return result;
    }

    /**
     * Currently SSO logout is not supported.
     * @param client client
     * @param username username
     * @return is logout success
     */
    @Override
    public boolean logout(Client client, String username) {
        return false;
    }

    /**
     * validate the SSO token
     * @param client client
     * @param logger logger
     * @return
     */
    public boolean isAuthenticated(Client client, Logger logger) {
        int i = 0;
        for (String cookieName : client.getCookies().keySet()) {
            if(cookieName.equals(ssoToken.getName()) && cookieName.contains(SESSION_COOKIE_PARAM_NAME)) {
                i++;
            }
        }
        return i == NUMBER_OF_LOGIN_COOKIES;
    }

    private void applyCookie(SSOToken ssoToken, Client client) {
        client.getCookies().put(ssoToken.getName(), ssoToken.getToken());
    }
}
