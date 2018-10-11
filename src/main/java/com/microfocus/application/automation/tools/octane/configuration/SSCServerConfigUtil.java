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

import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.lang.reflect.Field;
/*
    Utility to help retrieving the configuration of the SSC Server URL.
 */
public class SSCServerConfigUtil {

    public static String getSSCServer() {
        Descriptor sscDescriptor = getSSCDescriptor();
        return getSSCServerFromDescriptor(sscDescriptor);
    }

    private static String getSSCServerFromDescriptor(Descriptor sscDescriptor) {
        Object urlObj = getFieldValue(sscDescriptor, "url");
        if(urlObj != null) {
            return urlObj.toString();
        }
        return null;
    }

    private static Object getFieldValue(Object someObject, String fieldName) {
        for (Field field : someObject.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(field.getName().equals(fieldName)) {
                Object value = null;
                try {
                    value = field.get(someObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private static Descriptor getSSCDescriptor(){
        return Jenkins.getInstance().getDescriptorByName("com.fortify.plugin.jenkins.FPRPublisher");
    }

}
