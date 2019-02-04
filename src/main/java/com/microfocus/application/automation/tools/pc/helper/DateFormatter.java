/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.pc.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    public static final String DEFAULT_PATTERN = "E yyyy MMM dd 'at' HH:mm:ss.SSS a zzz";

    private SimpleDateFormat simpleDateFormat;
    private String pattern;
    private String date;

    public DateFormatter(String pattern){
        this.pattern = pattern.isEmpty() ? DEFAULT_PATTERN : pattern;
        simpleDateFormat = new SimpleDateFormat (this.pattern);
        renewDate();
    }

    public void renewDate() {
        try {
            date = simpleDateFormat.format(new Date());
        }
        catch (Exception ex) {
            this.pattern = DEFAULT_PATTERN;
            simpleDateFormat = new SimpleDateFormat (this.pattern);
            date = simpleDateFormat.format(new Date());
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern.isEmpty() ? DEFAULT_PATTERN : pattern;
        simpleDateFormat = new SimpleDateFormat (this.pattern);
    }

    public String getDate(){
        try {
            renewDate();
            return date;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

}
