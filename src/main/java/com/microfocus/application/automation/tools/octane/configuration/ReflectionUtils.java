/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.configuration;

import hudson.model.Action;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/***
 * A utility class to help retrieving data from objects,
 * on whom we have no type data.
 */
public class ReflectionUtils {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(ReflectionUtils.class);

    public static <T>  T getFieldValue(Object someObject, String fieldName) {
        for (Field field : someObject.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals(fieldName)) {
                Object value = null;
                try {
                    value = field.get(someObject);
                } catch (IllegalAccessException e) {
                    logger.error("Failed to getFieldValue", e);
                }
                if (value != null) {
                    return (T)value;
                }
            }
        }
        return null;
    }

    public static Object invokeMethodByName(Action action, String methodName, Object... args) throws InvocationTargetException, IllegalAccessException {
        Method method = getMethodByName(action, methodName);

        return method.invoke(action, args);
    }

    public static Method getMethodByName(Action action, String methodName) {
        Method method = Arrays.stream(action.getClass().getDeclaredMethods())
                .filter(m->m.getName().equals(methodName))
                .findFirst().orElse(null);
        return method;
    }
}
