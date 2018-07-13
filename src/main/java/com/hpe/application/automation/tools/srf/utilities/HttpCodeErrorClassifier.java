/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
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
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.srf.utilities;

import com.hpe.application.automation.tools.srf.model.SrfException;
import com.hpe.application.automation.tools.srf.run.RunFromSrfBuilder;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;

import java.io.IOException;
import java.util.logging.Logger;

public class HttpCodeErrorClassifier {
    private static final Logger systemLogger = Logger.getLogger(RunFromSrfBuilder.class.getName());

    public static void throwError(int statusCode, String responseMessage) throws IOException, AuthorizationException, SrfException {
        systemLogger.fine(String.format("Received http status code %d with response message %s", statusCode, responseMessage));

        switch (statusCode) {
            case 401: throw new AuthenticationException("Failed to login, possibly wrong credentials supplied");
            case 403: throw new AuthorizationException("Operation is forbidden");
            default:
                JSONObject srfError = JSONObject.fromObject(responseMessage);
                if (SrfException.isSrfException(srfError))
                    throw new SrfException(srfError.getString("message"), srfError.getString("code"));

                throw new SrfException(String.format("Request failed with http code %d and http response %s", statusCode, responseMessage));
        }

    }

}
