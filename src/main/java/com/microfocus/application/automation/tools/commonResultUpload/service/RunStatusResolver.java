/*
 *
 *  *
 *  *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  *  marks are the property of their respective owners.
 *  * __________________________________________________________________
 *  * MIT License
 *  *
 *  * © Copyright 2012-2019 Micro Focus or one of its affiliates..
 *  *
 *  * The only warranties for products and services of Micro Focus and its affiliates
 *  * and licensors (“Micro Focus”) are set forth in the express warranty statements
 *  * accompanying such products and services. Nothing herein should be construed as
 *  * constituting an additional warranty. Micro Focus shall not be liable for technical
 *  * or editorial errors or omissions contained herein.
 *  * The information contained herein is subject to change without notice.
 *  * ___________________________________________________________________
 *  *
 *
 */

package com.microfocus.application.automation.tools.commonResultUpload.service;

import com.microfocus.application.automation.tools.results.service.almentities.IAlmConsts;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

public final class RunStatusResolver {

    private RunStatusResolver() { }

    public static String getRunStatus(String status, Map<String, String> runStatusMapping) {
        String passCondition = runStatusMapping.get(IAlmConsts.IStatuses.PASSED.value());
        String failedCondition = runStatusMapping.get(IAlmConsts.IStatuses.FAILED.value());

        if (!StringUtils.isEmpty(passCondition)) {
            return resolveCondition(status, passCondition)
                    ? IAlmConsts.IStatuses.PASSED.value() : IAlmConsts.IStatuses.FAILED.value();
        }
        if (!StringUtils.isEmpty(failedCondition)) {
            return resolveCondition(status, failedCondition)
                    ? IAlmConsts.IStatuses.FAILED.value() : IAlmConsts.IStatuses.PASSED.value();
        }

        return IAlmConsts.IStatuses.NO_RUN.value();
    }

    private static boolean resolveCondition(String statusValue, String condition) {
        String mark = condition.substring(0, 2);
        String conditionValue = condition.substring(2);

        if (conditionValue.equals("NULL")) {
            switch (mark) {
                case "==":
                    return StringUtils.isEmpty(statusValue);
                case "!=":
                    return !StringUtils.isEmpty(statusValue);
                default:
                    throw new IllegalArgumentException("Condition mark is incorrect for run status NULL.");
            }
        }

        switch (mark)  {
            case "==":
                if (isNumeric(conditionValue)) {
                    return Double.parseDouble(statusValue) == Double.parseDouble(conditionValue);
                } else {
                    return statusValue.equals(conditionValue);
                }
            case "!=":
                if (isNumeric(conditionValue)) {
                    return Double.parseDouble(statusValue) != Double.parseDouble(conditionValue);
                } else {
                    return !statusValue.equals(conditionValue);
                }
            case ">>":
                return Double.parseDouble(statusValue) > Double.parseDouble(conditionValue);
            case ">=":
                return Double.parseDouble(statusValue) >= Double.parseDouble(conditionValue);
            case "<<":
                return Double.parseDouble(statusValue) < Double.parseDouble(conditionValue);
            case "<=":
                return Double.parseDouble(statusValue) <= Double.parseDouble(conditionValue);
            default:
                throw new IllegalArgumentException("Condition mark is incorrect.");
        }
    }

    private static boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
